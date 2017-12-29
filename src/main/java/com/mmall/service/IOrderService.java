package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.vo.OrderProductVo;
import com.mmall.vo.OrderVo;

import java.util.Map;

/**
 * Created by SMY on 2017/12/17.
 */
public interface IOrderService {

    ServerResponse pay(Integer userId, Long orderNo, String path);

    ServerResponse alipayCallBack(Map<String,String> params);

    ServerResponse queryOrderPayStatus(Integer userId,Long orderNo);

    ServerResponse<OrderVo> createOrder(Integer userId, Integer shippingId);

    ServerResponse<OrderProductVo> getOrderCartProduct(Integer userId,Long orderNo);

    ServerResponse<PageInfo> getList(Integer pageSize, Integer pageNum, Integer userId);

    ServerResponse<OrderVo> getDetail(Long orderNo, Integer userId);

    ServerResponse cancle(Long orderNo);




    // 管理员
    ServerResponse<PageInfo> manageList(Integer pageSize, Integer pageNum);

    ServerResponse<OrderVo> searchByOrderNo(Long orderNo);

    ServerResponse sendGoods(Long orderNo);
}
