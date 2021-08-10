package com.xqq.oss.core.warn;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResourceWarnType  {
    /**
     * 异常配置枚举
     */
    USER_MISS("401", "请重新登录"),
    INVALID_REQUEST("040001", "无效请求"),
    INVALID_CLIENT("040002", "无效client_id"),
    INVALID_GRANT("040003", "无效授权"),
    INVALID_SCOPE("040004", "无效scope"),
    INVALID_TOKEN("040005", "会话超时！请重新登陆"),
    INSUFFICIENT_SCOPE("040010", "授权不足"),
    REDIRECT_URI_MISMATCH("040020", "redirect url不匹配"),
    ACCESS_DENIED("040030", "拒绝访问"),
    METHOD_NOT_ALLOWED("040040", "不支持该方法"),
    SERVER_ERROR("040050", "权限服务错误"),
    UNAUTHORIZED_CLIENT("040060", "未授权客户端"),
    UNAUTHORIZED("040061", "未授权"),
    UNSUPPORTED_RESPONSE_TYPE("040070", " 不支持的响应类型"),
    UNSUPPORTED_GRANT_TYPE("040071", "不支持的授权类型"),
    EXPIRED_TOKEN("040101", "会话失效！请重新登陆"),
    REMOTE_LOGIN("040102", "异地登陆！请重新登陆"),
    INVALID_NOT_TOKEN("040005", "越权访问！"),
    INVALID_REFERER_ERROR("040008", "跨站点请求伪造！"),
    INVALID_REFERER_LOGIN("040009", "未授权登陆"),
    INVALID_REFERER_USER("040011", "业务系统未授权,请添加授权！"),
    INVALID_REFERER_ORG("040012", "权限服务错误请联系管理员！");
    /**
     * 错误类型码
     */
    private final String code;
    /**
     * 错误类型描述信息
     */
    private final String msg;

}
