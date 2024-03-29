package com.train.usercenterservice.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.toolkit.IdWorker;
import com.train.commonservice.constant.CommonConstant;
import com.train.commonservice.constant.SqlConstant;
import com.train.commonservice.constant.user.UserConstant;
import com.train.commonservice.enumeration.CommonEnum;
import com.train.commonservice.recurrence.RespPageRecurrence;
import com.train.commonservice.recurrence.RespRecurrence;
import com.train.commonservice.utils.RandomUtils;
import com.train.entityservice.entity.authority.Jurisdiction;
import com.train.entityservice.entity.authority.Role;
import com.train.entityservice.entity.user.User;
import com.train.entityservice.entity.user.UserSubsidiary;
import com.train.entityservice.entity.user.UserThirdparty;
import com.train.usercenterservice.dto.AddAdministratorsDTO;
import com.train.usercenterservice.dto.QueryUserManagementLoginDTO;
import com.train.usercenterservice.dto.QueryUserTabulationDTO;
import com.train.usercenterservice.mapper.UserCenterMapper;
import com.train.usercenterservice.remote.authority.RemoteAuthorityService;
import com.train.usercenterservice.service.IUserCenterService;
import com.train.usercenterservice.user.service.IUserService;
import com.train.usercenterservice.user.service.IUserSubsidiaryService;
import com.train.usercenterservice.user.service.IUserThirdpartyService;
import com.train.usercenterservice.utils.MybatisPageConvertRespPageUtils;
import com.train.usercenterservice.utils.RedisUtils;
import com.train.usercenterservice.utils.UserInfoHolderUtils;
import com.train.usercenterservice.vo.TokenVO;
import com.train.usercenterservice.vo.UserAuthorityVO;
import com.train.usercenterservice.vo.UserInfoVO;
import com.train.usercenterservice.vo.UserTabulationVO;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 用户模块实现类
 *
 * @author zhangLei
 * @serial 2019/09/13
 */
@Service
public class UserCenterServiceImpl implements IUserCenterService {

    private final static Logger LOGGER = LoggerFactory.getLogger(UserCenterServiceImpl.class);

    private final IUserService userService;
    private final RedisUtils redisUtils;
    private final UserInfoHolderUtils userInfoHolderUtils;
    private final RemoteAuthorityService remoteAuthorityService;
    private final IUserSubsidiaryService userSubsidiaryService;
    private final IUserThirdpartyService userThirdpartyService;
    private final UserCenterMapper userCenterMapper;


    @Autowired
    public UserCenterServiceImpl(IUserService userService, RedisUtils redisUtils, UserInfoHolderUtils userInfoHolderUtils, RemoteAuthorityService remoteAuthorityService, IUserSubsidiaryService userSubsidiaryService, IUserThirdpartyService userThirdpartyService, UserCenterMapper userCenterMapper) {
        this.userService = userService;
        this.redisUtils = redisUtils;
        this.userInfoHolderUtils = userInfoHolderUtils;
        this.remoteAuthorityService = remoteAuthorityService;
        this.userSubsidiaryService = userSubsidiaryService;
        this.userThirdpartyService = userThirdpartyService;
        this.userCenterMapper = userCenterMapper;
    }


    @Override
    public boolean checkUserTokenServiceInvocation(String userToken) {
        boolean sign = false;
        String userJsonString = redisUtils.get(userToken);
        if (StringUtils.isNotEmpty(userJsonString)) {
            User user = JSONObject.parseObject(userJsonString, User.class);
            User userReal = userService.selectById(user.getId());
            if (null != userReal) {
                sign = true;
            }
        }
        return sign;
    }

