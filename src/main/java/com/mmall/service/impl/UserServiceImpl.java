package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Created by Administrator on 2017/12/8.
 */
@Service("iUserService")
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 用户账号密码登录
     *
     * @param username
     * @param password
     * @return
     */
    @Override
    public ServerResponse<User> checkLogin(String username, String password) {
        int result = userMapper.checkUsername(username);
        if (result < 0) {
            return ServerResponse.createByError("用户名不存在");
        }
        //将密码进行MD5加密后再进行数据库查询
        password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username, password);
        if (user == null) {
            return ServerResponse.createByError("密码错误");
        }
        user.setPassword(null);
        return ServerResponse.createBySuccess("登录成功", user);
    }

    /**
     * 用户注册
     *
     * @param user
     * @return
     */
    @Override
    public ServerResponse<String> register(User user) {
        //检查用户名是否已经存在,保证用户名唯一
        ServerResponse checkUsernameValid = this.checkValid(user.getUsername(), Const.USERNAME);
        if (!checkUsernameValid.isSuccess()) {
            return ServerResponse.createByError("该用户已存在");
        }
        //检查Email是否存在
        ServerResponse checkEmailValid = this.checkValid(user.getEmail(), Const.EMAIL);
        if (!checkEmailValid.isSuccess()) {
            return ServerResponse.createByError("Email已被使用");
        }
        //设置用户类型为普通用户
        user.setRole(Const.Role.ROLE_CUSTOMER);
        //将密码进行MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        //执行插入
        int result = userMapper.insert(user);
        if (result > 0) {
            return ServerResponse.createBySuccess("注册成功");
        }
        return ServerResponse.createByError("注册失败");
    }

    /**
     * 获取验证问题
     * @param username 用户名
     * @return
     */
    @Override
    public ServerResponse<String> forgetGetQuestion(String username) {
        // 首先查询一下用户存不存在
        ServerResponse checkUsernameValid = this.checkValid(username, Const.USERNAME);
        if (checkUsernameValid.isSuccess()) {
            // 用户不存在
            return ServerResponse.createByError("用户不存在");
        }
        // 查询用户问题
        String question = userMapper.forgetGetQuestion(username);
        if (StringUtils.isNotBlank(question)) {
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByError("该用户未设置找回密码问题");
    }

    /**
     *  校验 用户名|密码
     * @param str
     * @param type
     * @return
     */
    @Override
    public ServerResponse<String> checkValid(String str, String type) {
        if (StringUtils.isNotBlank(str) && StringUtils.isNotBlank(type)) {
            int result = 0;
            // 匹配类型 是 username 或者 Email
            if (type.equals(Const.USERNAME)) {
                result = userMapper.checkUsername(str);
                if (result > 0) {
                    return ServerResponse.createByError("该用户已存在");
                }
            } else {
                result = userMapper.checkUserEmail(str);
                if (result > 0) {
                    return ServerResponse.createByError("Email已被使用");
                }
            }
        } else {
            return ServerResponse.createByError("参数错误");
        }
        return ServerResponse.createBySuccess("校验成功");
    }

    /**
     * 校验问题答案
     * @param username 用户名
     * @param answer 答案
     * @param question 问题
     * @return
     */
    @Override
    public ServerResponse<String> forgetCheckAnswer(String username, String answer, String question) {
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(answer) && StringUtils.isNotBlank(question)) {
            int result = userMapper.forgetCheckAnswer(username, answer, question);
            if (result > 0) {
                String token = UUID.randomUUID().toString();
                TokenCache.put(TokenCache.TOKEN + username, token);
                return ServerResponse.createBySuccess(token);
            }
            return ServerResponse.createByError("问题答案错误");
        }
        return ServerResponse.createByError("参数不能为空");
    }

    /**
     * 用户未登录下 忘记密码，重置密码
     * @param username 用户名
     * @param passwordNew 新密码
     * @param forgetToken token 用来校验有效时间
     * @return
     */
    @Override
    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken) {
        if(StringUtils.isBlank(forgetToken)){
            return ServerResponse.createByError("参数错误，Token需要传递");
        }
        ServerResponse checkUsernameValid = this.checkValid(username, Const.USERNAME);
        if (checkUsernameValid.isSuccess()) {
            return ServerResponse.createByError("该用户不存在");
        }
        String token = TokenCache.getToken(TokenCache.TOKEN + username);
        if (StringUtils.isNotBlank(token) && StringUtils.equals(forgetToken,token)) {
            int result = userMapper.resetPassword(username, passwordNew);
            if (result > 0) {
                return ServerResponse.createBySuccess("修改密码成功");
            } else {
                return ServerResponse.createByError("修改密码操作失效");
            }
        }
        return ServerResponse.createByError("token已经失效");
    }

    /**
     * 修改密码
     * @param username 用户名
     * @param passwordOld 旧密码
     * @param passwordNew 新密码
     * @return
     */
    @Override
    public ServerResponse<String> resetPassword(String username, String passwordOld, String passwordNew) {
        passwordOld = MD5Util.MD5EncodeUtf8(passwordOld);
        passwordNew = MD5Util.MD5EncodeUtf8(passwordNew);
        int result = userMapper.updatePassword(username, passwordOld, passwordNew);
        if (result > 0) {
            return ServerResponse.createBySuccess("修改密码成功");
        }
        return ServerResponse.createByError("旧密码输入错误");
    }

    /**
     * 修改用户个人信息
     * @param user
     * @return
     */
    @Override
    public ServerResponse<String> updataUserInfo(User user) {
        int result = userMapper.updateByPrimaryKeySelective(user);
        if (result > 0) {
            return ServerResponse.createBySuccess("更新个人信息成功");
        }
        return ServerResponse.createByError("服务器内部错误");
    }
}
