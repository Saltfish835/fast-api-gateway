package org.example.gateway.core.helper;

import com.alibaba.fastjson.JSONObject;
import org.example.gateway.common.config.PredicateConfig;
import org.example.gateway.core.ConfigLoader;
import org.example.gateway.core.predicates.Predicate;
import org.example.gateway.core.request.GatewayRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

public class PredicateHelper {

    private static final Logger logger = LoggerFactory.getLogger(PredicateHelper.class);

    // TODO 后续使用Caffine来缓存
    /**
     * 缓存实例
     */
    private static final ConcurrentHashMap<String,Object> instanceMap = new ConcurrentHashMap<>();

    /**
     * 缓存toPredicateConfig方法
     */
    private static final ConcurrentHashMap<String,Method> toPredicateConfigMethodMap = new ConcurrentHashMap<>();

    /**
     * 缓存isMatch方法
     */
    private static final ConcurrentHashMap<String,Method> isMatchMethodMap = new ConcurrentHashMap<>();


    /**
     * 根据predicate id 获取predicate类型
     * @param predicateId
     * @return
     */
    public static Class getPredicateType(String predicateId) {
        final Predicate predicate = ConfigLoader.getConfig().getPredicateMap().get(predicateId);
        return predicate == null ? null : predicate.getClass();
    }


    /**
     * 将JSONObject类型对象转换成PredicateConfig对象
     * @param predicateId
     * @param predicateConfJsonObj
     * @return
     */
    public static PredicateConfig getPredicateConfig(String predicateId, JSONObject predicateConfJsonObj){
        PredicateConfig predicateConfig = null;
        try {
            // 获取对应的Predicate类型
            final Class predicateType = getPredicateType(predicateId);
            if(predicateType == null) {
                throw new RuntimeException("predicate not found, predicateId:" + predicateId);
            }
            // 获取predicate对象
            Object instance = null;
            if(instanceMap.get(predicateId) != null) {
                instance = instanceMap.get(predicateId);
            }else {
                instance = predicateType.newInstance();
                instanceMap.put(predicateId,instance);
            }
            // 获取toPredicateConfig方法
            Method toPredicateConfig = null;
            if(toPredicateConfigMethodMap.get(predicateId) != null) {
                toPredicateConfig = toPredicateConfigMethodMap.get(predicateId);
            }else {
                toPredicateConfig = predicateType.getMethod("toPredicateConfig", JSONObject.class);
                toPredicateConfigMethodMap.put(predicateId, toPredicateConfig);
            }
            // 使用predicate对象调用toPredicateConfig方法
            predicateConfig = (PredicateConfig)toPredicateConfig.invoke(instance, predicateConfJsonObj);
        }catch (Exception e) {
            logger.error("getPredicateConfig error",e);
        }
        return predicateConfig;
    }


    /**
     * 判断请求是否满足此predicate
     * @param predicateId
     * @param gatewayRequest
     * @param predicateConfig
     * @return
     */
    public static Boolean getMatchResult(String predicateId, GatewayRequest gatewayRequest, PredicateConfig predicateConfig) {
        Boolean matchResult = null;
        try {
            // 获取对应的Predicate类型
            final Class predicateType = getPredicateType(predicateId);
            if(predicateType == null) {
                throw new RuntimeException("predicate not found, predicateId:" + predicateId);
            }
            // 获取predicate对象
            Object instance = null;
            if(instanceMap.get(predicateId) != null) {
                instance = instanceMap.get(predicateId);
            }else {
                instance = predicateType.newInstance();
                instanceMap.put(predicateId, instance);
            }
            // 获取isMatch方法
            Method isMatch = null;
            if(isMatchMethodMap.get(predicateId) != null) {
                isMatch = isMatchMethodMap.get(predicateId);
            }else {
                isMatch = predicateType.getMethod("isMatch", GatewayRequest.class, PredicateConfig.class);
                isMatchMethodMap.put(predicateId, isMatch);
            }
            // 使用predicate对象调用isMatch方法
            matchResult = (Boolean) isMatch.invoke(instance, gatewayRequest, predicateConfig);
        }catch (Exception e) {
            logger.error("getMatchResult error", e);
        }
        return matchResult;
    }
}
