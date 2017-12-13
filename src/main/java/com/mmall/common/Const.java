package com.mmall.common;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Created by Administrator on 2017/12/8.
 */
public class Const {

    public static String CURRENT_USER = "currentUser";
    public static String USERNAME = "username";
    public static String EMAIL = "email";
    public interface Role{
        /**
         * 普通用户
         */
        int ROLE_CUSTOMER = 0;

        /**
         * 管理员
         */
        int ROLE_ADMIN = 1;
    }

    public interface ProductListOrderBy{
        Set<String> PRICE_ASC_DESC = Sets.newHashSet("price_desc","price_asc");
    }
}
