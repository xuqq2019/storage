package com.xqq.oss.controller;

import com.xqq.oss.core.model.Result;
import com.xqq.oss.core.exception.BusinessException;
import com.xqq.oss.core.warn.ExceptionStatus;
import com.xqq.oss.service.FileManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 文件管理服务
 * @author xuqq
 * @version v1.0
 * @date 2020-10-13
 */
@RestController
@RequestMapping("/oss")
@Slf4j
public class FileManagerController {

    @Resource
    private FileManagerService fileManagerService;

    /**
     * 创建存储桶
     * @return Result
     * @author xuqq
     * @date 2020-10-13
     */
    @PostMapping("/bucket")
    public Result<Object> create() {
        return fileManagerService.createBucket();
    }

    /**
     * 文件上传
     * @param webFile 上传文件到OSS时需要指定包含文件后缀在内的完整路径，例如abc/efg/123.jpg。
     * @return Result
     * @author xuqq
     * @date 2020-10-05
     */
    @PostMapping("/upload")
    public Result<Object> upload(@RequestParam(value = "file",required = false) MultipartFile webFile,@RequestParam(value = "apk",required = false) MultipartFile appFile) {
        if(webFile==null){
            return fileManagerService.uploadFile(appFile);
        }else if(appFile==null){
            return fileManagerService.uploadFile(webFile);
        }else {
            throw new BusinessException(ExceptionStatus.PARAM_PARSE_EXCEPTION);
        }
    }

    /**
     * 文件下载
     * @param path 上传文件到OSS时需要指定包含文件后缀在内的完整路径，例如abc/efg/123.jpg
     * @author xuqq
     * @date 2020-10-05
     */
    @GetMapping("/download")
    public void download(String path) {
        fileManagerService.downloadFile(path);
    }

    /**
     * 文件下载
     * @param list 上传文件到OSS时需要指定包含文件后缀在内的完整路径，例如abc/efg/123.jpg
     * @author xuqq
     * @date 2020-10-05
     */
    @PostMapping("/download/batch")
    public void downloadBatch(@RequestBody Set<String> list) {
        fileManagerService.downloadBatch(list);
    }

    /**
     * 文件预览
     * @param path 上传文件到OSS时需要指定包含文件后缀在内的完整路径，例如abc/efg/123.jpg
     * @author xuqq
     * @date 2020-10-05
     */
    @GetMapping("/preview")
    public void preview(String path, HttpServletRequest request) {
        // 有range的话
        String rangeString = request.getHeader("Range");
        if (rangeString != null && rangeString.contains("bytes=") && rangeString.contains("-")) {
            fileManagerService.previewVideo(path,rangeString);
        }else {
            fileManagerService.previewFile(path);
        }
    }

    /**
     * 文件删除
     * @param path 上传文件到OSS时需要指定包含文件后缀在内的完整路径，例如abc/efg/123.jpg
     * @author xuqq
     * @date 2020-10-05
     */
    @DeleteMapping("/clear")
    public Result<Object> delete(String path) {
        return fileManagerService.delete(path);
    }

    /**
     * 检查文件是否存在
     * @param path 上传文件到OSS时需要指定包含文件后缀在内的完整路径，例如abc/efg/123.jpg
     * @author xuqq
     * @date 2020-10-05
     */
    @GetMapping("/check")
    public Result<Object> check(String path) {
        return fileManagerService.check(path);
    }

    /**
     * 文件夹目录预览
     * @author xuqq
     * @date 2020-10-05
     */
    @GetMapping("/folder/files")
    public Result<List<Map<String, Object>>> queryObject() {
        return fileManagerService.queryFileFromFolder();
    }

}
