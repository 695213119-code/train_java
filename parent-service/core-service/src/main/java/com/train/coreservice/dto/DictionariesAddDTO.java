package com.train.coreservice.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 添加字典DTO
 *
 * @author zhangLei
 * @serial 2019/09/18
 */
@Data
public class DictionariesAddDTO {

    @ApiModelProperty(value = "字典的key")
    @NotBlank(message = "字典的key不能为空")
    private String key;

    @ApiModelProperty(value = "字典的val")
    @NotBlank(message = "字典的val不能为空")
    private String value;

    @ApiModelProperty(value = "备注")
    @NotBlank(message = "备注不能为空")
    private String remarks;

}
