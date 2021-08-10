package com.xqq.oss.core.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;

@Data
public class Result<T> {
    private String code;
    private String msg;
    @JsonInclude(Include.NON_NULL)
    private T data;
}
