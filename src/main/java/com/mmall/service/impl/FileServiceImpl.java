package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Created by Administrator on 2017/12/12.
 */
@Service("iFileService")
public class FileServiceImpl implements IFileService {

    private static Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    @Override
    public String upload(MultipartFile multipartFile,String path) {
        // 获取文件名称
        String fileName = multipartFile.getOriginalFilename();
        //获取文件的类型，后缀名
        String suffix = fileName.substring(fileName.lastIndexOf('.')+1);
        String targetFileName = (UUID.randomUUID()+fileName)+"."+suffix;
        logger.info("上传的文件名称{},新名称{},文件类型{},文件保存本地路径{}",fileName,targetFileName,suffix,path);
        File fileDir = new File(path);
        if(!fileDir.exists()) {
            fileDir.mkdirs();
            fileDir.setWritable(true);
        }
        File targetFile = new File(path,targetFileName);
        try {
            //先把文件上传到本地
            multipartFile.transferTo(targetFile);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("上传到本地失败",e.getMessage());
        }
        try {
            // 上传到FTP
            FTPUtil.upload(Lists.newArrayList(targetFile));
            targetFile.delete();
        } catch (IOException e) {
            logger.error("上传到FTP失败",e.getMessage());
            e.printStackTrace();
            return null;
        }
        return targetFile.getName();
    }
}
