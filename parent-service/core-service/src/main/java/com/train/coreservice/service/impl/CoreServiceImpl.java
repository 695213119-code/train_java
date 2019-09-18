package com.train.coreservice.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.train.commonservice.entity.core.Interface;
import com.train.commonservice.exception.BusinessException;
import com.train.commonservice.recurrence.RespRecurrence;
import com.train.coreservice.core.service.IInterfaceService;
import com.train.coreservice.dto.FiltrateInterfaceDTO;
import com.train.coreservice.service.ICoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 核心API实现类
 *
 * @author zhangLei
 * @serial 2019/09/17
 */
@Service
public class CoreServiceImpl implements ICoreService {

    private final static Logger LOGGER = LoggerFactory.getLogger(CoreServiceImpl.class);

    private final IInterfaceService iInterfaceService;

    @Autowired
    public CoreServiceImpl(IInterfaceService iInterfaceService) {
        this.iInterfaceService = iInterfaceService;
    }


    @Override
    public boolean checkPathNeedsToken(String path) {
        Integer needsToken = 2;
        boolean sign = true;
        Interface anInterface = iInterfaceService.selectOne(new EntityWrapper<Interface>().eq("url", path));
        if (null != anInterface && needsToken.equals(anInterface.getIden())) {
            sign = false;
        }
        return sign;
    }


    @Override
    public RespRecurrence addFiltrateInterface(FiltrateInterfaceDTO filtrateInterfaceDTO) {
        Interface inter = new Interface();
        BeanUtils.copyProperties(filtrateInterfaceDTO, inter);
        try {
            iInterfaceService.insert(inter);
        } catch (Exception e) {
            LOGGER.error("添加过滤接口失败 原因:{} 参数:{}", e.getMessage(), filtrateInterfaceDTO);
            return new RespRecurrence<>().failure();
        }
        return new RespRecurrence<>().success();
    }
}