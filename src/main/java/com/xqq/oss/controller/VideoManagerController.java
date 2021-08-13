package com.xqq.oss.controller;

import com.xqq.oss.core.model.Result;
import com.xqq.oss.service.FileManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 视频管理服务
 * @author xuqq
 * @version v1.0
 * @date 2020-10-13
 */
@RestController
@RequestMapping("/oss/video")
@Slf4j
public class VideoManagerController {

    @Resource
    private FileManagerService fileManagerService;

    /**
     * 视频上传
     * @param webFile 上传视频到OSS时需要指定包含视频后缀在内的完整路径，例如abc/efg/123.jpg。
     * @return Result
     * @author xuqq
     * @date 2020-10-05
     */
    @PostMapping
    public Result<Object> upload(@RequestParam(value = "file",required = false) MultipartFile webFile) {
        return fileManagerService.uploadFile(webFile);
    }

    /**
     * 视频下载
     * @param path 上传视频到OSS时需要指定包含视频后缀在内的完整路径，例如abc/efg/123.jpg
     * @author xuqq
     * @date 2020-10-05
     */
    @GetMapping("/download")
    public void download(String path) {
        fileManagerService.downloadFile(path);
    }

    /**
     * 视频播放
     * @param path 上传视频到OSS时需要指定包含视频后缀在内的完整路径，例如abc/efg/123.jpg
     * @author xuqq
     * @date 2020-10-05
     */
    @GetMapping("/preview")
    public void preview(String path) {
        fileManagerService.previewVideo(path);

    }

    /**
     * 视频删除
     * @param path 上传视频到OSS时需要指定包含视频后缀在内的完整路径，例如abc/efg/123.jpg
     * @author xuqq
     * @date 2020-10-05
     */
    @DeleteMapping("/clear")
    public Result<Object> delete(String path) {
        return fileManagerService.delete(path);
    }

    /**
     * 检查视频是否存在
     * @param path 上传视频到OSS时需要指定包含视频后缀在内的完整路径，例如abc/efg/123.jpg
     * @author xuqq
     * @date 2020-10-05
     */
    @GetMapping("/check")
    public Result<Object> check(String path) {
        return fileManagerService.check(path);
    }

    /**
     * 视频夹目录预览
     * @author xuqq
     * @date 2020-10-05
     */
    @GetMapping("/folder/files")
    public Result<List<Map<String, Object>>> queryObject() {
        return fileManagerService.queryFileFromFolder();
    }

}
