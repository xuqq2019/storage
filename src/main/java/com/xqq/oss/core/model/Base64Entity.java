package com.xqq.oss.core.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class Base64Entity {
    @NotBlank(message = "base64字符不能为空")
    private String base64;
}
