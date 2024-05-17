package org.example.gateway.core.predicates.method;

import com.alibaba.fastjson.JSONObject;
import org.example.gateway.common.config.PredicateConfig;
import org.example.gateway.common.constants.PredicateConst;
import org.example.gateway.core.predicates.Predicate;
import org.example.gateway.core.predicates.PredicateAspect;
import org.example.gateway.core.predicates.path.PathPredicateConfig;
import org.example.gateway.core.request.GatewayRequest;

import java.util.ArrayList;
import java.util.List;

@PredicateAspect(id = PredicateConst.METHOD_PREDICATE_ID, name = PredicateConst.METHOD_PREDICATE_NAME)
public class MethodPredicate implements Predicate {

    @Override
    public PredicateConfig toPredicateConfig(JSONObject predicateConfJsonObj) {
        final MethodPredicateConfig methodPredicateConfig = new MethodPredicateConfig();
        methodPredicateConfig.setId(predicateConfJsonObj.getString("id"));
        ArrayList<String> values = new ArrayList<>();
        predicateConfJsonObj.getJSONArray("values").stream().forEach(item -> {
            values.add((String)item);
        });
        methodPredicateConfig.setValues(values);
        return methodPredicateConfig;
    }

    @Override
    public Boolean isMatch(GatewayRequest gatewayRequest, PredicateConfig predicateConfig) {
        String reqMethod = gatewayRequest.getHttpMethod().toString();
        List<String> methods = ((MethodPredicateConfig) predicateConfig).getValues();
        for(String method : methods) {
            // 如果当前用户请求方式与配置的请求方式一致，说明匹配上了
            if(reqMethod.equals(method)) {
                return true;
            }
        }
        return false;
    }
}
