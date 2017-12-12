package com.mmall.util;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;

/**
 * Created by SMY on 2017/12/12.
 */
public class FTPUtil {

    private static Logger logger = LoggerFactory.getLogger(FTPUtil.class);
    private static String ftpIp = PropertiesUtil.getProperty("ftp.server.ip");
    private static String ftpUser = PropertiesUtil.getProperty("ftp.user");
    private static String ftpPass = PropertiesUtil.getProperty("ftp.pass");

    public static boolean upload(List<File> fileList) throws IOException {
        logger.info("文件开始上传");
        FTPUtil ftpUtil = new FTPUtil(ftpIp, ftpUser, 21, ftpPass);
        boolean result = ftpUtil.upload(PropertiesUtil.getProperty("upload_image_path"), fileList);
        logger.info("文件上传结束");
        return result;
    }

    private boolean upload(String remotePath, List<File> fileList) throws IOException {
        InputStream is = null;
        if (connec(this.ip, this.username, this.pwd)) {
            try {
                ftpClient.changeWorkingDirectory(remotePath);
                ftpClient.setBufferSize(1024);
                ftpClient.setControlEncoding("UTF-8");
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                for (File fileItem : fileList) {
                    is = new FileInputStream(fileItem);
                    ftpClient.storeFile(fileItem.getName(), is);
                }
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("上传出现异常：" + e.getMessage());
                return false;
            } finally {
                is.close();
                ftpClient.disconnect();
            }
        }
        return false;
    }

    private boolean connec(String ip, String username, String pwd) {
        ftpClient = new FTPClient();
        try {
            ftpClient.login(ip, username, pwd);
            return true;
        } catch (IOException e) {
            logger.error("FTP服务器连接失败");
            e.printStackTrace();
            return false;
        }
    }

    public FTPUtil(String ip, String username, Integer port, String pwd) {
        this.ip = ip;
        this.username = username;
        this.port = port;
        this.pwd = pwd;
    }

    private String ip;
    private String username;
    private Integer port;
    private String pwd;
    private FTPClient ftpClient;

    public static String getFtpIp() {
        return ftpIp;
    }

    public static void setFtpIp(String ftpIp) {
        FTPUtil.ftpIp = ftpIp;
    }

    public static String getFtpUser() {
        return ftpUser;
    }

    public static void setFtpUser(String ftpUser) {
        FTPUtil.ftpUser = ftpUser;
    }

    public static String getFtpPass() {
        return ftpPass;
    }

    public static void setFtpPass(String ftpPass) {
        FTPUtil.ftpPass = ftpPass;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
