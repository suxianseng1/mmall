package com.mmall.controller;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpSession;

/**
 * Created by SMY on 2017/12/8.
 */
@Controller
@RequestMapping("/user/")
public class UserController {

    @Autowired
    private IUserService userService;
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    /**
     * 用户登录
     * @param username
     * @param password
     * @param session
     * @return
     */
    @RequestMapping(value="login.do",method = RequestMethod.POST)
    @ResponseBody
    private ServerResponse<User> login(String username, String password, HttpSession session){
        ServerResponse<User> response = userService.checkLogin(username,password);
        if(response.isSuccess()){
            session.setAttribute(Const.CURRENT_USER,response.getData());
        }
        return response;
    }

    /**
     * 用户退出登录
     * @param session
     * @return
     */
    @RequestMapping(value="loginout.do",method = RequestMethod.POST)
    @ResponseBody
    private ServerResponse<String> loginOut(HttpSession session){
        try {
            session.removeAttribute(Const.CURRENT_USER);
            return ServerResponse.createBySuccessMessage("退出成功");
        } catch(Exception e){
            log.error(e.getMessage());
            return ServerResponse.createByErrorMessage("服务器异常");
        }
    }

    /**
     * 用户注册
     * @param user
     * @return
     */
    @RequestMapping(value="register.do",method = RequestMethod.POST)
    @ResponseBody
    private ServerResponse<String> register(User user){
        return userService.register(user);
    }

    /**
     * 校验用户名和Email
     * @param str
     * @param type
     * @return
     */
    @RequestMapping(value="check_valid.do",method = RequestMethod.POST)
    @ResponseBody
    private ServerResponse<String> checkValid(String str,String type){
        return userService.checkValid(str,type);
    }

    /**
     * 获取用户信息
     * @param session
     * @return
     */
    @RequestMapping(value="get_user_info.do",method = RequestMethod.POST)
    @ResponseBody
    private ServerResponse<User> getUserInfo(HttpSession session){
        Object obj = session.getAttribute(Const.CURRENT_USER);
        User user = null;
        if(obj == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,无法获取当前用户信息");
        }
        user = (User)obj;
        return ServerResponse.createBySuccess(user);
    }

    /**
     * 未登录下忘记密码，获取验证问题
     * @param username
     * @return
     */
    @RequestMapping(value="forget_get_question.do",method = RequestMethod.POST)
    @ResponseBody
    private ServerResponse<String> forgetGetQuestion(String username){
        return userService.forgetGetQuestion(username);
    }

    /**
     * 验证用户密保问题答案
     * @param username
     * @param answer
     * @param question
     * @return
     */
    @RequestMapping(value="forget_check_answer.do",method = RequestMethod.POST)
    @ResponseBody
    private ServerResponse<String> forgetCheckAnswer(String username,String answer,String question){
        return userService.forgetCheckAnswer(username,answer,question);
    }

    /**
     * 用户密码忘记，重置密码
     * @param username
     * @param passwordNew
     * @param forgetToken
     * @return
     */
    @RequestMapping(value="forget_reset_password.do",method = RequestMethod.POST)
    @ResponseBody
    private ServerResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken){
        return userService.forgetResetPassword(username, passwordNew, forgetToken);
    }

    /**
     * 登录状态下修改用户密码
     * @param passwordOld
     * @param passwordNew
     * @param session
     * @return
     */
    @RequestMapping(value="reset_password.do",method = RequestMethod.POST)
    @ResponseBody
    private ServerResponse<String> resetPassword(String passwordOld,String passwordNew,HttpSession session){
        Object obj = session.getAttribute(Const.CURRENT_USER);
        if(obj == null){
            return ServerResponse.createByErrorMessage("用户还未登录");
        }
        User user = (User)obj;
        return userService.resetPassword(user.getUsername(),passwordOld,passwordNew);
    }

    /**
     * 登录状态下修改用户信息
     * @param session
     * @param user
     * @return
     */
    @RequestMapping(value="update_information.do",method = RequestMethod.POST)
    @ResponseBody
    private ServerResponse<String> updateInformation(HttpSession session,User user){
        Object obj = session.getAttribute(Const.CURRENT_USER);
        if(obj == null){
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        // 从Session中获取的用户ID设置到更新的User中
        user.setId(((User)obj).getId());
        ServerResponse<String> serverResponse = userService.updataUserInfo(user);
        if(serverResponse.isSuccess()){
            session.setAttribute(Const.CURRENT_USER,user);
        }
        return serverResponse;
    }
}
