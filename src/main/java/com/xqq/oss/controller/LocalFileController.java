package com.xqq.oss.controller;

import com.xqq.oss.core.exception.BusinessException;
import com.xqq.oss.core.warn.ExceptionStatus;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

@Controller
@RequestMapping(value = "/oss/local")
public class LocalFileController {

    @Value("${video.local.storage:D:\\}")
    private String videoStoragePath;

    @RequestMapping("/preview")
    public void home1(String fileName, HttpServletResponse response, HttpServletRequest request) {
        long fileLength;
        try {
            // 有range的话
            String rangeString = request.getHeader("Range");
            if (rangeString != null && rangeString.contains("bytes=") && rangeString.contains("-")) {
                // 坑爹地方一：http状态码要为206
                //获取从那个字节开始读取文件
                long range = 0L;
                if (!StringUtils.isEmpty(rangeString)) {
                    range = Long.parseLong(rangeString.substring(rangeString.indexOf("=") + 1, rangeString.indexOf("-")));
                }
                String filePath = videoStoragePath+fileName;
                File file = new File(filePath);
                fileLength = file.length();
                //获取响应的输出流
                String contentType = request.getServletContext().getMimeType(fileName);
                // 随机读文件
                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
                //获取响应的输出流
                OutputStream outputStream = response.getOutputStream();
                //设置内容类型
                response.setHeader("Content-Type", contentType);
                //返回码需要为206，代表只处理了部分请求，响应了部分数据
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                // 移动访问指针到指定位置
                randomAccessFile.seek(range);
                // 每次请求只返回1MB的视频流
                byte[] bytes = new byte[1024 * 1024];
                int len = randomAccessFile.read(bytes);
                //设置此次相应返回的数据长度
                response.setContentLength(len);
                //设置此次相应返回的数据范围
                response.setHeader("Content-Range", "bytes "+range+"-"+(fileLength-1)+"/"+fileLength);
                // 将这1MB的视频流响应给客户端
                outputStream.write(bytes, 0, len);
                outputStream.close();
                randomAccessFile.close();
                System.out.println("返回数据区间:【"+range+"-"+(range+len)+"】");
            } else {
                throw new BusinessException(ExceptionStatus.RANGE_EXCEPTION);
            }
        } catch (ClientAbortException e) {
            e.printStackTrace();
            System.out.println("用户停止下载：");
            //捕获此异常表示拥护停止下载
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(fileName + "预览失败");
            throw new BusinessException(ExceptionStatus.FILE_PREVIEW_EXCEPTION);
        }
    }
}
