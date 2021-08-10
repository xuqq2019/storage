package com.xqq.oss.core.warn;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 异常状态信息
 * @author xuqq
 * @date 2020-09-09
 * @version v1.0
 */
@Getter
@AllArgsConstructor
public enum ExceptionStatus {
    /**
     * {@code 1000 成功请求，但是结果不是期望的成功结果}
     */
    SUCCEED_REQUEST_FAILED_RESULT("10100", "不是期望的结果功"),
    FILE_TYPE_NOT_SUPPORT("10101", "暂不支持该文件类型"),
    FILE_ALREADY_EXISTS("10102", "文件已存在"),
    FILE_NAME_NOT_EXISTS("10103", "文件名不存在"),
    FILE_MAX_STORAGE("10104", "上传的资源大小不能超出规定阈值"),
    FILE_MIN_STORAGE("10105", "上传的资源大小不能低于规定阈值"),
    FILE_TRANSFORM_EXCEPTION("10106", "文件转换异常"),
    OBJECT_GET_EXCEPTION("10107", "获取对象异常"),
    RESOURCES_NOT_FOUND_EXCEPTION("10108", "资源未找到"),
    FILE_DOWNLOAD_EXCEPTION("10109", "文件下载异常"),
    FILE_UPLOAD_EXCEPTION("10110", "文件上传异常"),
    IMAGE_BATCH_DOWNLOAD_EXCEPTION("10111", "文件批量下载异常"),
    FILE_BATCH_DOWNLOAD_EXCEPTION("10112", "文件批量下载异常"),
    FILE_BATCH_UPLOAD_EXCEPTION("10113", "文件批量上传异常"),
    BUCKET_CREATE_EXCEPTION("10114", "创建存储桶异常"),
    FILE_PREVIEW_EXCEPTION("10115", "文件预览异常"),
    VIOLATION_EXCEPTION("10116", "验证异常"),
    OBJECT_EXIST_EXCEPTION("10117", "对象已存在"),
    OBJECT_NOT_EXIST_EXCEPTION("10118", "关联对象不存在"),
    OBJECT_ADD_EXCEPTION("10119", "对象新增时异常"),
    OBJECT_UPDATE_EXCEPTION("10120", "对象更新时异常"),
    OBJECT_DELETE_EXCEPTION("10121", "对象删除时异常"),
    OBJECT_NAME_PARSE_EXCEPTION("10122", "对象名称为[名称.文件类型]格式"),
    OBJECT_LENGTH_LIMIT("10123", "图片数量达到设定阈值"),
    PARAM_PARSE_EXCEPTION("10124", "参数解析异常"),
    PARAM_SIZE_EXCEPTION("10125", "参数数量超出规定阈值"),
    MAP_STORAGE_SIZE_LIMIT("10126", "待转换的文件数量达到设定阈值"),
    SERVICE_EXCEPTION("10127", "服务异常"),
    FILE_SIZE_EXCEPTION("10128", "获取文件大小异常"),
    IMAGE_BASE64_PARSE_EXCEPTION("10129", "base64图片解析异常"),
    QUEUE_PUT_EXCEPTION("10130", "队列新增数据异常"),
    OSS_CLIENT_EXCEPTION("10131", "OSS客户端连接异常"),
    RANGE_EXCEPTION("10132", "请求头未放入Range"),
    /**
     * 未定义错误
     */
    NO_DEFINED("19999", "未定义错误");

    /**
     * 错误码
     */
    private final String code;
    /**
     * 错误信息
     */
    private final String msg;

}
