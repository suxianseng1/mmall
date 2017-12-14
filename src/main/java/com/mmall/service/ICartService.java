package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.vo.CartProductVo;
import com.mmall.vo.CartVo;

import java.util.List;

/**
 * Created by SMY on 2017/12/14.
 */
public interface ICartService {

    ServerResponse<CartVo> list(Integer userId);

    ServerResponse<CartVo> add(Integer userId, Integer productId, Integer count);

    ServerResponse<CartVo> delete(Integer userId, String productIds);

    ServerResponse<CartVo> cancelCheck(Integer userId, Integer productId);

    ServerResponse<CartVo> checked(Integer userId, Integer productId);

    ServerResponse<Integer> getCartProductCount(Integer userId);

    ServerResponse<CartVo> checkedOrUnCheckedAll(Integer userId,Integer check);
}
