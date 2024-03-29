package com.train.usercenterservice.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用户的权限标识返回类-VO
 *
 * @author zhangLei
 * @serial 2019/09/20
 */
@Data
public class UserAuthorityVO {

    @ApiModelProperty("权限名名称")
    private String jurName;

    @ApiModelProperty("权限标识")
    private String identification;

}
