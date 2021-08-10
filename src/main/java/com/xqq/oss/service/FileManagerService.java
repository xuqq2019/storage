package com.xqq.oss.service;

import com.xqq.oss.core.model.Result;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 文件管理服务
 * @author xuqq
 * @date 2020-10-13
 * @version v1.0
 */
public interface FileManagerService {
    /**
     * 创建存储桶
     * @return Result
     * @author xuqq
     * @date 2020-10-13
     */
     Result<Object> createBucket();
    /**
     * 文件上传
     * @param file 上传文件到OSS时需要指定包含文件后缀在内的完整路径，例如abc/efg/123.jpg
     * @return Result
     * @author xuqq
     * @date 2020-10-05
     */
     Result<Object> uploadFile(MultipartFile file);
    /**
     * 文件下载
     * @param objectName 上传文件到OSS时需要指定包含文件后缀在内的完整路径，例如abc/efg/123.jpg
     * @author xuqq
     * @date 2020-10-05
     */
     void downloadFile(String objectName);

    /**
     * base64图片批量下载
     * @param list 批量上传图片的base64字符集合
     * @author xuqq
     * @date 2020-10-05
     */
    void downloadBatch(Set<String> list);
    /**
     * 文件预览
     * @param path 上传文件到OSS时需要指定包含文件后缀在内的完整路径，例如abc/efg/123.jpg
     * @author xuqq
     * @date 2020-10-05
     */
     void previewFile(String path);
    /**
     * 文件删除
     * @param path 上传文件到OSS时需要指定包含文件后缀在内的完整路径，例如abc/efg/123.jpg
     * @return Result
     * @author xuqq
     * @date 2020-10-05
     */
    Result<Object> delete(String path);
    /**
     * 检查文件是否存在
     * @param path 上传文件到OSS时需要指定包含文件后缀在内的完整路径，例如abc/efg/123.jpg
     * @return Result
     * @author xuqq
     * @date 2020-10-05
     */
    Result<Object> check(String path);
    /**
     * 文件列表查询
     * @return Result
     * @author xuqq
     * @date 2020-10-05
     */
    Result<List<Map<String, Object>>> queryFileFromFolder();

    /**
     * base64图片上传
     * @param base64 上传图片的base64字符
     * @return Result
     * @author xuqq
     * @date 2020-10-05
     */
    Result<Map<String, Object>> imageUpload(String base64);

    /**
     * base64图片批量上传
     * @param list 批量上传图片的base64字符集合
     * @return Result
     * @author xuqq
     * @date 2020-10-05
     */
    Result<List<Map<String, Object>>> imageBatchUpload(List<String> list);

    /**
     * 图片预览
     * @param path 上传文件到OSS时需要指定包含文件后缀在内的完整路径，例如abc/efg/123.jpg
     * @return Result
     * @author xuqq
     * @date 2020-10-05
     */
    Result<Object> previewImage(String path);
    /**
     * 文件预览
     * @param path 上传文件到OSS时需要指定包含文件后缀在内的完整路径，例如abc/efg/123.jpg
     * @param rangeString range参数
     * @author xuqq
     * @date 2020-10-05
     */
    void previewVideo(String path,String rangeString);
}
