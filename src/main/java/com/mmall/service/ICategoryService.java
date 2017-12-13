package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;

import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 2017/12/11.
 */
public interface ICategoryService {

    ServerResponse<Set<Category>> getCategory(int categoryId);

    ServerResponse<String> addCategory(int parentId,String categoryName);

    ServerResponse<String> setCategoryName(String categoryName,int categoryId);


    void getDeepCateory(int categoryId,List<Integer> categoryIds);
}
