package com.xqq.oss.core.exception;

import com.xqq.oss.core.warn.ExceptionStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;


@EqualsAndHashCode(callSuper = true)
@Data
public class BusinessException extends RuntimeException {

    private String code;

    public BusinessException(String code, String message){
        super(message);
        this.code = code;
    }

    public BusinessException(String message){
        super(message);
        this.code = "-1";
    }

    public BusinessException(ExceptionStatus exceptionStatus){
        super(exceptionStatus.getMsg());
        this.code = exceptionStatus.getCode();
    }
}
