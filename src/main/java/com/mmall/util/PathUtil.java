package com.mmall.util;

/**
 * Created by Administrator on 2017/12/13.
 */
public class PathUtil {

    private static final String FTP_SERVER = PropertiesUtil.getProperty("ftp.server.http.prefix");
    private static final String IMG_PATH = PropertiesUtil.getProperty("upload_image_path");
    public static String getFTPImgPath(String fileName){
        return FTP_SERVER+IMG_PATH+"/"+fileName;
    }
}
