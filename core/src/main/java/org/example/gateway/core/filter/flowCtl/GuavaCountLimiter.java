package org.example.gateway.core.filter.flowCtl;

import com.google.common.util.concurrent.RateLimiter;
import org.apache.commons.lang3.StringUtils;
import org.example.gateway.common.constants.BasicConst;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 单机限流
 */
public class GuavaCountLimiter {

    private RateLimiter rateLimiter;

    private double maxPermits;

    public static final ConcurrentHashMap<String, GuavaCountLimiter> resourceRateLimiterMap = new ConcurrentHashMap<>();

    public GuavaCountLimiter(double maxPermits) {
        this.maxPermits = maxPermits;
        this.rateLimiter = RateLimiter.create(maxPermits);
    }

    public GuavaCountLimiter(double maxPermits, long warmUpPeriodAsSecond) {
        this.maxPermits = maxPermits;
        this.rateLimiter = RateLimiter.create(maxPermits, warmUpPeriodAsSecond, TimeUnit.SECONDS);
    }

    public static GuavaCountLimiter getInstance(String serviceId, FlowCtlFilterConfig flowCtlConfig) {
        if(StringUtils.isEmpty(serviceId) || flowCtlConfig == null || StringUtils.isEmpty(flowCtlConfig.getValue()) ||
                StringUtils.isEmpty(flowCtlConfig.getType()) || flowCtlConfig.getConfig() == null) {
            return null;
        }
        final String key = new StringBuffer().append(serviceId).append(BasicConst.DIT_SEPARATOR).append(flowCtlConfig.getValue()).toString();
        GuavaCountLimiter guavaCountLimiter = resourceRateLimiterMap.get(key);
        if(guavaCountLimiter == null) {
            // 每秒产生多少个令牌
            // TODO 动态修改配置后，此处缓存不会更新
            guavaCountLimiter = new GuavaCountLimiter(flowCtlConfig.getConfig().getPermits()/flowCtlConfig.getConfig().getDuration());
            resourceRateLimiterMap.putIfAbsent(key, guavaCountLimiter);
        }
        return guavaCountLimiter;
    }


    public boolean acquire(int permits) {
        final boolean success = rateLimiter.tryAcquire(permits);
        if(success) {
            return true;
        }else {
            return false;
        }
    }

}
