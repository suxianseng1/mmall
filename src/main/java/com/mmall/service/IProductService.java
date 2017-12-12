package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.vo.ProductDetailVo;

/**
 * Created by SMY on 2017/12/12.
 */
public interface IProductService {

    ServerResponse<String> saveOrUpdate(Product product);

    ServerResponse<String> setSaleStatus(int productId,int status);

    ServerResponse<ProductDetailVo> getProductDetail(int productId);

    ServerResponse<PageInfo> searchProductList(String productName, Integer productId, Integer pageNum, Integer pageSize);
}
