package com.mmall.controller;

import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Cart;
import com.mmall.pojo.User;
import com.mmall.service.ICartService;
import com.mmall.vo.CartProductVo;
import com.mmall.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

/**
 * Created by SMY on 2017/12/14.
 */
@Controller
@RequestMapping("/cart/")
public class CartController {

    @Autowired
    private ICartService cartService;

    @RequestMapping("list.do")
    @ResponseBody
    private ServerResponse<CartVo> list(HttpSession session){
        Integer userId =  ((User)session.getAttribute(Const.CURRENT_USER)).getId();
        return cartService.list(userId);
    }

    @RequestMapping("add.do")
    @ResponseBody
    private ServerResponse<CartVo> add(HttpSession session,Integer productId,Integer count){
        Integer userId =  ((User)session.getAttribute(Const.CURRENT_USER)).getId();
        return cartService.add(userId,productId,count);
    }

    @RequestMapping("delete_product.do")
    @ResponseBody
    private ServerResponse<CartVo> delete(HttpSession session,String productIds){
        Integer userId =  ((User)session.getAttribute(Const.CURRENT_USER)).getId();
        return cartService.delete(userId,productIds);
    }

    @RequestMapping("update.do")
    @ResponseBody
    private ServerResponse<CartVo> update(HttpSession session,Integer productId,Integer count){
        Integer userId =  ((User)session.getAttribute(Const.CURRENT_USER)).getId();
        return cartService.add(userId,productId,count);
    }

    @RequestMapping("select.do")
    @ResponseBody
    private ServerResponse<CartVo> select(HttpSession session,Integer productId){
        Integer userId =  ((User)session.getAttribute(Const.CURRENT_USER)).getId();
        return cartService.cancelCheck(userId,productId);
    }

    @RequestMapping("un_select.do")
    @ResponseBody
    private ServerResponse<CartVo> unSelect(HttpSession session,Integer productId){
        Integer userId =  ((User)session.getAttribute(Const.CURRENT_USER)).getId();
        return cartService.checked(userId,productId);
    }

    @RequestMapping("get_cart_product_count.do")
    @ResponseBody
    private ServerResponse<Integer> getCartProductCount(HttpSession session){
        Integer userId =  ((User)session.getAttribute(Const.CURRENT_USER)).getId();
        return cartService.getCartProductCount(userId);
    }

    @RequestMapping("select_all.do")
    @ResponseBody
    private ServerResponse<CartVo> selectAll(HttpSession session){
        Integer userId =  ((User)session.getAttribute(Const.CURRENT_USER)).getId();
        return cartService.checkedOrUnCheckedAll(userId,Const.Cart.CHECKED);
    }

    @RequestMapping("un_select_all.do")
    @ResponseBody
    private ServerResponse<CartVo> unSelectAll(HttpSession session){
        Integer userId =  ((User)session.getAttribute(Const.CURRENT_USER)).getId();
        return cartService.checkedOrUnCheckedAll(userId,Const.Cart.UN_CHECKED);
    }
}
