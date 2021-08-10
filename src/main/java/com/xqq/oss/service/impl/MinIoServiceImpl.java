package com.xqq.oss.service.impl;

import com.xqq.oss.core.exception.BusinessException;
import com.xqq.oss.core.warn.ExceptionStatus;
import com.xqq.oss.service.ObjectStorageSystemService;
import io.minio.*;
import io.minio.messages.Item;
import io.minio.messages.Tags;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * minio对象接口实现类
 *
 * @author xuqq
 * @version v1.0
 * @date 2020-10-13
 */
@Slf4j
public class MinIoServiceImpl implements ObjectStorageSystemService {
    @Resource
    private MinioClient minioClient;
    @Value("${oss.file.min-storage}")
    private long fileMinStorage;
    @Value("${oss.file.max-storage}")
    private long fileMaxStorage;
    @Value("${oss.file.cover}")
    private boolean fileCover;
    @Value("${oss.bucket.file.name}")
    private String bucketName;


    /**
     * 创建存储桶
     *
     * @author xuqq
     * @date 2020-10-13
     */
    @Override
    public void createBucket() {
        try {
            //判断存储空间是否已存在
            BucketExistsArgs bucketExistsArgs = BucketExistsArgs.builder().bucket(bucketName).build();
            // 检查存储桶是否已经存在
            boolean isExist = minioClient.bucketExists(bucketExistsArgs);
            if (!isExist) {
                log.info("start create Bucket");
                // 创建一个名为**的存储桶，用于存储照片的zip文件。
                MakeBucketArgs makeBucketArgs = MakeBucketArgs.builder().bucket(bucketName).build();
                minioClient.makeBucket(makeBucketArgs);
            }
        } catch (Exception e) {
            log.error("bucket creation exception", e);
            throw new BusinessException(ExceptionStatus.BUCKET_CREATE_EXCEPTION);
        }
    }

    /**
     * 文件上传
     *
     * @param filePath    上传文件到OSS时需要指定包含文件后缀在内的完整路径，例如abc/efg/123.jpg。
     * @param inputStream 存储文件
     * @author xuqq
     * @date 2020-10-05
     */
    @Override
    public void upload(String filePath, InputStream inputStream) {
        int size;
        try {
            //获取文件空间大小
            size = inputStream.available();
        } catch (IOException e) {
            log.error("client exception", e);
            throw new BusinessException(ExceptionStatus.FILE_UPLOAD_EXCEPTION);
        }
        upload(inputStream, bucketName, filePath, size);
    }

    /**
     * 文件预览
     *
     * @param path 上传文件到OSS时需要指定包含文件后缀在内的完整路径，例如abc/efg/123.jpg
     * @author xuqq
     * @date 2020-10-05
     */
    @Override
    public InputStream getObject(String path) {
        //文件不存在时则抛出异常提示没有该文件
        objectExist(path);
        try {
            // 调用objectStorageServiceClient.getObject返回一个OSSObject实例，该实例包含文件内容及文件元信息。
            GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket(bucketName).object(path).build();
            return minioClient.getObject(getObjectArgs);
        } catch (Exception e) {
            log.error("get resource exception", e);
            throw new BusinessException(ExceptionStatus.OBJECT_GET_EXCEPTION);
        }
    }

    /**
     * 文件预览
     *
     * @param path 上传文件到OSS时需要指定包含文件后缀在内的完整路径，例如abc/efg/123.jpg
     * @author xuqq
     * @date 2020-10-05
     */
    @Override
    public InputStream getObject(String path,Long offset,Long length) {
        //文件不存在时则抛出异常提示没有该文件
        objectExist(path);
        try {
            // 调用objectStorageServiceClient.getObject返回一个OSSObject实例，该实例包含文件内容及文件元信息。
            GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket(bucketName).object(path).offset(offset).length(length).build();
            return minioClient.getObject(getObjectArgs);
        } catch (Exception e) {
            log.error("get range {}-{} resource exception",offset,length, e);
            throw new BusinessException(ExceptionStatus.OBJECT_GET_EXCEPTION);
        }
    }

    /**
     * 文件预览
     *
     * @param path 上传文件到OSS时需要指定包含文件后缀在内的完整路径，例如abc/efg/123.jpg
     * @author xuqq
     * @date 2020-10-05
     */
    @Override
    public Long getContentLength(String path) {
        //文件不存在时则抛出异常提示没有该文件
        objectExist(path);
        try {
            // 调用objectStorageServiceClient.getObjectUrl返回一个OSSObject访问url实例，该实例包含文件内容及文件元信息。
            GetObjectTagsArgs getObjectArgs = GetObjectTagsArgs.builder().bucket(bucketName).object(path).build();
            Tags args = minioClient.getObjectTags(getObjectArgs);
            Map<String, String> map = args.get();
            for (String s : map.keySet()) {
                System.out.println(s+""+map.get(s)+"-");
            }
            return 99999999L;
        } catch (Exception e) {
            log.error("get resource request url exception", e);
            throw new BusinessException(ExceptionStatus.OBJECT_GET_EXCEPTION);
        }
    }

