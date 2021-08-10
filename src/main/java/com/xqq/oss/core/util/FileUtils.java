package com.xqq.oss.core.util;

import com.xqq.oss.core.exception.BusinessException;
import com.xqq.oss.core.warn.ExceptionStatus;
import com.xqq.oss.core.model.FileTypeEnum;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件工具类
 * @author xuqq
 * @date 2020-10-13
 * @version v1.0
 */
public class FileUtils {

    /**
     * 获取文件名
     * @param url url
     * @return 文件后缀
     */
    public static String fileNameFromUrl(String url) {
        String nonPramStr = url.substring(0, url.contains("?") ? url.indexOf("?") : url.length());
        return nonPramStr.substring(nonPramStr.lastIndexOf("-") + 1);
    }

    /**
     * 获取文件名
     * @param path url
     * @return 文件后缀
     */
    public static String fileNameFromPath(String path) {
        return path.substring(path.indexOf("-") + 1);
    }

    /**
     * 获取文件前缀
     * @param fileName 文件名
     * @return 文件前缀
     */
    public static String prefixFromFileName(String fileName) {
        return fileName.substring(0,fileName.lastIndexOf("."));
    }

    /**
     * 获取文件前缀
     * @param fileName 文件名
     * @return 文件前缀
     */
    public static String pdfNameFromFileName(String fileName) {
        return fileName.substring(0,fileName.lastIndexOf("."))+".pdf";
    }

    /**
     * 获取文件后缀名
     * @param fileName 文件名
     * @return 文件后缀
     */
    public static String suffixNameFromFileName(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    /**
     * 获取文件后缀
     * @param fileName 文件名
     * @return 文件后缀
     */
    public static String suffixFromFileName(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }

    /**
     * @param fileName 文件名
     * @return java.lang.String
     * @author xuqq
     * @date 2020-10-13
     * 生成随机的文件名
     */
    public static String generatorRandomFileName(String fileName) {
        return LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE) +"/"+ UUID.randomUUID().toString().replace("-","")+"-"+fileName;
    }

    /**
     * @param fileName 文件名
     * @return java.lang.String
     * @author xuqq0
     * @date 2020-10-13
     * 按日期存储生成文件名
     */
    public static String generatorFileName(String fileName) {
        return LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE) +"/"+ fileName;
    }

    /**
     * 生成随机的文件名
     * @return java.lang.String
     * @author xuqq
     * @date 2020-10-13
     */
    public static String generatorRandomFileName() {
        return LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE) +"/"+ UUID.randomUUID().toString().replace("-","");
    }

    /**
     * 生成随机的文件名
     * @return java.lang.String
     * @author xuqq
     * @date 2020-10-13
     */
    public static String generatorRandomImageName(String imageType) {
        return LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE) +"/"+ UUID.randomUUID().toString().replace("-","")+"."+imageType;
    }

    /**
     * 校验待处理的文件类型是否支持
     * @author xuqq
     * @date 2020-10-28
     */
    public static void validateFileType(String fileType) {
        boolean include = false;
        //遍历枚举类于文件类型匹配
        for (FileTypeEnum e : FileTypeEnum.values()) {
            if (e.getValue().equals(fileType)) {
                include = true;
                break;
            }
        }
        String typeArray=".mp4,.m4p,.mpg,.mpeg,.dat,.3gp,.mov,.rm,.ram,.rmvb,.wmv,.asf,.avi,.asx,.mkv.wav,.wma,.ra,.ogg,.mpc,.m4a,.aac,.mpa,.mp2,.m1a,.m2a,.mp3,.mid,.midi,.rmi,.mka,.ac3,.dts,.cda,.au,.snd,.aif,.aifc,.aiff";
        if(typeArray.contains(fileType)){
            include = true;
        }
        //遍历枚举类都找不到该类型则提示不支持该文件类型
        if (!include) {
            System.out.println("file type is {}" + fileType);
            throw new BusinessException(ExceptionStatus.FILE_TYPE_NOT_SUPPORT);
        }
    }
}
