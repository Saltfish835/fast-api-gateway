package org.example.gateway.core.helper;

import com.alibaba.fastjson.JSONObject;
import org.example.gateway.common.config.FilterConfig;
import org.example.gateway.core.ConfigLoader;
import org.example.gateway.core.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

public class FilterHelper {

    private static final Logger logger = LoggerFactory.getLogger(FilterHelper.class);


    // TODO 后续使用Caffine来缓存
    /**
     * 缓存实例
     */
    private static final ConcurrentHashMap<String, Object> instanceMap = new ConcurrentHashMap<>();

    /**
     * 缓存方法
     */
    private static final ConcurrentHashMap<String, Method> methodMap = new ConcurrentHashMap<>();

    /**
     * 根据filter id 获取 filter类型
     * @param filterId
     * @return
     */
    public static Class getFilterType(String filterId) {
        final Filter filter = ConfigLoader.getConfig().getFilterMap().get(filterId);
        return filter == null ? null : filter.getClass();
    }


    /**
     * 将JSONObject类型对象转换成FilterConfig对象
     * @param filterId
     * @param filterConfJsonObj
     * @return
     */
    public static FilterConfig getFilterConfig(String filterId, JSONObject filterConfJsonObj) {
        FilterConfig filterConfig = null;
        try{
            // 获取对应的Filter的类型
            final Class filterType = getFilterType(filterId);
            if(filterType == null) {
                throw new RuntimeException("filter not found, filterId:" + filterId);
            }
            // 获取filter对象
            Object instance = null;
            if(instanceMap.get(filterId) != null) {
                instance = instanceMap.get(filterId);
            }else {
                instance = filterType.newInstance();
                instanceMap.put(filterId, instance);
            }
            // 获取toFilterConfig方法
            Method toFilterConfig = null;
            if(methodMap.get(filterId) != null) {
                toFilterConfig = methodMap.get(filterId);
            }else {
                toFilterConfig = filterType.getMethod("toFilterConfig", JSONObject.class);
                methodMap.put(filterId, toFilterConfig);
            }
            // 使用filter对象调用toFilterConfig方法
            filterConfig = (FilterConfig)toFilterConfig.invoke(instance, filterConfJsonObj);
        }catch (Exception e) {
            logger.error("getFilterConfig error",e);
        }
        return filterConfig;
    }
}
