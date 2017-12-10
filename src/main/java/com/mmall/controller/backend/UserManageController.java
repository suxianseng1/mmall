package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Created by Administrator on 2017/12/10.
 */
@Controller
@RequestMapping("/manage/user/")
public class UserManageController {

    @Autowired
    private IUserService userService;

    @RequestMapping(value="/login.do",method = RequestMethod.POST)
    @ResponseBody
    private ServerResponse<User> login(String username, String password, HttpSession session){
        ServerResponse<User> serverResponse = userService.checkLogin(username, password);
        User user = serverResponse.getData();
        if(serverResponse.isSuccess()){
            session.setAttribute(Const.CURRENT_USER,serverResponse.getData());
            if(user.getRole()== Const.Role.ROLE_ADMIN){
                return ServerResponse.createBySuccess(user);
            } else {
                return ServerResponse.createByError("用户没有权限进入");
            }
        } else {
            return ServerResponse.createByError("账号或者密码错误");
        }
    }
}
