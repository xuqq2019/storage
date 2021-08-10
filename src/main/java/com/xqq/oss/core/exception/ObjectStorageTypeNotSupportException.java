package com.xqq.oss.core.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 对象存储插件异常
 * @author xuqq
 * @date 2020-09-09
 * @version v1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ObjectStorageTypeNotSupportException extends RuntimeException {

    /**
     * 构造函数
     * @author xuqq
     * @date 2020-10-15
     */
    public ObjectStorageTypeNotSupportException(){
        super("没有配置正确的对象存储插件类型");
    }
}
