package com.mmall.controller.backend;

import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpSession;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 2017/12/11.
 */
@Controller
@RequestMapping("/manage/category/")
public class CategoryManageController {

    @Autowired
    private ICategoryService categoryService;

    @RequestMapping(value = "get_category.do", method = RequestMethod.GET)
    @ResponseBody
    private ServerResponse<Set<Category>> get_category(HttpSession session, @RequestParam(value = "categoryId", defaultValue = "0") int categoryId) {
        return categoryService.getCategory(categoryId);
    }


    @RequestMapping(value = "add_category.do", method = RequestMethod.POST)
    @ResponseBody
    private ServerResponse<String> addCategory(@RequestParam(value = "parentId", defaultValue = "0") int parentId, String categoryName) {
        return categoryService.addCategory(parentId, categoryName);
    }

    @RequestMapping(value = "set_category_name.do", method = RequestMethod.GET)
    @ResponseBody
    private ServerResponse<String> setCategoryName(String categoryName, int categoryId) {
        return categoryService.setCategoryName(categoryName, categoryId);
    }


    @RequestMapping(value = "get_deep_category.do", method = RequestMethod.GET)
    @ResponseBody
    private ServerResponse<List<Integer>> getDeepCateory(Integer categoryId) {
        List<Integer> categoryIds = Lists.newArrayList();
        categoryIds.add(categoryId);
        categoryService.getDeepCateory(categoryId,categoryIds);
        if(categoryIds.size() == 0){
            return ServerResponse.createByErrorMessage("该类别下没有子类别");
        }
        return ServerResponse.createBySuccess(categoryIds);
    }

}
