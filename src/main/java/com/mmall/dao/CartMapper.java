package com.mmall.dao;

import com.mmall.pojo.Cart;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CartMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Cart record);

    int insertSelective(Cart record);

    Cart selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Cart record);

    int updateByPrimaryKey(Cart record);

    List<Cart> selectListByUserId(Integer userId);

    int checkAllChecked(Integer userId);

    Cart selectCartByUserIdAndProductId(@Param("userId") Integer userId,@Param("productId")  Integer productId);

    int deleteByUserIdAndProductId(@Param("userId")Integer userId, @Param("productIds")List<Integer> productIds);

    int selectCountByUserId(Integer userId);

    List<Integer> selectAllIdByUserId(Integer userId);

    List<Cart> selectByUserId(Integer userId);

    int deleteBatch(@Param("productIds")List<Integer> productIds,@Param("userId") Integer userId);
}