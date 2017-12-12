package com.mmall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Product;
import com.mmall.service.IFileService;
import com.mmall.service.IProductService;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVo;
import com.sun.deploy.net.HttpResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.MalformedURLException;
import java.util.Map;

/**
 * Created by SMY on 2017/12/12.
 */
@Controller
@RequestMapping("/manage/product/")
public class ProductController {

    @Autowired
    private IFileService fileService;
    @Autowired
    private IProductService productService;

    /**
     * 更新或者插入商品
     *
     * @param product
     * @return
     */
    @RequestMapping(value = "save.do", method = RequestMethod.GET)
    @ResponseBody
    private ServerResponse<String> save(Product product) {
        if (product == null)
            return ServerResponse.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        return productService.saveOrUpdate(product);
    }

    /**
     * 修改商品状态
     *
     * @param productId
     * @param status
     * @return
     */
    @RequestMapping(value = "set_sale_status.do", method = RequestMethod.GET)
    @ResponseBody
    private ServerResponse<String> setSaleStatus(Integer productId, Integer status) {
        if (productId == null || status == null)
            return ServerResponse.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());

        return productService.setSaleStatus(productId, status);
    }

    /**
     * 获取商品详情
     *
     * @param productId
     * @return
     */
    @RequestMapping(value = "detail.do", method = RequestMethod.GET)
    @ResponseBody
    private ServerResponse<ProductDetailVo> getProductDetail(Integer productId) {
        if (productId == null)
            return ServerResponse.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        return productService.getProductDetail(productId);
    }

    /**
     * 根据条件查询商品并分页返回
     *
     * @param productName
     * @param productId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "search.do", method = RequestMethod.GET)
    @ResponseBody
    private ServerResponse<PageInfo> search(String productName, Integer productId, @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum, @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        if (pageNum == null || pageSize == null)
            return ServerResponse.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        return productService.searchProductList(productName, productId, pageNum, pageSize);
    }

    /**
     * 分页获取商品所有详情
     *
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "list.do", method = RequestMethod.GET)
    @ResponseBody
    private ServerResponse<PageInfo> list(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum, @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        if (pageNum == null || pageSize == null)
            return ServerResponse.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        return productService.searchProductList(null, null, pageNum, pageSize);
    }


    /**
     * 上传富文本文件
     * 插件simditor约定
     *
     * @param file
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "richtext_img_upload.do", method = RequestMethod.POST)
    @ResponseBody
    private Map richtextImgUpload(@RequestParam(value = "upload_file", required = false) MultipartFile file, HttpServletRequest request, HttpServletResponse response) {
        Map map = Maps.newHashMap();
        if (file == null) {
            map.put("success", false);
            map.put("msg", "上传图片为空");
        }
        // path 是跟WEB-INF同级下
        String path = request.getSession().getServletContext().getRealPath("upload");
        String fileName = fileService.upload(file, path);
        if (StringUtils.isNotBlank(fileName)) {
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + fileName;
            map.put("success", true);
            map.put("msg", "上传成功");
            map.put("file_path", url);
            response.addHeader("Access-Control-Allow-Headers", "X-File-Name");
            return map;
        } else {
            map.put("success", false);
            map.put("msg", "上传失败");
            return map;
        }
    }

    /**
     * 上传图片文件
     *
     * @param file
     * @param request
     * @return
     */
    @RequestMapping(value = "upload.do", method = RequestMethod.POST)
    @ResponseBody
    private ServerResponse<Map> upload(@RequestParam(value = "upload_file", required = false) MultipartFile file, HttpServletRequest request) {
        Map map = Maps.newHashMap();
        if (file == null) {
            map.put("success", false);
            map.put("msg", "上传图片为空");
        }
        // path 是跟WEB-INF同级下
        String path = request.getSession().getServletContext().getRealPath("upload");
        String fileName = fileService.upload(file, path);
        if (StringUtils.isNotBlank(fileName)) {
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + fileName;
            map.put("msg", "上传成功");
            map.put("url", url);
            map.put("uri", fileName);
            return ServerResponse.createBySuccess(map);
        } else {
            return ServerResponse.createByErrorMessage("上传失败");
        }
    }

}
