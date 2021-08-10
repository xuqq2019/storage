package com.xqq.oss.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * oss接口
 * @author xuqq
 * @date 2020-10-13
 * @version v1.0
 */
public interface ObjectStorageSystemService {
    /**
     * 创建存储桶
     * @author xuqq
     * @date 2020-10-13
     */
     void createBucket();
    /**
     * 文件上传
     * @param filePath 上传文件到OSS时需要指定包含文件后缀在内的完整路径，例如abc/efg/123.jpg。
     * @param inputStream 存储文件
     * @author xuqq
     * @date 2020-10-05
     */
    void upload(String filePath, InputStream inputStream);

    InputStream getObject(String path);

    InputStream getObject(String path, Long offset, Long length);

    Long getContentLength(String path);
    /**
     * 文件列表查询
     * @return List<Map<String, Object>>
     * @author xuqq
     * @date 2020-10-05
     */
    List<Map<String, Object>> queryFileFromFolder();
    /**
     * 文件删除
     * @param path 上传文件到OSS时需要指定包含文件后缀在内的完整路径，例如abc/efg/123.jpg
     * @author xuqq
     * @date 2020-10-05
     */
    void delete(String path);
    /**
     * 检查文件是否存在
     * @param path 上传文件到OSS时需要指定包含文件后缀在内的完整路径，例如abc/efg/123.jpg
     * @author xuqq
     * @date 2020-10-05
     */
    void check(String path);

    void sliceUpload(String objectName, MultipartFile file,long fileSliceSize);
}
