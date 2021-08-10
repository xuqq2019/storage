package com.xqq.oss.service.impl;

import com.aliyun.oss.*;
import com.aliyun.oss.internal.OSSHeaders;
import com.aliyun.oss.model.*;
import com.xqq.oss.listen.PutObjectProgressListener;
import com.xqq.oss.core.exception.BusinessException;
import com.xqq.oss.core.warn.ExceptionStatus;
import com.xqq.oss.service.ObjectStorageSystemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件管理服务实现类
 *
 * @author xuqq
 * @version v1.0
 * @date 2020-10-13
 */
@Slf4j
public class AliCloudServiceImpl implements ObjectStorageSystemService {
    @Resource
    private OSS objectStorageServiceClient;
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
            if (!objectStorageServiceClient.doesBucketExist(bucketName)) {
                // 创建存储空间
                objectStorageServiceClient.createBucket(bucketName);
            }
        } catch (OSSException e) {
            log.error("oss exception", e);
            throw new BusinessException(ExceptionStatus.BUCKET_CREATE_EXCEPTION);
        } catch (ClientException e) {
            log.error("client exception :", e);
            throw new BusinessException(ExceptionStatus.BUCKET_CREATE_EXCEPTION);
        }
    }

    /**
     * 文件上传
     *
     * @param inputStream 存储文件
     * @param filePath    上传文件到OSS时需要指定包含文件后缀在内的完整路径，例如abc/efg/123.jpg。
     * @author xuqq
     * @date 2020-10-05
     */
    @Override
    public void upload(String filePath, InputStream inputStream) {
        upload(inputStream, bucketName, filePath);
    }

    /**
     * 文件列表查询
     *
     * @author xuqq
     * @date 2020-10-05
     */
    @Override
    public List<Map<String, Object>> queryFileFromFolder() {
        // objectStorageServiceClient.listObjects返回ObjectListing实例，包含此次listObject请求的返回结果。
        ObjectListing objectListing = objectStorageServiceClient.listObjects(bucketName);
        //objectListing.getObjectSummaries获取所有文件的描述信息
        List<Map<String, Object>> list = new ArrayList<>();
        for (OSSObjectSummary item : objectListing.getObjectSummaries()) {
            Map<String, Object> map = new HashMap<>(2);
            log.info(" - {} (size = {})",item.getKey(),item.getSize());
            map.put("name", item.getKey());
            map.put("size", item.getSize());
            list.add(map);
        }
        return list;
    }

    /**
     * 图片预览
     *
     * @param path 上传文件到OSS时需要指定包含文件后缀在内的完整路径，例如abc/efg/123.jpg
     * @author xuqq
     * @date 2020-10-05
     */
    @Override
    public InputStream getObject(String path) {
        log.info("待预览的图片路径:{}",path);
        //判断文件是否存在
        objectExist(path);
        try {
            // 调用objectStorageServiceClient.getObject返回一个OSSObject实例，该实例包含文件内容及文件元信息。
            OSSObject ossObject = objectStorageServiceClient.getObject(bucketName, path);
            // 调用ossObject.getObjectContent获取文件输入流，可读取此输入流获取其内容。
            return ossObject.getObjectContent();
        } catch (Exception e) {
            log.error("get resource exception",e);
            throw new BusinessException(ExceptionStatus.OBJECT_GET_EXCEPTION);
        }
    }

    @Override
    public InputStream getObject(String path, Long offset, Long length) {
        log.info("待预览的资源路径:{}",path);
        //判断文件是否存在
        objectExist(path);
        try {
            // 调用objectStorageServiceClient.getObject返回一个OSSObject实例，该实例包含文件内容及文件元信息。
            GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, path);
            getObjectRequest.setRange(offset,offset+length-1);
            getObjectRequest.addHeader("x-oss-range-behavior", "standard");
            OSSObject ossObject = objectStorageServiceClient.getObject(getObjectRequest);
            // 调用ossObject.getObjectContent获取文件输入流，可读取此输入流获取其内容。
            return ossObject.getObjectContent();
        } catch (Exception e) {
            log.error("get resource exception",e);
            throw new BusinessException(ExceptionStatus.OBJECT_GET_EXCEPTION);
        }
    }

    @Override
    public Long getContentLength(String path) {
        log.info("待预览的资源路径:{}",path);
        //判断文件是否存在
        objectExist(path);
        try {
            // 调用objectStorageServiceClient.getObject返回一个OSSObject实例，该实例包含文件内容及文件元信息。
            SimplifiedObjectMeta objectMeta = objectStorageServiceClient.getSimplifiedObjectMeta(bucketName, path);
            // 调用ossObject.getObjectContent获取文件输入流，可读取此输入流获取其内容。
            return objectMeta.getSize();
        } catch (Exception e) {
            log.error("get resource contentLength exception",e);
            throw new BusinessException(ExceptionStatus.OBJECT_GET_EXCEPTION);
        }
    }

    @Override
    public void delete(String path) {
        //文件不存在时则抛出异常提示没有该文件
        objectExist(path);
        try {
            // 调用objectStorageServiceClient.getObject返回一个OSSObject实例，该实例包含文件内容及文件元信息。
            objectStorageServiceClient.deleteObject(bucketName,path);
        } catch (Exception e) {
            log.error("delete resource exception",e);
            throw new BusinessException(ExceptionStatus.OBJECT_DELETE_EXCEPTION);
        }
    }

    @Override
    public void check(String path) {
        objectExist(path);
    }

    private void upload(InputStream inputStream, String bucketName, String fileName){
        //fileCover默认为true覆盖同名文件可节省网络开销和查询耗时，设置为false不能覆盖时判断文件是否存在
        if (!fileCover) {
            // 判断文件是否存在，禁止覆盖同名文件
            boolean flag = objectStorageServiceClient.doesObjectExist(bucketName, fileName);
            if (flag) {
                throw new BusinessException(ExceptionStatus.FILE_ALREADY_EXISTS);
            }
        }
        //获取文件空间大小
        int objectSize;
        try {
            objectSize = inputStream.available();
        } catch (IOException e) {
            throw new BusinessException(ExceptionStatus.FILE_SIZE_EXCEPTION);
        }
        //限制文件大小在一个区间值
        log.info("file size is : {}",objectSize);
        if (objectSize > fileMaxStorage) {
            String msg = "上传的资源大小不能超出"+(fileMaxStorage/1024/1024)+"M";
            throw new BusinessException(msg);
        }
        if (objectSize <= fileMinStorage) {
            String msg = "上传的资源大小不能低于"+fileMinStorage+"个字节";
            throw new BusinessException(msg);
        }
        log.info("生成存储到服务器上的文件存储路径:" + fileName);
        objectStorageServiceClient.putObject(new PutObjectRequest(bucketName, fileName, inputStream).withProgressListener(new PutObjectProgressListener()));
    }

    @Override
    public void sliceUpload(String objectName, MultipartFile file,long fileSliceSize) {
        try {
            // 创建InitiateMultipartUploadRequest对象。
            InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, objectName);
            // 如果需要在初始化分片时设置文件存储类型，请参考以下示例代码。
             ObjectMetadata metadata = new ObjectMetadata();
             metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());
             request.setObjectMetadata(metadata);
            // 初始化分片。
            InitiateMultipartUploadResult upResult = objectStorageServiceClient.initiateMultipartUpload(request);
            // 返回uploadId，它是分片上传事件的唯一标识，您可以根据这个uploadId发起相关的操作，如取消分片上传、查询分片上传等。
            String uploadId = upResult.getUploadId();
            // partETags是PartETag的集合。PartETag由分片的ETag和分片号组成。
            List<PartETag> partETags = new ArrayList<>();
            // 计算文件有多少个分片。
            long fileLength = file.getSize();
            int partCount = (int) (fileLength / fileSliceSize);
            if (fileLength % fileSliceSize != 0) {
                partCount++;
            }
            // 遍历分片上传。
            for (int i = 0; i < partCount; i++) {
                long startPos = i * fileSliceSize;
                long curPartSize = (i + 1 == partCount) ? (fileLength - startPos) : fileSliceSize;
                log.info("分片上传第{}次,当前上传分片尺寸是{},共有多少{}个分片,起始位置{}-{}",i,fileSliceSize,partCount,startPos,curPartSize);
                InputStream inputStream =file.getInputStream();
                // 跳过已经上传的分片。
                long skipIndex = inputStream.skip(startPos);
                log.info("分片上传第{}次,跳过已经上传的分片{}",i,skipIndex);
                UploadPartRequest uploadPartRequest = new UploadPartRequest();
                uploadPartRequest.setBucketName(bucketName);
                uploadPartRequest.setKey(objectName);
                uploadPartRequest.setUploadId(uploadId);
                uploadPartRequest.setInputStream(inputStream);
                // 设置分片大小。除了最后一个分片没有大小限制，其他的分片最小为100 KB。
                uploadPartRequest.setPartSize(curPartSize);
                // 设置分片号。每一个上传的分片都有一个分片号，取值范围是1~10000，如果超出这个范围，OSS将返回InvalidArgument的错误码。
                uploadPartRequest.setPartNumber(i + 1);
                // 每个分片不需要按顺序上传，甚至可以在不同客户端上传，OSS会按照分片号排序组成完整的文件。
                UploadPartResult uploadPartResult = objectStorageServiceClient.uploadPart(uploadPartRequest);
                // 每次上传分片之后，OSS的返回结果包含PartETag。PartETag将被保存在partETags中。
                partETags.add(uploadPartResult.getPartETag());
            }
            // 创建CompleteMultipartUploadRequest对象。
            // 在执行完成分片上传操作时，需要提供所有有效的partETags。OSS收到提交的partETags后，会逐一验证每个分片的有效性。当所有的数据分片验证通过后，OSS将把这些分片组合成一个完整的文件。
            CompleteMultipartUploadRequest completeMultipartUploadRequest =
                    new CompleteMultipartUploadRequest(bucketName, objectName, uploadId, partETags);
            // 如果需要在完成文件上传的同时设置文件访问权限，请参考以下示例代码。
            // completeMultipartUploadRequest.setObjectACL(CannedAccessControlList.PublicRead);
            // 完成上传。
            CompleteMultipartUploadResult completeMultipartUploadResult = objectStorageServiceClient.completeMultipartUpload(completeMultipartUploadRequest);
            log.info("complete multipart upload result {}",completeMultipartUploadResult.getKey());
        } catch (IOException e) {
            log.error("complete multipart upload exception",e);
        }
    }

    /**
     * 校验对象是否存在
     *
     * @param path  文件存储路径
     * @author xuqq
     * @date 202-12-28
     */
    private void objectExist(String path) {
        if(!objectStorageServiceClient.doesObjectExist(bucketName, path)){
            throw new BusinessException(ExceptionStatus.RESOURCES_NOT_FOUND_EXCEPTION);
        }
    }

}
