package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.Shipping;

/**
 * Created by Administrator on 2017/12/10.
 */
public interface IShippingService {

    ServerResponse<Integer> addShipping(Shipping shipping);

    ServerResponse<String> delShipping(int shippingId);

    ServerResponse<String> updateShippingInfo(Shipping shipping);

    ServerResponse<Shipping> viewShipping(int shippingId);
}
