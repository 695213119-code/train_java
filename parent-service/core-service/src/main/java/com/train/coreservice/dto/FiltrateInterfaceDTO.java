package com.train.coreservice.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 过滤接口DTO
 *
 * @author zhangLei
 * @serial 2019/09/17
 */
@Data
public class FiltrateInterfaceDTO {

    @ApiModelProperty(value = "接口路径")
    private String url;

    @ApiModelProperty(value = "接口描述")
    private String desc;

    @Override
    public String toString() {
        return "FiltrateInterfaceDTO{" +
                "url='" + url + '\'' +
                ", desc='" + desc + '\'' +
                '}';
    }
}
