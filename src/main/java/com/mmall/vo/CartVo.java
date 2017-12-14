package com.mmall.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by SMY on 2017/12/14.
 */
public class CartVo {

    private boolean allChecked;
    private BigDecimal cartTotalPrice;
    private List<CartProductVo> cartProductVoList;

    public boolean isAllChecked() {
        return allChecked;
    }

    public void setAllChecked(boolean allChecked) {
        this.allChecked = allChecked;
    }

    public BigDecimal getCartTotalPrice() {
        return cartTotalPrice;
    }

    public void setCartTotalPrice(BigDecimal cartTotalPrice) {
        this.cartTotalPrice = cartTotalPrice;
    }

    public List<CartProductVo> getCartProductVoList() {
        return cartProductVoList;
    }

    public void setCartProductVoList(List<CartProductVo> cartProductVoList) {
        this.cartProductVoList = cartProductVoList;
    }
}
