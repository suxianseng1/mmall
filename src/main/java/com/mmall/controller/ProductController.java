package com.mmall.controller;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.service.IProductService;
import com.mmall.vo.ProductDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by SMY on 2017/12/13.
 */
@Controller
@RequestMapping("/product/")
public class ProductController {

    @Autowired
    private IProductService productService;

    /**
     * categoryId
     * keyword
     * pageNum(default=1)
     * pageSize(default=10)
     * orderBy(default="")：排序参数：例如price_desc，price_asc
     *
     * @return
     */
    @RequestMapping("list.do")
    @ResponseBody
    private ServerResponse<PageInfo> list(Integer categoryId,//
                                          String keyWord,//
                                          @RequestParam(value="pageNum",defaultValue = "1",required = false) Integer pageNum,//
                                          @RequestParam(value="pageSize",defaultValue = "10",required = false) Integer pageSize,//
                                          @RequestParam(value="orderBy",defaultValue = "",required = false) String orderBy) {
        return productService.selectBykeywordAndCategoryId(categoryId, keyWord, pageNum, pageSize, orderBy);
    }

    @RequestMapping("detail.do")
    @ResponseBody
    private ServerResponse<ProductDetailVo> detail(Integer productId){
        if(productId == null)
            return ServerResponse.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        return productService.getProductDetail(productId);
    }
}
