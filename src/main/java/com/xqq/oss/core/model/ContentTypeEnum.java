package com.xqq.oss.core.model;


/**
 * 文件流类型
 * @author xuqq
 * @date 2020-10-13
 * @version v1.0
 */
public enum ContentTypeEnum {
    /**
     * 图片类型
     */
    PNG("image/png"),
    /**
     * 图片类型
     */
    JPG("image/jpeg"),
    /**
     * pdf类型
     */
    PDF("application/pdf"),
    /**
     * 类型
     */
    XLS("application/x-xls"),
    MP3("video/mp3"),
    MP4("video/mp4"),
    /**
     * 自动识别类型
     */
    MULTIPART("multipart/form-data");

    private final String value;

    ContentTypeEnum(String value){
        this.value=value;
    }

    public String getValue() {
        return value;
    }

}