    /**
     * 文件列表查询
     *
     * @author xuqq
     * @date 2020-10-05
     */
    @Override
    public List<Map<String, Object>> queryFileFromFolder() {
        try {
            // objectStorageServiceClient.listObjects返回ObjectListing实例，包含此次listObject请求的返回结果。
            ListObjectsArgs listObjectsArgs = new ListObjectsArgs();
            Iterable<Result<Item>> objectListing = minioClient.listObjects(listObjectsArgs);
            //objectListing.getObjectSummaries获取所有文件的描述信息
            List<Map<String, Object>> list = new ArrayList<>();
            for (Result<Item> itemResult : objectListing) {
                Item item = itemResult.get();
                Map<String, Object> map = new HashMap<>(2);
                log.info(" - {} (size = {})",item.objectName(),item.size());
                map.put("name", item.objectName());
                map.put("size", item.size());
                list.add(map);
            }
            return list;
        } catch (Exception e) {
            log.error("get resource list exception", e);
            throw new BusinessException(ExceptionStatus.RESOURCES_NOT_FOUND_EXCEPTION);
        }
    }

    @Override
    public void delete(String path) {
        //文件不存在时则抛出异常提示没有该文件
        objectExist(path);
        try {
            // 调用objectStorageServiceClient.getObject返回一个OSSObject实例，该实例包含文件内容及文件元信息。
            DeleteObjectTagsArgs deleteObjectTagsArgs = DeleteObjectTagsArgs.builder().bucket(bucketName).object(path).build();
            minioClient.deleteObjectTags(deleteObjectTagsArgs);
        } catch (Exception e) {
            log.error("delete resource exception", e);
            throw new BusinessException(ExceptionStatus.OBJECT_DELETE_EXCEPTION);
        }
    }

    @Override
    public void check(String path) {
        objectExist(path);
    }

    @Override
    public void sliceUpload(String objectName, MultipartFile file,long fileSliceSize) {
        InputStream is;
        int fileSize;
        try {
            is = file.getInputStream();
            fileSize = is.available();
        } catch (IOException e) {
            log.error("解析上传文件异常:",e);
            throw new BusinessException(ExceptionStatus.OBJECT_DELETE_EXCEPTION);
        }
        upload(is, bucketName, objectName,fileSize);
    }

    private void upload(InputStream fileInputStream, String bucketName, String path, int objectSize) {
        //fileCover默认为true覆盖同名文件可节省网络开销和查询耗时，设置为false不能覆盖时判断文件是否存在
        if (!fileCover) {
            // 判断文件是否存在，禁止覆盖同名文件
            objectExist(path);
        }
        //限制文件大小在一个区间值
        log.info("file size is :{}",objectSize);
        if (objectSize > fileMaxStorage) {
            long overSize = fileMaxStorage/1024/1024;
            String msg = "上传的资源大小不能超出"+overSize+"M";
            throw new BusinessException(msg);
        }
        if (objectSize <= fileMinStorage) {
            String msg = "上传的资源大小不能低于"+fileMinStorage+"个字节";
            throw new BusinessException(msg);
        }
        log.info("the object path of the save to server is :{}",path);
        try {
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .stream(fileInputStream, objectSize, -1)
                    .bucket(bucketName)
                    .object(path).build();
            // 使用putObject上传一个文件到存储桶中。
            minioClient.putObject(putObjectArgs);
        } catch (Exception e) {
            log.error("upload file exception :", e);
            throw new BusinessException(ExceptionStatus.FILE_UPLOAD_EXCEPTION);
        }
    }

    /**
     * 校验MinIo对象是否存在
     *
     * @param objectName 文件存储路径
     * @author xuqq
     * @date 202-12-28
     */
    private void objectExist(String objectName) {
        // 判断文件是否存在
        StatObjectArgs statObjectArgs = StatObjectArgs.builder().bucket(bucketName).object(objectName).build();
        try {
            minioClient.statObject(statObjectArgs);
        } catch (ConnectException e) {
            //查询对象必须存在
            log.error("client connection is abnormal", e);
            throw new BusinessException(ExceptionStatus.OSS_CLIENT_EXCEPTION);
        } catch (Exception e) {
            //查询对象必须存在
            log.error("get resource exception", e);
            throw new BusinessException(ExceptionStatus.RESOURCES_NOT_FOUND_EXCEPTION);
        }
    }

}
