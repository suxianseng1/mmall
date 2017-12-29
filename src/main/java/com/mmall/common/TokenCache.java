package com.mmall.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2017/12/9.
 */
public class TokenCache {

    private static final Logger logger = LoggerFactory.getLogger(TokenCache.class);

    public static final String TOKEN = "token_";

    private static LoadingCache<String, String> loadingCache = CacheBuilder.newBuilder().initialCapacity(1000).maximumSize(10000).expireAfterAccess(12, TimeUnit.HOURS).build(new CacheLoader<String, String>() {
        @Override
        public String load(String key) throws Exception {
            return "null";
        }
    });

    public static void put(String key,String value){
        loadingCache.put(key, value);
    }

    public static String getToken(String key){
        try {
            String value = loadingCache.get(key);
            if(value.equals("null")){
                return null;
            }
            return value;
        } catch (ExecutionException e) {
          logger.error("localCache get error",e);
        }
        return null;
    }
}
