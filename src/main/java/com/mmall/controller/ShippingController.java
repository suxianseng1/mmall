package com.mmall.controller;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Shipping;
import com.mmall.pojo.User;
import com.mmall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Created by SMY on 2017/12/10.
 */
@Controller
@RequestMapping("/shipping/")
public class ShippingController {

    @Autowired
    private IShippingService shippingService;

    @RequestMapping(value = "add.do", method = RequestMethod.GET)
    @ResponseBody
    private ServerResponse<Integer> addShipping(Shipping shipping, HttpSession session) {
        Object obj = session.getAttribute(Const.CURRENT_USER);
        if (obj == null) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        int id = ((User) obj).getId();
        shipping.setUserId(id);
        return shippingService.addShipping(shipping);
    }

    @RequestMapping(value = "del.do", method = RequestMethod.POST)
    @ResponseBody
    private ServerResponse<String> delShipping(Integer shippingId){
        return shippingService.delShipping(shippingId);
    }

    @RequestMapping(value = "update.do", method = RequestMethod.GET)
    @ResponseBody
    private ServerResponse<String> updateShippingInfo(Shipping shipping){
        return shippingService.updateShippingInfo(shipping);
    }

    @RequestMapping(value = "select.do", method = RequestMethod.GET)
    @ResponseBody
    private ServerResponse<Shipping> viewShipping(Integer shippingId,HttpSession session){
        Object obj = session.getAttribute(Const.CURRENT_USER);
        if (obj == null) {
            return ServerResponse.createByErrorMessage("请登录之后查询");
        }
        return shippingService.viewShipping(shippingId);
    }
}
