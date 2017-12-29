package com.mmall.service.impl;

import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 2017/12/11.
 */
@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 获取该类别下的所有子类别信息
     * @param categoryId 该类别的ID
     * @return
     */
    @Override
    public ServerResponse<Set<Category>> getCategory(int categoryId) {
        Set<Category> categories = categoryMapper.selectCategoryChildById(categoryId);
        if (categories == null || categories.size() == 0) {
            return ServerResponse.createByErrorMessage("未找到该品类");
        }
        return ServerResponse.createBySuccess(categories);
    }

    /**
     * 添加类别
     * @param parentId 父节点ID
     * @param categoryName 类别名称
     * @return
     */
    @Override
    @Transactional
    public ServerResponse<String> addCategory(int parentId, String categoryName) {
        if(checkCategoryNameAndId(categoryName,parentId)){
            return ServerResponse.createByErrorMessage("此类别下已有该类别名，请更换类别名再进行添加");
        }
        Category category = new Category();
        category.setParentId(parentId);
        category.setName(categoryName);
        int result = categoryMapper.insertSelective(category);
        if (result == 0) {
            return ServerResponse.createByErrorMessage("添加品类失败");
        }
        return ServerResponse.createBySuccess("添加品类成功");
    }

    /**
     * 修改类别名称
     * @param categoryName 新的类别名
     * @param categoryId 类别ID
     * @return
     */
    @Override
    @Transactional
    public ServerResponse<String> setCategoryName(String categoryName, int categoryId) {
        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);
        int result = categoryMapper.updateByPrimaryKeySelective(category);
        if (result == 0) {
            return ServerResponse.createByErrorMessage("更新品类名字失败");
        }
        return ServerResponse.createBySuccess("更新品类名字成功");
    }

    /**
     * 查询该节点下的所有子节点，然后递归查询各个节点的子节点
     * 递归：
     * Set集合
     * 每次都根据id来查询，把查询到的数据放到集合中
     *
     * @param categoryId
     * @return
     */
    @Override
    public void getDeepCateory(int categoryId,List<Integer> categoryIds) {
        Set<Integer> categoryIdsTemp = categoryMapper.selectDeepCategoryId(categoryId);
        if(categoryIdsTemp == null || categoryIdsTemp.size() == 0){
            return;
        }
        categoryIds.addAll(categoryIdsTemp);
        Iterator<Integer> iterator = categoryIdsTemp.iterator();
        while(iterator.hasNext()){
            getDeepCateory(iterator.next(),categoryIds);
        }
    }

    /**
     * 检查插入时 数据库中有没有存在 一样的类别
     * @param categoryName 类别名
     * @param parentId 父节点ID
     * @return
     */
    private boolean checkCategoryNameAndId(String categoryName ,int parentId){
        int result = categoryMapper.checkCategoryNameAndId(categoryName,parentId);
        return result != 0;
    }

}
