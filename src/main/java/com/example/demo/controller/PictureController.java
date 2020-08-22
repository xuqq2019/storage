package com.example.demo.controller;

import com.example.demo.util.FileUploader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/v1/picture")
@Slf4j
public class PictureController {
    @Value("${minio.pic.bucket.name}")
    private String minioBucketName;
    @Resource
    private FileUploader fileUploader;

    @GetMapping("/upload")
    public Object upload(@RequestParam("file") MultipartFile file){
        try {
            return fileUploader.upload(file,minioBucketName);
        } catch (Exception e) {
            return "上传文件处理异常";
        }
    }

    @GetMapping("/download/{fileName}")
    public void download(@PathVariable String fileName,HttpServletResponse response){
        fileUploader.downloadFile(fileName,minioBucketName,response);
    }

    @GetMapping("/preview/{fileName}")
    public void preview(@PathVariable String fileName, HttpServletResponse response){
        fileUploader.getFileInputStream(fileName,minioBucketName,response);
    }
}
