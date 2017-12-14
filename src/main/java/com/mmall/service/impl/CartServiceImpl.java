package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CartMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Cart;
import com.mmall.pojo.Product;
import com.mmall.service.ICartService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.vo.CartProductVo;
import com.mmall.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by SMY on 2017/12/14.
 */
@Service("iCarService")
public class CartServiceImpl implements ICartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    public ServerResponse<CartVo> list(Integer userId) {

        return ServerResponse.createBySuccess(getCartVo(userId));
    }

    @Override
    public ServerResponse<CartVo> add(Integer userId, Integer productId, Integer count) {
        Cart cart = cartMapper.selectCartByUserIdAndProductId(userId, productId);
        // 如果存在就更新，不存在就添加插入
        if (cart == null) {
            cart = new Cart();
            cart.setQuantity(count);
            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setChecked(Const.Cart.CHECKED);
            cartMapper.insert(cart);
        } else {
            Cart cartTemp = new Cart();
            count = cart.getQuantity() + count;
            cartTemp.setId(cart.getId());
            cartTemp.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cartTemp);
        }
        return ServerResponse.createBySuccess(this.getCartVo(userId));
    }

    @Override
    public ServerResponse<CartVo> delete(Integer userId, String productIds) {
        String[] ids = productIds.split(",");
        List<Integer> idList = Lists.newArrayList();
        for (String id : ids) {
            idList.add(Integer.parseInt(id));
        }
        int result = cartMapper.deleteByUserIdAndProductId(userId, idList);
        if (result < 0) {
            return ServerResponse.createByErrorMessage("移除失败");
        }
        return ServerResponse.createBySuccess(this.getCartVo(userId));
    }

    @Override
    public ServerResponse<CartVo> cancelCheck(Integer userId, Integer productId) {
        Cart cart = cartMapper.selectCartByUserIdAndProductId(userId, productId);
        if (cart == null) {
            return ServerResponse.createByErrorMessage("购物车中没有该商品");
        }
        Cart cartTemp = new Cart();
        cartTemp.setChecked(Const.Cart.CHECKED);
        cartTemp.setId(cart.getId());
        int result = cartMapper.updateByPrimaryKeySelective(cartTemp);
        if (result < 0) {
            return ServerResponse.createByErrorMessage("选中失败");
        }
        return ServerResponse.createBySuccess(this.getCartVo(userId));

    }

    @Override
    public ServerResponse<CartVo> checked(Integer userId, Integer productId) {
        Cart cart = cartMapper.selectCartByUserIdAndProductId(userId, productId);
        if (cart == null) {
            return ServerResponse.createByErrorMessage("购物车中没有该商品");
        }
        Cart cartTemp = new Cart();
        cartTemp.setChecked(Const.Cart.UN_CHECKED);
        cartTemp.setId(cart.getId());
        int result = cartMapper.updateByPrimaryKeySelective(cartTemp);
        if (result < 0) {
            return ServerResponse.createByErrorMessage("取消选中失败");
        }
        return ServerResponse.createBySuccess(this.getCartVo(userId));

    }

    @Override
    public ServerResponse<Integer> getCartProductCount(Integer userId) {
        int count = cartMapper.selectCountByUserId(userId);
        return ServerResponse.createBySuccess(count);
    }


    @Override
    public ServerResponse<CartVo> checkedOrUnCheckedAll(Integer userId,Integer check) {
        List<Integer> ids = cartMapper.selectAllIdByUserId(userId);
        if (ids != null) {
            for (Integer id : ids) {
                Cart cart = new Cart();
                cart.setId(id);
                cart.setChecked(Const.Cart.CHECKED);
                cartMapper.updateByPrimaryKeySelective(cart);
            }
        }
        return ServerResponse.createBySuccess(this.getCartVo(userId));
    }


    /**
     * @param userId
     * @return
     */
    private CartVo getCartVo(Integer userId) {
        List<Cart> cartList = cartMapper.selectListByUserId(userId);
        CartVo cartVo = new CartVo();
        CartProductVo cartProductVo = new CartProductVo();
        BigDecimal totalPrice = new BigDecimal("0");
        List<CartProductVo> cartProductVoList = Lists.newArrayList();
        if (cartList != null) {
            for (Cart cart : cartList) {
                Product product = productMapper.selectByPrimaryKey(cart.getProductId());
                // 在这里把两个对象的值 合并到 CartProductVo 中
                setPropertyToCartProductVo(cartProductVo, cart, product);
                //  判断购物车中的商品数量是否还有那么多的库存
                if (product.getStock() > cart.getQuantity()) {
                    // 库存大于购物车数量
                    cartProductVo.setQuantity(cart.getQuantity());
                    cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                } else {
                    // 购物车 数量超过了库存
                    cartProductVo.setQuantity(product.getStock());
                    cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
                    Cart cartTemp = new Cart();
                    cartTemp.setId(cart.getId());
                    cartTemp.setQuantity(product.getStock());
                    // 没有那么多库存就更新 数据库中的数据
                    cartMapper.updateByPrimaryKeySelective(cartTemp);
                }
                // 计算并保存 购物车中被选中商品的全部价钱
                if (cart.getChecked() == Const.Cart.CHECKED) {
                    BigDecimal tempPrice = BigDecimalUtil.mul(Double.valueOf(product.getPrice().doubleValue()), cart.getQuantity());
                    totalPrice = BigDecimalUtil.add(totalPrice.doubleValue(), tempPrice.doubleValue());
                }
                cartProductVoList.add(cartProductVo);
            }
        }
        // 最后判断 是否全部被选中
        if (checkAllChecked(userId))
            cartVo.setAllChecked(true);
        else
            cartVo.setAllChecked(false);
        cartVo.setCartTotalPrice(totalPrice);
        cartVo.setCartProductVoList(cartProductVoList);
        return cartVo;
    }

    /**
     * @param userId
     * @return
     */
    private boolean checkAllChecked(Integer userId) {
        return cartMapper.checkAllChecked(userId) == 0;
    }

    private void setPropertyToCartProductVo(CartProductVo cartProductVo, Cart cart, Product product) {
        cartProductVo.setId(cart.getId());
        cartProductVo.setUserId(cart.getUserId());
        cartProductVo.setQuantity(cart.getQuantity());
        cartProductVo.setProductId(cart.getProductId());
        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
        cartProductVo.setProductId(product.getId());
        cartProductVo.setProductName(product.getName());
        cartProductVo.setProductPrice(product.getPrice());
        cartProductVo.setProductStock(product.getStock());
        cartProductVo.setProductMainImage(product.getMainImage());
        cartProductVo.setProductStatus(product.getStatus());
        cartProductVo.setProductSubtitle(product.getSubtitle());
       /* cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);*/
    }

}
