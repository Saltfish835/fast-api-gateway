package org.example.gateway.core.filter.flowCtl;

import org.example.gateway.core.redis.JedisUtil;

/**
 * 使用Redis实现分布式限流
 */
public class RedisCountLimiter {

    private static final int SUCCESS_RESULT = 1;
    private static final int FAILED_RESULT = 0;

    protected JedisUtil jedisUtil;

    public RedisCountLimiter(JedisUtil jedisUtil) {
        this.jedisUtil = jedisUtil;
    }


    public boolean doFlowCtl(String key, int limit, int expire) {
        try{
            final Object result = jedisUtil.executeScript(key, limit, expire);
            if(result == null) {
                return true;
            }
            final Long value = Long.valueOf(result.toString());
            if(value == FAILED_RESULT) {
                return false;
            }
            return true;
        }catch (Exception e) {
            throw new RuntimeException("分布式限流发生错误");
        }
    }

}
