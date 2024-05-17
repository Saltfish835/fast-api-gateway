package org.example.gateway.config.center.nacos;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import org.example.gateway.common.config.Rule;
import org.example.gateway.common.config.FilterConfig;
import org.example.gateway.common.config.PredicateConfig;
import org.example.gateway.config.center.api.ConfigCenter;
import org.example.gateway.config.center.api.RulesChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

public class NacosConfigCenter implements ConfigCenter {

    private static final Logger logger = LoggerFactory.getLogger(NacosConfigCenter.class);

    private static final String DATA_ID = "api-gateway";

    private String serverAddr;

    private String env;

    private ConfigService configService;

    public NacosConfigCenter() {
    }

    @Override
    public void init(String serverAddr, String env) {
        this.serverAddr = serverAddr;
        this.env = env;
        try{
            configService = NacosFactory.createConfigService(serverAddr);
        }catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 把规则从配置中心拉取下来保存到本地
     * 当配置中心的规则发生变化，同步更新到本地
     * @param listener
     */
    @Override
    public void subscribeRulesChange(RulesChangeListener listener) {
        try {
            final String config = configService.getConfig(DATA_ID, env, 5000);
            logger.info("get config from nacos: {}", config);
//            final List<Rule> rules = parseRule(config);
            listener.onRulesChange(config);
            //监听配置的变化
            configService.addListener(DATA_ID, env, new Listener() {
                @Override
                public Executor getExecutor() {
                    return null;
                }

                @Override
                public void receiveConfigInfo(String s) {
                    logger.info("get config from nacos: {}", s);
//                    List<Rule> rules = parseRule(s);
                    listener.onRulesChange(s);
                }
            });
        }catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 将配置中心中存储的json字符串配置解析成配置规则对象
     * @param config
     * @return
     */
    private List<Rule> parseRule(String config) {
        List<Rule> rules = new ArrayList<>();
        JSONArray ruleJsonArr = JSON.parseObject(config).getJSONArray("rules");
        for(int i=0;i<ruleJsonArr.size();i++) {
            JSONObject ruleJsonObj = ruleJsonArr.getJSONObject(i);
            Rule rule = new Rule();
            // 设置基本信息
            rule.setId(ruleJsonObj.getString("id"));
            rule.setServiceId(ruleJsonObj.getString("serviceId"));
            rule.setVersion(ruleJsonObj.getString("version"));
            rule.setProtocol(ruleJsonObj.getString("protocol"));
            rule.setRetry(ruleJsonObj.getInteger("retry"));
            // 解析predicate配置
            Set<PredicateConfig> predicateConfigs = new HashSet<>();
            JSONArray predicateConfigJsonArr = ruleJsonObj.getJSONArray("predicateConfigs");
            for(int j=0;j<predicateConfigJsonArr.size();j++) {
                JSONObject predicateConfJsonObj = predicateConfigJsonArr.getJSONObject(j);
                String predicateId = predicateConfJsonObj.getString("id");
                if(predicateId.equals("path")) {
                    PathPredicateConfig pathPredicateConfig = predicateConfigJsonArr.getObject(j, PathPredicateConfig.class);
                    predicateConfigs.add(pathPredicateConfig);
                    continue;
                }else if(predicateId.equals("method")) {
                    MethodPredicateConfig methodPredicateConfig = predicateConfigJsonArr.getObject(j, MethodPredicateConfig.class);
                    predicateConfigs.add(methodPredicateConfig);
                    continue;
                }else {
                    throw new RuntimeException("不支持的predicate");
                }
            }
            rule.setPredicateConfigs(predicateConfigs);
            // 解析filter配置
            Set<FilterConfig> filterConfigs = new HashSet<>();
            JSONArray filterConfigJsonArr = ruleJsonObj.getJSONArray("filterConfigs");
            for(int j=0;j< filterConfigJsonArr.size();j++) {
                JSONObject filterConfigJsonObj = filterConfigJsonArr.getJSONObject(j);
                String filterId = filterConfigJsonObj.getString("id");
                if(filterId.equals("stripPrefix")) {
                    StripPrefixFilterConfig prefixFilterConfig = filterConfigJsonArr.getObject(j, StripPrefixFilterConfig.class);
                    filterConfigs.add(prefixFilterConfig);
                    continue;
                }else if(filterId.equals("prefixPath")) {
                    PrefixPathFilterConfig prefixPathFilterConfig = filterConfigJsonArr.getObject(j, PrefixPathFilterConfig.class);
                    filterConfigs.add(prefixPathFilterConfig);
                    continue;
                }else if(filterId.equals("loadBalance")) {
                    LoadBalanceFilterConfig loadBalanceFilterConfig = filterConfigJsonArr.getObject(j, LoadBalanceFilterConfig.class);
                    filterConfigs.add(loadBalanceFilterConfig);
                    continue;
                }else {
                    throw new RuntimeException("不支持的filter");
                }
            }
            rule.setFilterConfigs(filterConfigs);
            rules.add(rule);
        }
        return rules;
    }
}
