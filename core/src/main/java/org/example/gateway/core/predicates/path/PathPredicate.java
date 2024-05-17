package org.example.gateway.core.predicates.path;

import com.alibaba.fastjson.JSONObject;
import org.example.gateway.common.config.PredicateConfig;
import org.example.gateway.common.constants.PredicateConst;
import org.example.gateway.core.predicates.Predicate;
import org.example.gateway.core.predicates.PredicateAspect;
import org.example.gateway.core.request.GatewayRequest;

import java.util.ArrayList;
import java.util.List;

@PredicateAspect(id = PredicateConst.PATH_PREDICATE_ID, name = PredicateConst.PATH_PREDICATE_NAME)
public class PathPredicate implements Predicate {


    @Override
    public PredicateConfig toPredicateConfig(JSONObject predicateConfJsonObj) {
        final PathPredicateConfig pathPredicateConfig = new PathPredicateConfig();
        pathPredicateConfig.setId(predicateConfJsonObj.getString("id"));
        List<String> values = new ArrayList<>();
        predicateConfJsonObj.getJSONArray("values").stream().forEach(item -> {
            values.add((String)item);
        });
        pathPredicateConfig.setValues(values);
        return pathPredicateConfig;
    }


    @Override
    public Boolean isMatch(GatewayRequest gatewayRequest, PredicateConfig predicateConfig) {
        // 当前请求路径
        String reqPath = gatewayRequest.getPath();
        // 当前配置路径
        List<String> paths = ((PathPredicateConfig) predicateConfig).getValues();
        for(String path : paths) {
            // 如果当前用户请求路径是以此配置路径开头，说明匹配上了
            if(reqPath.startsWith(path)) {
                return true;
            }
        }
        return false;
    }
}
