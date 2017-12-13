package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Category;
import com.mmall.pojo.Product;
import com.mmall.service.ICategoryService;
import com.mmall.service.IProductService;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVo;
import com.mmall.vo.ProductListVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Administrator on 2017/12/12.
 */
@Service("iProductService")
public class ProductServiceImpl implements IProductService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ICategoryService categoryService;

    @Override
    public ServerResponse<String> saveOrUpdate(Product product) {
        String[] imgs = null;
        if (product.getSubImages() != null)
            imgs = product.getSubImages().split(",");
        if (imgs != null && imgs.length > 0) {
            product.setMainImage(imgs[0]);
        }
        if (product.getId() == null || product.getId() == 0) {
            int result = productMapper.insertSelective(product);
            if (result > 0) {
                return ServerResponse.createBySuccessMessage("新增产品成功");
            }
            return ServerResponse.createByErrorMessage("新增产品失败");
        } else {
            int result = productMapper.updateByPrimaryKeySelective(product);
            if (result > 0) {
                return ServerResponse.createBySuccessMessage("更新产品成功");
            }
            return ServerResponse.createByErrorMessage("更新产品失败");
        }
    }

    public ServerResponse<String> setSaleStatus(int productId, int status) {
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);
        int result = productMapper.updateByPrimaryKeySelective(product);
        if (result > 0) {
            return ServerResponse.createBySuccess("修改产品状态成功");
        }
        return ServerResponse.createByErrorMessage("修改产品状态失败");
    }

    public ServerResponse<ProductDetailVo> getProductDetail(int productId) {
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null)
            return ServerResponse.createByErrorMessage("没有查到该商品信息，已下架或被删除");
        ProductDetailVo productDetailVo = this.parseToProductDetailVo(product);
        return ServerResponse.createBySuccess(productDetailVo);
    }


    @Override
    public ServerResponse<PageInfo> searchProductList(String productName,//
                                                      Integer productId,//
                                                      Integer pageNum,//
                                                      Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        if (StringUtils.isNotBlank(productName))
            productName = new StringBuilder().append("%").append(productName).append("%").toString();
        List<Product> products = productMapper.selectByNameAndId(productName, productId);
        List<ProductListVo> productListVos = Lists.newArrayList();
        for (Product product : products) {
            ProductListVo productListVo = this.parseToProductListVo(product);
            productListVos.add(productListVo);
        }
        PageInfo pageInfo = new PageInfo(products);
        pageInfo.setList(productListVos);
        return ServerResponse.createBySuccess(pageInfo);
    }

    public ServerResponse<PageInfo> selectBykeywordAndCategoryId(Integer categoryId,//
                                                                 String keyWord,//
                                                                 Integer pageNum,//
                                                                 Integer pageSize,//
                                                                 String orderBy) {
        List<Integer> categoryIds = Lists.newArrayList();
        if(categoryId!=null){
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            if(category == null && StringUtils.isBlank(keyWord)){
                PageHelper.startPage(pageNum,pageSize);
                List<ProductListVo> productList = Lists.newArrayList();
                PageInfo pageInfo = new PageInfo(productList);
                return ServerResponse.createBySuccess(pageInfo);
            }
            // 递归获得所有
            categoryService.getDeepCateory(categoryId,categoryIds);
        }
        PageHelper.startPage(pageNum,pageSize);
        if(Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)){
            orderBy = orderBy.replace("_"," ");
            PageHelper.orderBy(orderBy);
        }
        if(StringUtils.isNotBlank(keyWord))
            keyWord = "%"+keyWord+"%";
        List<Product> productList = productMapper.selectBykeywordAndProductIds(keyWord==null?null:keyWord,categoryIds.size()==0?null:categoryIds);
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for(Product product : productList){
            ProductListVo productListVo = this.parseToProductListVo(product);
            productListVoList.add(productListVo);
        }
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    /**
     * 把 Product 类转换成 ProductListVo 包装类
     *
     * @param product
     * @return
     */
    private ProductListVo parseToProductListVo(Product product) {
        ProductListVo productListVo = new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setSubtitle(product.getSubtitle());
        productListVo.setPrice(product.getPrice());
        productListVo.setMainImage(product.getMainImage());
        productListVo.setCategoryId(product.getCategoryId());
        productListVo.setName(product.getName());
        productListVo.setStatus(product.getStatus());
        productListVo.setImageHost(PropertiesUtil.getProperty(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://img.happymmall.com/")));
        return productListVo;
    }

    /**
     * 把 Product 类转换成 ProductDetailVO 包装类
     *
     * @param product
     * @return
     */
    private ProductDetailVo parseToProductDetailVo(Product product) {
        ProductDetailVo productDetailVo = new ProductDetailVo();
        productDetailVo.setId(product.getId());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImages(product.getSubImages());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setName(product.getName());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setStock(product.getStock());

        productDetailVo.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        productDetailVo.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));
        // 因为Product类中只有categoryId没有parentId ，所以要根据categoryId查询一下类别
        // 如果没有查询到该商品属于哪个类别，默认就属于 根节点0
        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if (category == null) {
            productDetailVo.setParentCategoryId(0);
        } else {
            productDetailVo.setCategoryId(category.getParentId());
        }
        productDetailVo.setImageHost(PropertiesUtil.getProperty(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://img.happymmall.com/")));
        return productDetailVo;
    }
}
