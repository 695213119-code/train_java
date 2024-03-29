package com.train.authorityservice.api;

import com.train.authorityservice.dto.AddAuthorityDTO;
import com.train.authorityservice.service.IAuthorityService;
import com.train.commonservice.recurrence.RespRecurrence;
import com.train.entityservice.entity.authority.Jurisdiction;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限模块API
 *
 * @author zhangLei
 * @serial 2019/09/21
 */
@RestController
@RequestMapping("/api/authority")
@Api(tags = "权限API", hidden = true)
public class AuthorityApiController {

    private final IAuthorityService authorityService;

    @Autowired
    public AuthorityApiController(IAuthorityService authorityService) {
        this.authorityService = authorityService;
    }


    @GetMapping("/getUserAuthorityServiceInvocation")
    @ApiOperation(value = "查询用户权限[服务调用API]")
    public List<Jurisdiction> getUserAuthorityServiceInvocation(@ApiParam(value = "用户id", required = true) @RequestParam Long userId) {
        return authorityService.getUserAuthorityServiceInvocation(userId);
    }

    @PostMapping("/addAuthority")
    @ApiOperation(value = "添加权限")
    public RespRecurrence addAuthority(@ApiParam(value = "权限参数DTO", required = true) @RequestBody AddAuthorityDTO authorityDTO,
                                       BindingResult bindingResult) {
        return authorityService.addAuthority(authorityDTO, bindingResult);
    }

}
