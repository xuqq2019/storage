package com.xqq.oss.core.model;


/**
 * 文件类型，文本，office，压缩包等等
 * @author xuqq
 * @date 2020-10-13
 * @version v1.0
 */
public enum FileTypeEnum {
    /**
     * 图片类型
     */
    PNG("png"),
    JPG("jpg"),
    JPEG("jpeg"),
    DOC("doc"),
    DOCX("docx"),
    PPT("ppt"),
    PDF("pdf"),
    XLS("xls"),
    XLSX("xlsx"),
    VISIO("vsd");

    private final String value;

    FileTypeEnum(String value){
        this.value=value;
    }

    public String getValue() {
        return value;
    }

}
