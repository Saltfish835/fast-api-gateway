package org.example.gateway.core.predicates;

import com.alibaba.fastjson.JSONObject;
import org.example.gateway.common.config.PredicateConfig;
import org.example.gateway.core.request.GatewayRequest;

public interface Predicate {


    /**
     * 将JSONObject对象转换成PredicateConfig对象
     * @param predicateConfJsonObj
     * @return
     */
    PredicateConfig toPredicateConfig(JSONObject predicateConfJsonObj);


    /**
     * 判断请求与predicate是否匹配
     * @param gatewayRequest
     * @param predicateConfig
     * @return
     */
    Boolean isMatch(GatewayRequest gatewayRequest, PredicateConfig predicateConfig);
}
