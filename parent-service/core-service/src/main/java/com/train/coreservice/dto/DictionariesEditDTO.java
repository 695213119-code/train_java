package com.train.coreservice.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 编辑字典DTO
 *
 * @author zhangLei
 * @serial 2019/09/18
 */
@Data
public class DictionariesEditDTO {

    @ApiModelProperty(value = "主键")
    @NotBlank(message = "字典id不能为空")
    private Long id;

    @ApiModelProperty(value = "字典的val")
    @NotBlank(message = "字典val不能为空")
    private String value;

    @ApiModelProperty(value = "备注")
    @NotBlank(message = "字典备注不能为空")
    private String remarks;
}