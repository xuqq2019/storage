package com.xqq.oss.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import io.minio.*;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Service
public class FileUploader {
   @Resource
   private MinioClient minioClient;

    /**
     * 上传文件
     * @param file 文件流
     * @return String 提示信息
     */
    public String upload(MultipartFile file,String minioBucketName) throws NoSuchAlgorithmException, IOException, InvalidKeyException{
        try {
            BucketExistsArgs bucketExistsArgs = BucketExistsArgs.builder().bucket(minioBucketName).build();
            // 检查存储桶是否已经存在
            boolean isExist = minioClient.bucketExists(bucketExistsArgs);
            if(!isExist) {
                log.info("start create Bucket");
                // 创建一个名为asiatrip的存储桶，用于存储照片的zip文件。
                MakeBucketArgs makeBucketArgs = MakeBucketArgs.builder().bucket(minioBucketName).build();
                minioClient.makeBucket(makeBucketArgs);
            }
            String fileName = file.getOriginalFilename();
            if(StringUtils.isEmpty(fileName)){
                return "文件名为空，请检查上传文件";
            }else {
                //服务做分布式可考虑雪花算法
                fileName= LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE)+fileName.trim();
            }
            // 检查一下将要存储的文件名称有没有被占用，防止旧文件被覆盖，不能保证数据的唯一性的时候用
            try {
                GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket(minioBucketName).object(fileName).build();
                minioClient.getObject(getObjectArgs);
                return file.getOriginalFilename()+"已存在";
            } catch (Exception e) {
                log.info("待上传的文件信息阅览地址>>>"+minioClient.getObjectUrl(minioBucketName,fileName));
            }
            InputStream fileInputStream = file.getInputStream();
            long objectSize = fileInputStream.available();
            long partSize = fileInputStream.available();
            if (objectSize >= 0L) {
                if (objectSize < 5242880L) {
                    partSize=5242880L;
                }
            } else{
                return "文件不能为空";
            }
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .stream(fileInputStream,objectSize,partSize)
                    .bucket(minioBucketName)
                    .object(fileName).build();
            // 使用putObject上传一个文件到存储桶中。
            minioClient.putObject(putObjectArgs);
            return fileName+"文件上传成功";
        } catch(MinioException e) {
            log.error("上传文件异常:",e);
            return "上传文件异常";
        }
    }
    /**
     * 下载文件
     * @param objectName 文件名
     * @param bucketName 桶名（文件夹）
     * @param response 返回体
     */
    public void downloadFile(String objectName,String bucketName,HttpServletResponse response) {
        try {
            GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket(bucketName).object(objectName).build();
            InputStream file = minioClient.getObject(getObjectArgs);
            String filename = new String(objectName.getBytes("ISO8859-1"), StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition", "attachment;filename=" + filename);
            ServletOutputStream servletOutputStream = response.getOutputStream();
            int len;
            byte[] buffer = new byte[1024];
            while((len=file.read(buffer)) > 0){
                servletOutputStream.write(buffer, 0, len);
            }
            servletOutputStream.flush();
            file.close();
            servletOutputStream.close();
        } catch (Exception e) {
            log.error("下载异常",e);
        }
    }


    /**
     * 获取文件流
     * @param objectName 文件名
     * @param bucketName 桶名（文件夹）
     */
    public void getFileInputStream(String objectName, String bucketName,HttpServletResponse response) {
        log.info("预览文件开始");
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket(bucketName).object(objectName).build();
            inputStream = minioClient.getObject(getObjectArgs);
            if(inputStream==null){
                return;
            }
            outputStream = response.getOutputStream();
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }
            response.flushBuffer();
        } catch (Exception e) {
            log.error("预览文件失败",e);
        }  finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("minio输入流关闭异常", e);
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    log.error("minio输出流关闭异常", e);
                }
            }
            log.info("预览文件结束");
        }
    }
}

