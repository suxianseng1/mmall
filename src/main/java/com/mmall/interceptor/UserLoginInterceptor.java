package com.mmall.interceptor;

import com.alibaba.fastjson.JSON;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import netscape.javascript.JSObject;
import org.apache.ibatis.plugin.Intercepts;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Created by SMY on 2017/12/20.
 */
public class UserLoginInterceptor extends HandlerInterceptorAdapter {

    /**
     * This implementation always returns {@code true}.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        HttpSession session = request.getSession();
        Object obj = session.getAttribute(Const.CURRENT_USER);
        if (obj != null) {
            return true;
        }
        ServerResponse serverResponse = ServerResponse.createByErrorMessage("您还没有登录，请登录！");
        Object json = JSON.toJSON(serverResponse);
        System.out.println(json.toString());
        response.getWriter().print(json.toString());
        return false;
    }

}
