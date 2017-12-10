package com.mmall.dao;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    int checkUsername(String username);

    User selectLogin(@Param("username") String username, @Param("password") String password);

    int checkUserEmail(String email);

    String forgetGetQuestion(String username);

    int forgetCheckAnswer(@Param("username") String username, @Param("answer") String answer, @Param("question") String question);

    int resetPassword(@Param("username") String username, @Param("password") String passwordNew);

    int updatePassword(@Param("username") String username,@Param("passwordOld")String passwordOld, @Param("passwordNew") String passwordNew);
}