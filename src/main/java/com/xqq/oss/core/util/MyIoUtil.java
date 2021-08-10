package com.xqq.oss.core.util;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipOutputStream;

/**
 * io操作工具类
 * @author xuqq
 * @date 2020-12-28
 * @version v1.0
 */
@Slf4j
public class MyIoUtil {

    public static void safeCloseStream(ServletOutputStream outputStream, InputStream inputStream) {
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                log.error("文件资源输出流关闭异常", e);
            }
        }
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                log.error("文件资源输入流关闭异常", e);
            }
        }
    }

    public static void safeCloseStream(DataOutputStream dataOutputStream, ZipOutputStream zipOutputStream) {
        if (dataOutputStream != null) {
            try {
                dataOutputStream.close();
            } catch (IOException e) {
                log.error("文件资源输出流关闭异常", e);
            }
        }
        if (zipOutputStream != null) {
            try {
                zipOutputStream.close();
            } catch (IOException e) {
                log.error("文件资源zip压缩流关闭异常", e);
            }
        }
    }

    public static void safeCloseStream(ByteArrayOutputStream outputStream, InputStream inputStream) {
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                log.error("文件资源输出流关闭异常", e);
            }
        }
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                log.error("文件资源输入流关闭异常", e);
            }
        }
    }
}
