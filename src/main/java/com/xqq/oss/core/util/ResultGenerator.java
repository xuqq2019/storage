package com.xqq.oss.core.util;

import com.xqq.oss.core.model.Result;
import com.xqq.oss.core.exception.BusinessException;
import com.xqq.oss.core.warn.ExceptionStatus;
import com.xqq.oss.core.warn.ResourceWarnType;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletResponse;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * Result的封装类
 * @author xuqq
 * @date 2020-10-03
 * @version v1.0
 */
public class ResultGenerator {

   public static <T> Result<T> success(T data){
       Result<T> result =new Result<>();
       result.setCode("200");
       result.setMsg("处理成功");
       result.setData(data);
       return result;
   }

    public static Result<Object> success(){
        Result<Object> result =new Result<>();
        result.setCode("200");
        result.setMsg("处理成功");
        return result;
    }

    public static Result<Object> businessException(ExceptionStatus exceptionStatus){
        Result<Object> result =new Result<>();
        result.setCode(exceptionStatus.getCode());
        result.setMsg(exceptionStatus.getMsg());
        return result;
    }

    public static Result<Object> businessException(String msg){
        Result<Object> result =new Result<>();
        result.setCode("400");
        result.setMsg(msg);
        return result;
    }

    public static Result<Object> businessException(BusinessException businessException){
        Result<Object> result =new Result<>();
        result.setCode(businessException.getCode());
        result.setMsg(businessException.getMessage());
        return result;
    }

    public static Result<Object> fail(ExceptionStatus exceptionStatus){
        HttpServletResponse response = getResponse();
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        Result<Object> result =new Result<>();
        result.setCode(exceptionStatus.getCode());
        result.setMsg(exceptionStatus.getMsg());
        return result;
    }

    public static Result<Object> businessException(ResourceWarnType resourceWarnType){
        Result<Object> result =new Result<>();
        result.setCode(resourceWarnType.getCode());
        result.setMsg(resourceWarnType.getMsg());
        return result;
    }

    public static Result<Object> fail(String msg){
        HttpServletResponse response = getResponse();
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        Result<Object> result =new Result<>();
        result.setCode("400");
        result.setMsg(msg);
        return result;
    }

    private static HttpServletResponse getResponse(){
        return ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getResponse();
    }
}
