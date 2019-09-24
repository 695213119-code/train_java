package com.train.authorityservice.service;

import com.train.authorityservice.dto.AddRoleDTO;
import com.train.commonservice.recurrence.RespRecurrence;
import com.train.entityservice.entity.authority.Role;
import org.springframework.validation.BindingResult;

/**
 * 角色APi接口类
 *
 * @author zhangLei
 * @serial 2019/09/24
 */
public interface ISystemRoleService {

    /**
     * 添加角色
     *
     * @param roleDTO       角色参数
     * @param bindingResult BindingResult
     * @return RespRecurrence
     */
    RespRecurrence addRole(AddRoleDTO roleDTO, BindingResult bindingResult);


    /**
     * 根据角色id查询角色
     *
     * @param roleId 角色id
     * @return RoleVO
     */
    Role getRoleServiceInvocation(Long roleId);
}
