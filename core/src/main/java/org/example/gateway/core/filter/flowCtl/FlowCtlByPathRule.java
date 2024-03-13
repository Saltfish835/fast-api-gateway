package org.example.gateway.core.filter.flowCtl;


import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.example.gateway.common.config.Rule;
import org.example.gateway.common.constants.FilterConst;
import org.example.gateway.core.redis.JedisUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 根据路径进行限流
 */
public class FlowCtlByPathRule implements IGatewayFlowCtlRule{

    private String serviceId;

    private String path;

    private RedisCountLimiter redisCountLimiter;

    private static final String LIMIT_MESSAGE ="您的请求过于频繁,请稍后重试";

    private static ConcurrentHashMap<String, FlowCtlByPathRule> servicePathMap = new ConcurrentHashMap<>();

    public FlowCtlByPathRule(String serviceId, String path, RedisCountLimiter redisCountLimiter) {
        this.serviceId = serviceId;
        this.path = path;
        this.redisCountLimiter = redisCountLimiter;
    }

    public static FlowCtlByPathRule getInstance(String serviceId, String path) {
        final StringBuffer stringBuffer = new StringBuffer();
        final String key = stringBuffer.append(serviceId).append(".").append(path).toString();
        FlowCtlByPathRule flowCtlByPathRule = servicePathMap.get(key);
        if(flowCtlByPathRule ==  null) {
            flowCtlByPathRule = new FlowCtlByPathRule(serviceId, path, new RedisCountLimiter(new JedisUtil()));
            servicePathMap.put(key, flowCtlByPathRule);
        }
        return flowCtlByPathRule;
    }

    @Override
    public void doFlowCtlFilter(Rule.FlowCtlConfig flowCtlConfig, String serviceId) {
        if(flowCtlConfig == null || StringUtils.isEmpty(serviceId) || StringUtils.isEmpty(flowCtlConfig.getConfig())) {
            // 用户未配置流控
            return;
        }
        // 拿到流控的配置
        Map<String, Integer> configMap = JSON.parseObject(flowCtlConfig.getConfig(), Map.class);
        if(!configMap.containsKey(FilterConst.FLOW_CTL_LIMIT_DURATION) || !configMap.containsKey(FilterConst.FLOW_CTL_LIMIT_DURATION)) {
            // 没有这两项配置则不进行流控
            return;
        }
        final double duration = configMap.get(FilterConst.FLOW_CTL_LIMIT_DURATION);
        final double permits = configMap.get(FilterConst.FLOW_CTL_LIMIT_PERMITS);
        // 当前限流是否通过
        boolean flag = true;
        // 分布式还是单机的流控
        if(FilterConst.FLOW_CTL_MODEL_DISTRIBUTED.equalsIgnoreCase(flowCtlConfig.getModel())) {
            // 分布式限流
            final String key = new StringBuffer().append(serviceId).append(".").append(path).toString();
            flag = redisCountLimiter.doFlowCtl(key, (int)permits, (int)duration);
        }else {
            // 单机限流
            final GuavaCountLimiter guavaCountLimiter = GuavaCountLimiter.getInstance(serviceId, flowCtlConfig);
            if(guavaCountLimiter == null) {
                throw new RuntimeException("获取单机限流工具为空");
            }
            // 每秒能访问多少次
            final double count = Math.ceil(permits / duration);
            flag = guavaCountLimiter.acquire((int) count);
        }
        if(!flag) {
            // 如果限流不通过，直接抛异常
            throw new RuntimeException(LIMIT_MESSAGE);
        }
    }
}
