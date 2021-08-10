package com.xqq.oss.controller;

import com.xqq.oss.core.model.Result;
import com.xqq.oss.core.model.Base64Entity;
import com.xqq.oss.service.FileManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * 图片管理服务
 *
 * @author xuqq
 * @version v1.0
 * @date 2020-10-13
 */
@RestController
@RequestMapping("oss-youo")
@Slf4j
public class ImageManagerController {

    @Resource
    private FileManagerService fileManagerProduce;

    /**
     * base64格式图片上传
     *
     * @param base64 上传图片的base64字符
     * @return Result
     * @author xuqq
     * @date 2020-10-05
     */
    @PostMapping("/image/upload")
    public Result imageUpload(@Validated @RequestBody Base64Entity base64) {
        return fileManagerProduce.imageUpload(base64.getBase64());
    }

    /**
     * base64格式图片批量上传
     *
     * @param list 批量上传图片的base64字符集合
     * @return Result
     * @author xuqq
     * @date 2020-10-05
     */
    @PostMapping("/image/batch/upload")
    public Result imageBatchUpload(@RequestBody List<String> list) {
        return fileManagerProduce.imageBatchUpload(list);
    }

    /**
     * 图片预览
     *
     * @param path 上传图片件到OSS时需要指定包含文件后缀在内的完整路径，例如abc/efg/123.jpg
     * @author xuqq
     * @date 2020-10-05
     */
    @GetMapping("/image/preview")
    public Result previewImage(String path) throws IOException {
        return fileManagerProduce.previewImage(path);
    }

}
