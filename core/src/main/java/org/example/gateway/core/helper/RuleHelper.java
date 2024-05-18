package org.example.gateway.core.helper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.example.gateway.common.config.DynamicConfigManager;
import org.example.gateway.common.config.FilterConfig;
import org.example.gateway.common.config.PredicateConfig;
import org.example.gateway.common.config.Rule;
import org.example.gateway.common.constants.RuleConst;
import org.example.gateway.core.request.GatewayRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RuleHelper {

    private static final Logger logger = LoggerFactory.getLogger(RuleHelper.class);

    /**
     * 解析配置
     * @param configJsonStr
     * @return
     */
    public static List<Rule> parseRule(String configJsonStr) {
        List<Rule> rules = new ArrayList<>();
        JSONArray ruleJsonArr = JSON.parseObject(configJsonStr).getJSONArray(RuleConst.RULES);
        for(int i=0; i<ruleJsonArr.size(); i++) {
            Rule rule = new Rule();
            JSONObject ruleJsonObj = ruleJsonArr.getJSONObject(i);
            // 设置基本信息
            rule.setId(ruleJsonObj.getString(RuleConst.ID));
            rule.setServiceId(ruleJsonObj.getString(RuleConst.SERVICE_ID));
            rule.setVersion(ruleJsonObj.getString(RuleConst.VERSION));
            rule.setProtocol(ruleJsonObj.getString(RuleConst.PROTOCOL));
            // 解析可选配置
            if(ruleJsonObj.containsKey(RuleConst.RETRY)) { // retry是可选配置
                rule.setRetry(ruleJsonObj.getInteger(RuleConst.RETRY));
            }
            if(ruleJsonObj.containsKey(RuleConst.BREAKER)) { // breaker是可选配置
                final JSONObject breakerJsonObj = ruleJsonObj.getJSONObject(RuleConst.BREAKER);
                final Integer breakerTimeoutMs = breakerJsonObj.getInteger(RuleConst.BREAKER_TIMEOUT_MS);
                final Integer breakerThreadCoreSize = breakerJsonObj.getInteger(RuleConst.BREAKER_THREAD_CORE_SIZE);
                final String breakerFallbackResponse = breakerJsonObj.getString(RuleConst.BREAKER_FALLBACK_RESPONSE);
                rule.setBreaker(new Rule.Breaker(breakerThreadCoreSize, breakerFallbackResponse, breakerTimeoutMs));
            }
            // 解析predicate配置
            Set<PredicateConfig> predicateConfigs = new HashSet<>();
            JSONArray predicateConfigJsonArr = ruleJsonObj.getJSONArray(RuleConst.PREDICATE_CONFIGS);
            for(int j=0; j<predicateConfigJsonArr.size(); j++) {
                JSONObject predicateConfJsonObj = predicateConfigJsonArr.getJSONObject(j);
                String predicateId = predicateConfJsonObj.getString(RuleConst.PREDICATE_ID);
                final PredicateConfig predicateConfig = PredicateHelper.getPredicateConfig(predicateId, predicateConfJsonObj);
                if(predicateConfig == null) {
                    throw new RuntimeException("parse predicateConfJsonObj error");
                }
                predicateConfigs.add(predicateConfig);
            }
            rule.setPredicateConfigs(predicateConfigs);
            // 解析filter配置
            Set<FilterConfig> filterConfigs = new HashSet<>();
            final JSONArray filterConfigJsonArr = ruleJsonObj.getJSONArray(RuleConst.FILTER_CONFIGS);
            for(int j=0; j<filterConfigJsonArr.size(); j++) {
                final JSONObject filterConfigJsonObj = filterConfigJsonArr.getJSONObject(j);
                final String filterId = filterConfigJsonObj.getString(RuleConst.FILTER_ID);
                final FilterConfig filterConfig = FilterHelper.getFilterConfig(filterId, filterConfigJsonObj);
                if(filterConfig == null) {
                    throw new RuntimeException("parse filterConfigJsonObj error");
                }
                filterConfigs.add(filterConfig);
            }
            rule.setFilterConfigs(filterConfigs);
            rules.add(rule);
        }
        return rules;
    }


    /**
     * 获取此请求对应的规则
     * @param gatewayRequest
     * @return
     */
    public static Rule getRule(GatewayRequest gatewayRequest){
        Rule rule = null;
        try {
            ConcurrentHashMap<String, Rule> ruleMap = DynamicConfigManager.getInstance().getRuleMap();
            for(Rule ruleTmp : ruleMap.values()) {
                boolean flag = true;
                // 需要满足所有predicate，才能认为此request满足此rule
                Set<PredicateConfig> predicateConfigs = ruleTmp.getPredicateConfigs();
                for(PredicateConfig predicateConfig : predicateConfigs) {
                    final String predicateId = predicateConfig.getId();
                    // 判断请求是否满足此predicate
                    final Boolean isMatch = PredicateHelper.getMatchResult(predicateId, gatewayRequest, predicateConfig);
                    if(isMatch == null) {
                        throw new RuntimeException("cant obtain match result, predicateId:"+predicateId+", uri:"+ gatewayRequest.getUri());
                    }
                    // 如果有一条predicate不能匹配，说明此request不适合此rule
                    if(isMatch == false) {
                        flag = false;
                        break;
                    }
                }
                if(flag) {
                    rule = ruleTmp;
                }
            }
        }catch (Exception e) {
            logger.error("getRule error",e);
        }
        return rule;
    }
}
