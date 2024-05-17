package org.example.gateway.core.helper;

import com.alibaba.fastjson.JSONObject;
import org.example.gateway.common.config.PredicateConfig;
import org.example.gateway.core.ConfigLoader;
import org.example.gateway.core.predicates.Predicate;
import org.example.gateway.core.request.GatewayRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class PredicateHelper {

    private static final Logger logger = LoggerFactory.getLogger(PredicateHelper.class);

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
            // 通过反射调用方法
            final Object instance = predicateType.newInstance();
            final Method toPredicateConfig = predicateType.getMethod("toPredicateConfig", JSONObject.class);
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
            // 通过反射调用方法
            final Object instance = predicateType.newInstance();
            final Method isMatch = predicateType.getMethod("isMatch", GatewayRequest.class, PredicateConfig.class);
            matchResult = (Boolean) isMatch.invoke(instance, gatewayRequest, predicateConfig);
        }catch (Exception e) {
            logger.error("getMatchResult error", e);
        }
        return matchResult;
    }
}
