package org.example.gateway.config.center.nacos;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import org.example.gateway.common.config.Rule;
import org.example.gateway.config.center.api.ConfigCenter;
import org.example.gateway.config.center.api.RulesChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
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

    @Override
    public void subscribeRulesChange(RulesChangeListener listener) {
        try {
            final String config = configService.getConfig(DATA_ID, env, 5000);
            logger.info("config from nacos: {}", config);
            final List<Rule> rules = JSON.parseObject(config).getJSONArray("rules").toJavaList(Rule.class);
            listener.onRulesChange(rules);
            //监听配置的变化
            configService.addListener(DATA_ID, env, new Listener() {
                @Override
                public Executor getExecutor() {
                    return null;
                }

                @Override
                public void receiveConfigInfo(String s) {
                    logger.info("config from nacos: {}", s);
                    List<Rule> rules = JSON.parseObject(s).getJSONArray("rules").toJavaList(Rule.class);
                    listener.onRulesChange(rules);
                }
            });
        }catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }
}
