package com.mmall.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

/**
 * Created by Administrator on 2017/12/12.
 */
public interface IFileService {

    String upload(MultipartFile multipartFile,String path);
}
