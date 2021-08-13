package com.xqq.oss.service.impl;

import com.xqq.oss.core.model.Result;
import com.xqq.oss.core.exception.BusinessException;
import com.xqq.oss.core.util.ResultGenerator;
import com.xqq.oss.core.warn.ExceptionStatus;
import com.xqq.oss.core.model.ContentTypeEnum;
import com.xqq.oss.core.model.FileTypeEnum;
import com.xqq.oss.service.FileManagerService;
import com.xqq.oss.service.ObjectStorageSystemService;
import com.xqq.oss.core.util.FileUtils;
import com.xqq.oss.core.util.MyIoUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 文件管理服务实现类
 *
 * @author xuqq
 * @version v1.0
 * @date 2020-10-13
 */
@Slf4j
@Service
public class FileManagerServiceImpl implements FileManagerService {
    @Resource
    private ExecutorService executorService;
    @Resource
    private ObjectStorageSystemService ossProduce;
    @Value("${oss.video.max-size:10485760}")
    private long fileSliceSize;


    /**
     * 创建存储桶
     *
     * @return Result
     * @author xuqq
     * @date 2020-10-13
     */
    @Override
    public Result<Object> createBucket() {
        //调用实例创建存储桶
        ossProduce.createBucket();
        return ResultGenerator.success();
    }

    /**
     * 文件上传
     *
     * @param file 上传文件到OSS时需要指定包含文件后缀在内的完整路径，例如abc/efg/123.jpg。
     * @return Result
     * @author xuqq
     * @date 2020-10-05
     */
    @Override
    public Result<Object> uploadFile(MultipartFile file) {
        try {
            // 上传文件到指定的存储空间（bucketName）并将其保存为指定的文件名称（objectName）
            String fileName = file.getOriginalFilename();
            String path;
            if (StringUtils.isEmpty(fileName)) {
                throw new BusinessException(ExceptionStatus.FILE_NAME_NOT_EXISTS);
            } else {
                //服务做分布式可考虑雪花算法
                path = FileUtils.generatorRandomFileName(fileName);
            }
            String fileSuffix = FileUtils.suffixNameFromFileName(fileName);
            //校验文件类型是否为支持类型
            FileUtils.validateFileType(fileSuffix);
            long size = file.getSize();
            if(size>fileSliceSize){
                ossProduce.sliceUpload(path,file,fileSliceSize);
            }else {
                //获取文件流
                InputStream fileInputStream = file.getInputStream();
                ossProduce.upload(path, fileInputStream);
            }
            Map<String, Object> map = new HashMap<>(2);
            map.put("path", path);
            map.put("size", size);
            return ResultGenerator.success(map);
        } catch (IOException e) {
            log.error("client exception :", e);
            throw new BusinessException(ExceptionStatus.FILE_UPLOAD_EXCEPTION);
        }
    }

    /**
     * 文件下载
     *
     * @param path 上传文件到OSS时需要指定包含文件后缀在内的完整路径，例如abc/efg/123.jpg
     * @author xuqq
     * @date 2020-10-05
     */
    @Override
    public void downloadFile(String path) {
        ServletOutputStream servletOutputStream = null;
        InputStream inputStream = null;
        // 调用objectStorageServiceClient.getObject返回一个OSSObject实例，该实例包含文件内容及文件元信息。
        try {
            HttpServletResponse response = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getResponse();
            assert response != null;
            //校验对象是否存在，不存在返回提示
            inputStream = ossProduce.getObject(path);
            String fileName = FileUtils.fileNameFromPath(path);
            log.info("download file name :{}",fileName);
            //文件名必须转码，否者下载后文件名中有中文时会乱码
            fileName = new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            servletOutputStream = response.getOutputStream();
            int len;
            byte[] buffer = new byte[1024];
            while ((len = inputStream.read(buffer)) > 0) {
                servletOutputStream.write(buffer, 0, len);
            }
            servletOutputStream.flush();
        } catch (BusinessException businessException) {
            log.error("{}文件下载失败",path);
            throw businessException;
        } catch (Exception e) {
            log.error("{}文件下载失败",path,e);
            throw new BusinessException(ExceptionStatus.FILE_DOWNLOAD_EXCEPTION);
        } finally {
            // 数据读取完成后，获取的流必须关闭，否则会造成连接泄漏，导致请求无连接可用，程序无法正常工作。
            MyIoUtil.safeCloseStream(servletOutputStream, inputStream);
        }
    }

