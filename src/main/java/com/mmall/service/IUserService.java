package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;

/**
 * Created by SMY on 2017/12/8.
 */
public interface IUserService {


    /**
     * 登录接口
     * @param username
     * @param password
     * @return
     */
    ServerResponse<User> checkLogin(String username,String password);

    /**
     * 注册接口
     * @param user
     * @return
     */
    ServerResponse<String> register(User user);

    /**
     * 用户未登录，忘记密码 需要修改密码的问题
     * @param username
     * @return
     */
    ServerResponse<String> forgetGetQuestion(String username);

    /**
     * 用户名或者Email验证
     * @param str
     * @param type
     * @return
     */
    ServerResponse<String> checkValid(String str,String type);

    /**
     * 验证用户问题和问题答案是否正确
     * @param username
     * @param answer
     * @param question
     * @return
     */
    ServerResponse<String> forgetCheckAnswer(String username,String answer,String question);

    /**
     * 修改密码
     * @param username
     * @param passwordNew
     * @param forgetToken
     * @return
     */
    ServerResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken);

    /**
     * 用户登录状态下修改密码
     * @param username
     * @param passwordNew
     * @param passwordOld
     * @return
     */
    ServerResponse<String> resetPassword(String username,String passwordOld,String passwordNew);

    /**
     * 用户登录状态修改用户信息
     * @param user
     * @return
     */
    ServerResponse<String> updataUserInfo(User user);

    /**
     * 登录状态下获取用户信息
     * @param userId
     * @return
     */
    ServerResponse<User> getInfomation(int userId);
}
