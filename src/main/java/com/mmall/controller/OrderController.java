package com.mmall.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.util.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by SMY on 2017/12/17.
 */
@Controller
@RequestMapping("/order/")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    @Autowired
    private IOrderService orderService;

    @RequestMapping("pay.do")
    @ResponseBody
    private ServerResponse<Map<String, String>> pay(HttpSession session, Long orderNo,HttpServletRequest request) {
        Integer userId = ((User) session.getAttribute(Const.CURRENT_USER)).getId();
        String path = request.getSession().getServletContext().getRealPath(PropertiesUtil.getProperty("upload_image_path"));
        return  orderService.pay(userId,orderNo,path);
    }

    @RequestMapping("alipay_callback.do")
    @ResponseBody
    private ServerResponse callBack(HttpServletRequest request) throws AlipayApiException {
        Map<String, String[]> tempParams = request.getParameterMap();
        Map<String, String> requestParams = Maps.newHashMap();
        for (Iterator<String> iterator = tempParams.keySet().iterator(); iterator.hasNext(); ) {
            String key = iterator.next();
            String[] strs = tempParams.get(key);
            String str = "";
            for (int i = 0; i < strs.length; i++) {
                str = strs.length - 1 == i ? str + strs[i] : str + strs[i] + ",";
            }
        }
        requestParams.remove("sign_type");
        try {
            boolean result = AlipaySignature.rsaCheckV2(requestParams, Configs.getPublicKey(),"utf-8",Configs.getSignType());
            if(!result){
                return ServerResponse.createByErrorMessage("非法请求,验证不通过,再恶意请求我就报警找网警了");
        }
        } catch (AlipayApiException e) {
            logger.error("支付宝回调验证异常",e);
            e.printStackTrace();
            throw e;
        }
        ServerResponse serverResponse =  orderService.alipayCallBack(requestParams);
        if(serverResponse.isSuccess()){
            return ServerResponse.createBySuccess(Const.AlipayCallback.RESPONSE_FAILED);
        }
        return ServerResponse.createBySuccess(Const.AlipayCallback.RESPONSE_SUCCESS);
    }


    @RequestMapping(" query_order_pay_status.do")
    @ResponseBody
    private ServerResponse queryOrderPayStatus(Long orderNo,HttpSession session){
        Integer userId = ((User) session.getAttribute(Const.CURRENT_USER)).getId();
        if(orderNo == null){
            return ServerResponse.createByErrorMessage("无效参数，参数不能为空");
        }
        return orderService.queryOrderPayStatus(userId,orderNo);
    }
}