    /**
     * 文件批量下载
     *
     * @param list 上传文件到OSS时需要指定包含文件后缀在内的完整路径，例如abc/efg/123.jpg
     * @author xuqq
     * @date 2020-10-05
     */
    @Override
    public void downloadBatch(Set<String> list) {
        HttpServletResponse response = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getResponse();
        assert response != null;
        //文件名必须转码，否者下载后文件名中有中文时会乱码
        response.setHeader("Content-Disposition", "attachment;filename=" + new String("file.zip".getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
        response.setHeader("Content-Type", "application/zip");
        try {
            if (list == null || list.size() < 1) {
                throw new BusinessException(ExceptionStatus.PARAM_PARSE_EXCEPTION);
            }
            int listMaxSize = 10;
            if (list.size() > listMaxSize) {
                throw new BusinessException(ExceptionStatus.PARAM_SIZE_EXCEPTION);
            }
            Set<String> setFileName = new HashSet<>(list.size());
            ServletOutputStream outputStream = response.getOutputStream();
            ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(outputStream));
            DataOutputStream dataOutputStream = null;
            //设置压缩流，直接写入response，实现边压缩边下载
            zipOutputStream.setMethod(ZipOutputStream.DEFLATED);
            for (String path : list) {
                String fileName = FileUtils.fileNameFromPath(path);
                log.info("待下载的文件名称:{}",fileName);
                // 校验文件是否存在
                InputStream inputStream = ossProduce.getObject(path);
                //文件名称存在相同时需修改放入zip中的文件名
                if(setFileName.contains(fileName)){
                    fileName = path;
                }else {
                    //文件是否已处理过的依据
                    setFileName.add(fileName);
                }
                zipOutputStream.putNextEntry(new ZipEntry(fileName));
                dataOutputStream = new DataOutputStream(zipOutputStream);
                int len;
                byte[] buffer = new byte[1024];
                while ((len = inputStream.read(buffer)) > 0) {
                    dataOutputStream.write(buffer, 0, len);
                }
                dataOutputStream.flush();
                inputStream.close();
                zipOutputStream.closeEntry();
            }
            // 数据读取完成后，获取的流必须关闭，否则会造成连接泄漏，导致请求无连接可用，程序无法正常工作。
            // 此处不能用finally处理，否则会导致二次返回流信息
            MyIoUtil.safeCloseStream(dataOutputStream, zipOutputStream);
        } catch (BusinessException businessException) {
            log.error("文件批量下载失败");
            throw businessException;
        } catch (Exception e) {
            log.error("文件批量下载失败", e);
            throw new BusinessException(ExceptionStatus.FILE_BATCH_DOWNLOAD_EXCEPTION);
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
    public void previewFile(String path) {
        ServletOutputStream servletOutputStream = null;
        InputStream inputStream = null;
        try {
            HttpServletResponse response = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getResponse();
            assert response != null;
            log.info("待预览的文件:{}", path);
            //涉及到word类型的文档预览将请求路径转换为pdf的请求路径
            String filePath = path;
            String fileType = FileUtils.suffixNameFromFileName(path);
            if (FileTypeEnum.PDF.getValue().equals(fileType)) {
                response.setContentType(ContentTypeEnum.PDF.getValue());
            } else if (FileTypeEnum.DOC.getValue().equals(fileType)
                    || FileTypeEnum.DOCX.getValue().equals(fileType)
                    || FileTypeEnum.VISIO.getValue().equals(fileType)) {
                filePath = FileUtils.pdfNameFromFileName(path);
                response.setContentType(ContentTypeEnum.PDF.getValue());
            } else if (FileTypeEnum.JPG.getValue().equals(fileType) || FileTypeEnum.JPEG.getValue().equals(fileType)) {
                response.setContentType(ContentTypeEnum.JPG.getValue());
            } else if (FileTypeEnum.PNG.getValue().equals(fileType)) {
                response.setContentType(ContentTypeEnum.PNG.getValue());
            } else if (FileTypeEnum.XLS.getValue().equals(fileType) || FileTypeEnum.XLSX.getValue().equals(fileType)) {
                response.setContentType(ContentTypeEnum.XLS.getValue());
            } else {
                //校验文件类型是否为支持类型
                FileUtils.validateFileType(fileType);
                response.setContentType(ContentTypeEnum.MULTIPART.getValue());
            }
            inputStream = ossProduce.getObject(filePath);
            servletOutputStream = response.getOutputStream();
            int len;
            byte[] buffer = new byte[1024];
            while ((len = inputStream.read(buffer)) > 0) {
                servletOutputStream.write(buffer, 0, len);
            }
            servletOutputStream.flush();
        } catch (IOException e) {
            log.error("{}预览失败",path);
            throw new BusinessException(ExceptionStatus.FILE_PREVIEW_EXCEPTION);
        } finally {
            // 数据读取完成后，获取的流必须关闭，否则会造成连接泄漏，导致请求无连接可用，程序无法正常工作。
            MyIoUtil.safeCloseStream(servletOutputStream, inputStream);
        }
    }

    @Override
    public Result<Object> delete(String path) {
        //调用实例删除文件
        ossProduce.delete(path);
        return ResultGenerator.success();
    }

    @Override
    public Result<Object> asyncDeleteResourceList(Set<String> resources) {
        //调用实例批量异步删除文件
        CompletableFuture.supplyAsync(() -> deleteResourceList(resources));
        return ResultGenerator.success();
    }

    public Result<Object> deleteResourceList(Set<String> resources) {
        //调用实例批量删除文件
        for (String path : resources) {
            delete(path);
        }
        return ResultGenerator.success();
    }

    @Override
    public Result<Object> check(String path) {
        //调用实例删除文件
        ossProduce.check(path);
        return ResultGenerator.success();
    }

    /**
     * 文件列表查询
     *
     * @author xuqq
     * @date 2020-10-05
     */
    @Override
    public Result<List<Map<String, Object>>> queryFileFromFolder() {
        return ResultGenerator.success(ossProduce.queryFileFromFolder());
    }


    /**
     * 图片上传
     *
     * @param base64 上传图片的base64字符
     * @return Result
     * @author xuqq
     * @date 2020-10-05
     */
    @Override
    public Result<Map<String, Object>> imageUpload(String base64) {
        return ResultGenerator.success(imageTransform(base64));
    }

    /**
     * 图片上传
     *
     * @param list 批量上传图片的base64字符集合
     * @return Result
     * @author xuqq
     * @date 2020-10-05
     */
    @Override
    public Result<List<Map<String, Object>>> imageBatchUpload(List<String> list) {
        List<Map<String, Object>> completableFutures = new ArrayList<>();
        AtomicInteger count = new AtomicInteger();
        for (String base64 : list) {
            try {
                completableFutures.add(CompletableFuture.supplyAsync(() -> imageTransform(base64), executorService).get());
            } catch (InterruptedException | ExecutionException e) {
                log.error("异步执行base64图片批量上传异常", e);
                count.set(1);
                break;
            }
        }
        if (count.get() == 1) {
            throw new BusinessException(ExceptionStatus.IMAGE_BATCH_DOWNLOAD_EXCEPTION);
        }
        return ResultGenerator.success(completableFutures);
    }


    /**
     * 图片预览
     *
     * @param path 上传文件到OSS时需要指定包含文件后缀在内的完整路径，例如abc/efg/123.jpg
     * @author xuqq
     * @date 2020-10-05
     */
    @Override
    public Result<Object> previewImage(String path) {
        InputStream inputStream = null;
        ByteArrayOutputStream data = null;
        try {
            HttpServletResponse response = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getResponse();
            assert response != null;
            log.info("待预览的图片路径:" + path);
            //根据文件类型去设置请求头的返回类型
            String fileType = FileUtils.suffixNameFromFileName(path);
            if (FileTypeEnum.JPG.getValue().equals(fileType) || FileTypeEnum.JPEG.getValue().equals(fileType)) {
                response.setContentType(ContentTypeEnum.JPG.getValue());
            } else if (FileTypeEnum.PNG.getValue().equals(fileType)) {
                response.setContentType(ContentTypeEnum.PNG.getValue());
            } else {
                throw new BusinessException(ExceptionStatus.FILE_TYPE_NOT_SUPPORT);
            }
            inputStream = ossProduce.getObject(path);
            data = new ByteArrayOutputStream();
            int len;
            byte[] buffer = new byte[1024];
            while ((len = inputStream.read(buffer)) != -1) {
                data.write(buffer, 0, len);
            }
            // 对字节数组Base64编码
            BASE64Encoder encoder = new BASE64Encoder();
            // 返回Base64编码过的字节数组字符串
            String base64 = encoder.encode(data.toByteArray());
            //sun包解析的byte带换行符
            String line = System.lineSeparator();
            base64 = base64.replaceAll(line, "");
            log.debug(base64);
            String base = "data:image/png;base64," + base64;
            return ResultGenerator.success(base);
        } catch (IOException e) {
            log.error("预览失败", e);
            throw new BusinessException(ExceptionStatus.FILE_PREVIEW_EXCEPTION);
        } finally {
            // 数据读取完成后，获取的流必须关闭，否则会造成连接泄漏，导致请求无连接可用，程序无法正常工作。
            MyIoUtil.safeCloseStream(data, inputStream);
        }
    }

    private Map<String, Object> imageTransform(String base64) {
        InputStream byteArrayInputStream = null;
        try {
            String base64String = base64.replaceAll("\"", "");
            String imageType = "jpeg";
            Map<String, Object> map = new HashMap<>(2);
            String[] imageFormatArray = base64String.split(";base64,");
            if (imageFormatArray.length > 1) {
                String[] imageTypeArray = imageFormatArray[0].split("/");
                if (imageTypeArray.length > 1) {
                    imageType = imageTypeArray[1];
                    log.info("上传图片类型为:{}",imageType);
                    if (imageType.equals(FileTypeEnum.JPG.getValue())
                            || imageType.equals(FileTypeEnum.PNG.getValue())
                            || imageType.equals(FileTypeEnum.JPEG.getValue())) {
                        base64String = imageFormatArray[1];
                    } else {
                        //解析的文件类型不符合要求时提示不支持
                        throw new BusinessException(ExceptionStatus.FILE_TYPE_NOT_SUPPORT);
                    }
                } else {
                    //解析出现异常时抛出业务异常
                    throw new BusinessException(ExceptionStatus.PARAM_PARSE_EXCEPTION);
                }
            }
            log.debug("上传图片base64字符为:{}",base64String);
            // 对字节数组Base64编码
            BASE64Decoder decoder = new BASE64Decoder();
            // 返回Base64编码过的字节数组字符串
            byte[] bytes;
            try {
                bytes = decoder.decodeBuffer(base64String);
            } catch (IOException e) {
                throw new BusinessException(ExceptionStatus.IMAGE_BASE64_PARSE_EXCEPTION);
            }
            byteArrayInputStream = new ByteArrayInputStream(bytes);
            int size;
            try {
                size = byteArrayInputStream.available();
            } catch (IOException e) {
                throw new BusinessException(ExceptionStatus.FILE_SIZE_EXCEPTION);
            }
            map.put("size", size);
            //随机生成唯一的存储路径
            String filePath = FileUtils.generatorRandomImageName(imageType);
            // 上传文件到指定的存储空间（bucketName）并将其保存为指定的文件名称（objectName）
            ossProduce.upload(filePath, byteArrayInputStream);
            map.put("path", filePath);
            return map;
        } finally {
            if (byteArrayInputStream != null) {
                try {
                    byteArrayInputStream.close();
                } catch (IOException e) {
                    log.error("文件资源输入流关闭异常", e);
                }
            }
        }
    }


    @Override
    public void previewVideo(String path){
        log.info("加载视频分片播放");
        InputStream is;
        ServletOutputStream servletOutputStream;
        //文件名
        String fileName;
        long fileLength;
        try {
            HttpServletResponse response = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getResponse();
            assert response != null;
            HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
            // 有range的话
            String rangeString = request.getHeader("Range");
            // 坑爹地方一：http状态码要为206
                //获取从那个字节开始读取文件
                long range  = Long.parseLong(rangeString.substring(rangeString.indexOf("=") + 1, rangeString.indexOf("-")));
                //文件名
                fileName = FileUtils.fileNameFromPath(path);
                fileLength = ossProduce.getContentLength(path);
                is = ossProduce.getObject(path, range, (1024 * 1024 * 2L));
                //获取响应的输出流
                String contentType = request.getServletContext().getMimeType(fileName);
                //设置内容类型
                response.setHeader("Content-Type", contentType);
                //返回码需要为206，代表只处理了部分请求，响应了部分数据
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                //设置此次相应返回的数据范围
                response.setHeader("Content-Range", "bytes " + range + "-" + (fileLength - 1) + "/" + fileLength);
                servletOutputStream = response.getOutputStream();
                int len;
                byte[] buffer = new byte[1024];
                while ((len = is.read(buffer)) > 0) {
                    servletOutputStream.write(buffer, 0, len);
                }
                // 将这1MB的视频流响应给客户端
                servletOutputStream.flush();
                //设置此次相应返回的数据长度
                response.setContentLength(len);
                log.info("返回数据区间:[{}-{}]",range,(range + len));
        } catch (ClientAbortException e) {
            //捕获此异常表示拥护停止下载
            throw new BusinessException(ExceptionStatus.OSS_CLIENT_EXCEPTION);
        } catch (IOException e) {
            log.error("{}预览失败",path);
            throw new BusinessException(ExceptionStatus.FILE_PREVIEW_EXCEPTION);
        }
    }
}
