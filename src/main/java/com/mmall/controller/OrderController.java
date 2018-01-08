package com.mmall.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.OrderProductVo;
import com.mmall.vo.OrderVo;
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

    @RequestMapping("create.do")
    @ResponseBody
    private ServerResponse<OrderVo> create(HttpSession session, Integer shippingId) {
        Integer userId = ((User) session.getAttribute(Const.CURRENT_USER)).getId();
        if (shippingId == null || shippingId == 0) {
            return ServerResponse.createByErrorMessage("地址不能为空，请选择收货地址");
        }
        return orderService.createOrder(userId, shippingId);
    }

    @RequestMapping("get_order_cart_product.do")
    @ResponseBody
    private ServerResponse<OrderProductVo> getOrderCartProduct(HttpSession session, Long orderNo) {
        Integer userId = ((User) session.getAttribute(Const.CURRENT_USER)).getId();
        if (orderNo == null || orderNo == 0L) {
            return ServerResponse.createByErrorMessage("订单号不能为空");
        }
        return orderService.getOrderCartProduct(userId, orderNo);
    }


    @RequestMapping("list.do")
    @ResponseBody
    private ServerResponse<PageInfo> list(HttpSession session, //
                                          @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,//
                                          @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum) {
        Integer userId = ((User) session.getAttribute(Const.CURRENT_USER)).getId();
        return orderService.getList(pageSize, pageNum, userId);
    }

    @RequestMapping("detail.do")
    @ResponseBody
    private ServerResponse<OrderVo> detail(HttpSession session,Long orderNo){
        Integer userId = ((User) session.getAttribute(Const.CURRENT_USER)).getId();
        return orderService.getDetail(orderNo, userId);
    }

    @RequestMapping("cancel.do")
    @ResponseBody
    private ServerResponse cancel(HttpSession session,Long orderNo){
        Integer userId = ((User) session.getAttribute(Const.CURRENT_USER)).getId();
        if (orderNo == null || orderNo == 0L) {
            return ServerResponse.createByErrorMessage("订单号不能为空");
        }
        return orderService.cancle(orderNo);
    }

    @RequestMapping("pay.do")
    @ResponseBody
    private ServerResponse<Map<String, String>> pay(HttpSession session, Long orderNo, HttpServletRequest request) {
        Integer userId = ((User) session.getAttribute(Const.CURRENT_USER)).getId();
        String path = request.getSession().getServletContext().getRealPath(PropertiesUtil.getProperty("upload_image_path"));
        if (orderNo == null || orderNo == 0L) {
            return ServerResponse.createByErrorMessage("支付订单不能为空");
        }
        return orderService.pay(userId, orderNo, path);
    }

    @RequestMapping("alipay_callback.do")
    @ResponseBody
    private ServerResponse callBack(HttpServletRequest request) throws AlipayApiException {
        // 取出支付宝回调携带的所有参数并进行转换，数组转换为字符串
        Map<String, String[]> tempParams = request.getParameterMap();
        //  参数存放 Map
        Map<String, String> requestParams = Maps.newHashMap();
        for (Iterator<String> iterator = tempParams.keySet().iterator(); iterator.hasNext(); ) {
            String key = iterator.next();
            String[] strs = tempParams.get(key);
            String str = "";
            // 这里如果数组的长度是1，说明只有一个，直接赋值就好，如果超过一个，后面加一个逗号来隔离
            for (int i = 0; i < strs.length; i++) {
                str = strs.length - 1 == i ? str + strs[i] : str + strs[i] + ",";
            }
            requestParams.put(key, str);
        }
        // 去除sign_type
        requestParams.remove("sign_type");
        try {
            // 验证签名
            boolean result = AlipaySignature.rsaCheckV2(requestParams, Configs.getPublicKey(), "utf-8", Configs.getSignType());
            if (!result) {
                return ServerResponse.createByErrorMessage("非法请求,再恶意请求我就报警找网警了");
            }
        } catch (AlipayApiException e) {
            logger.error("支付宝回调验证异常", e);
            e.printStackTrace();
            throw e;
        }
        // 调用Service 方法进行处理
        ServerResponse serverResponse = orderService.alipayCallBack(requestParams);
        if (!serverResponse.isSuccess()) {
            logger.error("OrderController.callBack()","数据操作失败");
            return ServerResponse.createBySuccess(Const.AlipayCallback.RESPONSE_FAILED);
        }
        logger.info("支付宝支付回调完成，没有异常");
        return ServerResponse.createBySuccess(Const.AlipayCallback.RESPONSE_SUCCESS);
    }


    @RequestMapping(" query_order_pay_status.do")
    @ResponseBody
    private ServerResponse queryOrderPayStatus(Long orderNo, HttpSession session) {
        Integer userId = ((User) session.getAttribute(Const.CURRENT_USER)).getId();
        if (orderNo == null) {
            return ServerResponse.createByErrorMessage("无效参数，参数不能为空");
        }
        return orderService.queryOrderPayStatus(userId, orderNo);
    }
}
