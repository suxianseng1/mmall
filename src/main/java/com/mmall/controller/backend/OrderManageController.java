package com.mmall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.vo.OrderVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Created by SMY on 2017/12/20.
 */
@Controller
@RequestMapping("/manage/order/")
public class OrderManageController {

    @Autowired
    private IOrderService orderService;

    @RequestMapping("list.do")
    @ResponseBody
    private ServerResponse<PageInfo> list(HttpSession session, //
                                          @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,//
                                          @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum) {
        Integer userId = ((User) session.getAttribute(Const.CURRENT_USER)).getId();
        return orderService.manageList(pageSize, pageNum);
    }

    @RequestMapping("search.do")
    @ResponseBody
    private ServerResponse<OrderVo> search(Long orderNo){
        if(orderNo == null || orderNo == 0L){
            return ServerResponse.createByErrorMessage("请输入订单号");
        }
        return orderService.searchByOrderNo(orderNo);
    }

    @RequestMapping("detail.do")
    @ResponseBody
    private ServerResponse<OrderVo> detail(Long orderNo){
        if(orderNo == null || orderNo == 0L){
            return ServerResponse.createByErrorMessage("请输入订单号");
        }
        return orderService.searchByOrderNo(orderNo);
    }


    @RequestMapping("send_goods.do")
    @ResponseBody
    private ServerResponse sendGoods(Long orderNo){
        if(orderNo == null || orderNo == 0L){
            return ServerResponse.createByErrorMessage("请输入订单号");
        }
        return orderService.sendGoods(orderNo);
    }
}
