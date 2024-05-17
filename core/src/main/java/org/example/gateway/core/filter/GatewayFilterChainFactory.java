package org.example.gateway.core.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.commons.lang3.StringUtils;
import org.example.gateway.common.config.Rule;
import org.example.gateway.common.config.FilterConfig;
import org.example.gateway.common.constants.FilterConst;
import org.example.gateway.core.ConfigLoader;
import org.example.gateway.core.context.GatewayContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class GatewayFilterChainFactory implements FilterFactory{

    private static final Logger logger = LoggerFactory.getLogger(GatewayFilterChainFactory.class);

    /**
     * 缓存路由规则对应的过滤器链
     */
    private static Cache<String,GatewayFilterChain> chainCache = Caffeine.newBuilder().recordStats()
            .expireAfterWrite(10, TimeUnit.MINUTES).build(); // 缓存的过期时间10min

    private static class SingletonInstance {
        private static final GatewayFilterChainFactory INSTANCE  = new GatewayFilterChainFactory();
    }

    public static GatewayFilterChainFactory getInstance() {
        return SingletonInstance.INSTANCE;
    }




    /**
     * 构建过滤器链
     * @param ctx
     * @return
     * @throws Exception
     */
    @Override
    public GatewayFilterChain buildFilterChain(GatewayContext ctx) throws Exception {
        // 先从缓存中拿， 如果不存在则构建
        return chainCache.get(ctx.getRule().getId(), k -> {
            return doBuildFilterChain(ctx.getRule());
        });
    }


    private GatewayFilterChain doBuildFilterChain(Rule rule) {
        final GatewayFilterChain gatewayFilterChain = new GatewayFilterChain();
        final ArrayList<Filter> filters = new ArrayList<>();
        // 监控过滤器默认添加
        filters.add(getFilterInfo(FilterConst.MONITOR_FILTER_ID));
        filters.add(getFilterInfo(FilterConst.MONITOR_END_FILTER_ID));
        if(rule != null) {
            for(FilterConfig filterConfig : rule.getFilterConfigs()) {
                if(filterConfig == null) {
                    continue;
                }
                final String filterConfigId = filterConfig.getId();
                if(StringUtils.isNotEmpty(filterConfigId) && getFilterInfo(filterConfigId) != null) {
                    final Filter filter = getFilterInfo(filterConfigId);
                    filters.add(filter);
                }
            }
        }
        // 路由过滤器默认添加
        filters.add(getFilterInfo(FilterConst.ROUTER_FILTER_ID));
        filters.sort(Comparator.comparingInt(Filter::getOrder)); // 给所有过滤器排个序，使过滤器有序执行
        gatewayFilterChain.addFilterList(filters);
        return gatewayFilterChain;
    }


    @Override
    public Filter getFilterInfo(String filterId){
        return ConfigLoader.getConfig().getFilterMap().get(filterId);
    }
}
