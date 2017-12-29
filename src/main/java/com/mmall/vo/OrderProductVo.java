package com.mmall.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by SMY on 2017/12/18.
 */
public class OrderProductVo {

    private String imageHost;
    private BigDecimal productTotalPrice;
    private List<OrderItemVo> orderItemVoList;

    public String getImageHost() {
        return imageHost;
    }

    public void setImageHost(String imageHost) {
        this.imageHost = imageHost;
    }

    public BigDecimal getProductTotalPrice() {
        return productTotalPrice;
    }

    public void setProductTotalPrice(BigDecimal productTotalPrice) {
        this.productTotalPrice = productTotalPrice;
    }

    public List<OrderItemVo> getOrderItemVoList() {
        return orderItemVoList;
    }

    public void setOrderItemVoList(List<OrderItemVo> orderItemVoList) {
        this.orderItemVoList = orderItemVoList;
    }
}
