package org.example.gateway.core.filter.flowCtl;

import com.google.common.util.concurrent.RateLimiter;
import org.apache.commons.lang3.StringUtils;
import org.example.gateway.common.config.Rule;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 单机限流
 */
public class GuavaCountLimiter {

    private RateLimiter rateLimiter;

    private double maxPermits;

    public static ConcurrentHashMap<String, GuavaCountLimiter> resourceRateLimiterMap = new ConcurrentHashMap<>();

    public GuavaCountLimiter(double maxPermits) {
        this.maxPermits = maxPermits;
        this.rateLimiter = RateLimiter.create(maxPermits);
    }

    public GuavaCountLimiter(double maxPermits, long warmUpPeriodAsSecond) {
        this.maxPermits = maxPermits;
        this.rateLimiter = RateLimiter.create(maxPermits, warmUpPeriodAsSecond, TimeUnit.SECONDS);
    }

    public static GuavaCountLimiter getInstance(String serviceId, Rule.FlowCtlConfig flowCtlConfig) {
        if(StringUtils.isEmpty(serviceId) || flowCtlConfig == null || StringUtils.isEmpty(flowCtlConfig.getValue()) ||
                StringUtils.isEmpty(flowCtlConfig.getType()) || StringUtils.isEmpty(flowCtlConfig.getConfig())) {
            return null;
        }
        final String key = new StringBuffer().append(serviceId).append(".").append(flowCtlConfig.getValue()).toString();
        GuavaCountLimiter guavaCountLimiter = resourceRateLimiterMap.get(key);
        if(guavaCountLimiter == null) {
            // 每秒产生50个令牌
            guavaCountLimiter = new GuavaCountLimiter(50);
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
