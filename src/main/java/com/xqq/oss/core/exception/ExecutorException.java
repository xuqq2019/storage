package com.xqq.oss.core.exception;

import com.xqq.oss.core.warn.ExceptionStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 线程池阻塞逻辑异常
 * @author xuqq
 * @date 2020-09-09
 * @version v1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ExecutorException extends RuntimeException {
    /**
     * 异常代码
     */
    private String code;

    /**
     * 构造函数
     * @param code 异常代码
     * @param message 异常信息
     * @author xuqq
     * @date 2020-10-15
     */
    public ExecutorException(String code, String message){
        super(message);
        this.code = code;
    }


    /**
     * 构造函数
     * @param exceptionStatus 异常状态信息
     * @author xuqq
     * @date 2020-10-15
     */
    public ExecutorException(ExceptionStatus exceptionStatus){
        super(exceptionStatus.getMsg());
        this.code = exceptionStatus.getCode();
    }
}
