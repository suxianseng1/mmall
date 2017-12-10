package com.mmall.service.impl;

import com.mmall.common.ServerResponse;
import com.mmall.dao.ShippingMapper;
import com.mmall.pojo.Shipping;
import com.mmall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by SMY on 2017/12/10.
 */
@Service("iShippingService")
public class ShippingServiceImpl implements IShippingService {

    @Autowired
    private ShippingMapper shippingMapper;

    public ServerResponse<Integer> addShipping(Shipping shipping){
       int id = shippingMapper.insertSelective(shipping);
       if(id >0 ){
           return ServerResponse.createBySuccess("新建地址成功",id);
       }
       return ServerResponse.createByError("新建地址失败");
    }

    public ServerResponse<String> delShipping(int shippingId){
        int result = shippingMapper.deleteByPrimaryKey(shippingId);
        if(result > 0){
            return ServerResponse.createBySuccess("删除地址成功");
        }
        return ServerResponse.createByError("删除地址失败");
    }

    public ServerResponse<String> updateShippingInfo(Shipping shipping){
        int result = shippingMapper.updateByPrimaryKeySelective(shipping);
        if(result >0){
            return ServerResponse.createBySuccess("更新地址成功");
        }
        return ServerResponse.createByError("更新地址失败");
    }

    public ServerResponse<Shipping> viewShipping(int shippingId){
        Shipping shipping = shippingMapper.selectByPrimaryKey(shippingId);
        if(shipping != null){
            return ServerResponse.createBySuccess(shipping);
        }
        return ServerResponse.createByError("没有找到该收货地址");
    }

}
