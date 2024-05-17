package org.example.gateway.core.helper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.example.gateway.common.config.PredicateConfig;
import org.example.gateway.common.config.Rule;
import org.example.gateway.common.constants.RuleConst;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RuleHelper {

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
            rule.setRetry(ruleJsonObj.getInteger(RuleConst.RETRY));
            // 解析predicate配置
            Set<PredicateConfig> predicateConfigs = new HashSet<>();
            JSONArray predicateConfigJsonArr = ruleJsonObj.getJSONArray(RuleConst.PREDICATE_CONFIGS);
            for(int j=0;j<predicateConfigJsonArr.size();j++) {
                JSONObject predicateConfJsonObj = predicateConfigJsonArr.getJSONObject(j);
                String predicateId = predicateConfJsonObj.getString(RuleConst.PREDICATE_ID);
                Class predicateConfType = PredicateHelper.getPredicateConfType(predicateId);
                predicateConfigs.add(PredicateHelper.getPredicateConf(predicateConfJsonObj,predicateConfType));
            }


        }
        return rules;
    }
}
