package com.mmall.dao;

import com.mmall.pojo.Category;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

public interface CategoryMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Category record);

    int insertSelective(Category record);

    Category selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Category record);

    int updateByPrimaryKey(Category record);

    Set<Category> selectCategoryChildById(int categoryId);

    Set<Integer> selectDeepCategoryId(int categoryId);

    int checkCategoryNameAndId(@Param("categoryName") String categoryName, @Param("parentId") int parentId);
}