    @Override
    public RespRecurrence userManagementLogin(QueryUserManagementLoginDTO userManagementLoginDTO, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return new RespRecurrence().failure(CommonEnum.INVALID_PARAMETER.getCode(), bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        User user = userService.selectOne(new EntityWrapper<User>().eq(SqlConstant.SQL_FIELD_PHONE, userManagementLoginDTO.getPhone()).
                eq(CommonConstant.SQL_DELETE_SIGN, CommonConstant.SQL_DELETE_SIGN_NOT));

        if (null == user) {
            return new RespRecurrence<UserInfoVO>().failure("用户不存在");
        }

        //TODO 密码加密待完成,目前先不处理
        if (!user.getPassword().equals(userManagementLoginDTO.getPassword())) {
            return new RespRecurrence<UserInfoVO>().failure("密码输入错误");
        }

        //TODO 判断权限

        //踢出上一个用户
        clearUserResidualCache(user.getPhone());
        String token = RandomUtils.generateToken();
        redisUtils.set(token, JSONObject.toJSONString(user), 24, TimeUnit.HOURS);
        redisUtils.set(UserConstant.USER_MANAGEMENT_TOKEN_REDIS_KEY + user.getPhone(), token, 24, TimeUnit.HOURS);
        return new RespRecurrence<>().success(new TokenVO(token));
    }

    @Override
    public RespRecurrence getUserDetails(String key) {
        Long userId = userInfoHolderUtils.getUserId();
        User user = userService.selectOne(new EntityWrapper<User>().eq(SqlConstant.SQL_FIELD_ID, userId).
                eq(CommonConstant.SQL_DELETE_SIGN, CommonConstant.SQL_DELETE_SIGN_NOT));
        if (null == user) {
            return new RespRecurrence().failure(CommonEnum.BUSINESS_CODE.getCode(), "非法的用户");
        }

        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setUserId(String.valueOf(user.getId()));
        userInfoVO.setPhone(user.getPhone());

        // 查询角色
        UserSubsidiary userSubsidiary = userSubsidiaryService.selectOne(new EntityWrapper<UserSubsidiary>().eq(SqlConstant.SQL_FIELD_USER_ID, user.getId())
                .eq(CommonConstant.SQL_DELETE_SIGN, CommonConstant.SQL_DELETE_SIGN_NOT));
        if (null == userSubsidiary) {
            return new RespRecurrence().failure(CommonEnum.BUSINESS_CODE.getCode(), "非法的用户角色");
        }
        Role role = remoteAuthorityService.getRoleServiceInvocation(user.getRoleId());
        if (null != role) userInfoVO.setRoleName(role.getRoleName());

        //查询权限
        List<Jurisdiction> jurisdictionList = remoteAuthorityService.getUserAuthorityServiceInvocation(user.getId());
        if (CollUtil.isNotEmpty(jurisdictionList)) {
            List<UserAuthorityVO> userAuthorityList = new ArrayList<>();
            jurisdictionList.forEach(x -> {
                UserAuthorityVO authority = new UserAuthorityVO();
                BeanUtils.copyProperties(x, authority);
                userAuthorityList.add(authority);
            });
            userInfoVO.setUserAuthority(userAuthorityList);
        }

        final String phone = "1";
        if (phone.equals(key)) {
            userInfoVO.setAge(userSubsidiary.getAge());
            userInfoVO.setAvatar(userSubsidiary.getAvatar());
            userInfoVO.setGender(userSubsidiary.getGender());
            userInfoVO.setNickName(userSubsidiary.getNickName());
            userInfoVO.setCreateTime(user.getCreateTime());
        } else {
            final String wechat = "2";
            int platform = key.equals(wechat) ? UserConstant.THIRD_PLATFORM_TYPE_WECHAT : UserConstant.THIRD_PLATFORM_TYPE_QQ;
            UserThirdparty userThirdparty = userThirdpartyService.selectOne(new EntityWrapper<UserThirdparty>().eq(SqlConstant.SQL_FIELD_USER_ID, user.getId())
                    .eq(SqlConstant.SQL_FIELD_PLATFORM, platform)
                    .eq(CommonConstant.SQL_DELETE_SIGN, CommonConstant.SQL_DELETE_SIGN_NOT));
            if (null != userThirdparty) {
                userInfoVO.setAge(userThirdparty.getAge());
                userInfoVO.setAvatar(userThirdparty.getAvatar());
                userInfoVO.setGender(userThirdparty.getGender());
                userInfoVO.setNickName(userThirdparty.getNickName());
                userInfoVO.setCreateTime(userThirdparty.getCreateTime());
            }
        }
        return new RespRecurrence<>().success(userInfoVO);
    }

    @Override
    public RespRecurrence userLogOut() {
        userInfoHolderUtils.deleteUserToken();
        return new RespRecurrence().success();
    }

    @Override
    public RespPageRecurrence queryUserTabulation(QueryUserTabulationDTO queryUserTabulationDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new RespPageRecurrence().failure(CommonEnum.INVALID_PARAMETER.getCode(), bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        Page<UserTabulationVO> page = new Page<>(queryUserTabulationDTO.getPage(), queryUserTabulationDTO.getLimit());
        List<UserTabulationVO> userTabulations = userCenterMapper.queryUserTabulation(page, queryUserTabulationDTO);
        return new RespPageRecurrence<>().success(userTabulations, MybatisPageConvertRespPageUtils.convert(page));
    }

    @Override
    public RespRecurrence addAdministrators(AddAdministratorsDTO administrators, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return new RespRecurrence().failure(CommonEnum.INVALID_PARAMETER.getCode(), bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        User userCheck = userService.selectOne(new EntityWrapper<User>().eq(SqlConstant.SQL_FIELD_PHONE, administrators.getPhone()));
        if (null != userCheck) {
            return new RespRecurrence().failure(CommonEnum.BUSINESS_CODE.getCode(), "此用户已经存在,请勿重复添加");
        }

        Long userId = IdWorker.getId();
        User user = new User();
        user.setId(userId);
        user.setPhone(administrators.getPhone());
        user.setRoleId(Long.parseLong(administrators.getRoleId()));
        //TODO 密码待加密
        user.setPassword(administrators.getPassword());

        UserSubsidiary userSubsidiary = new UserSubsidiary();
        userSubsidiary.setUserId(userId);
        //用户昵称默认为手机号码
        userSubsidiary.setNickName(administrators.getPhone());

        try {
            userService.insert(user);
            userSubsidiaryService.insert(userSubsidiary);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("添加管理员失败,参数:{}", administrators);
        }

        return new RespRecurrence().success();
    }

    @Override
    public RespRecurrence resetPassword(Long userId) {

        User user = userService.selectById(userId);

        if (null == user) {
            return new RespRecurrence().failure(CommonEnum.BUSINESS_CODE.getCode(), "非法的用户");
        }

        //TODO 待完善
        user.setPassword("123456");
        user.setUpdateTime(new Date());

        try {
            userService.updateById(user);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("用户重置密码失败");
        }

        return new RespRecurrence().success();
    }


    /**
     * 清空用户残留的token-redis缓存
     *
     * @param phone 用户手机号
     */
    private void clearUserResidualCache(String phone) {
        String userTokenRedisKey = redisUtils.get(UserConstant.USER_MANAGEMENT_TOKEN_REDIS_KEY + phone);
        if (StringUtils.isNotEmpty(userTokenRedisKey)) {
            redisUtils.delete(userTokenRedisKey);
        }
    }


}
