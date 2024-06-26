package org.example.gateway.config.center.nacos;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import org.example.gateway.config.center.api.ConfigCenter;
import org.example.gateway.config.center.api.RulesChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                    listener.onRulesChange(s);
                }
            });
        }catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

}
