package org.example.gateway.core.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.commons.lang3.StringUtils;
import org.example.gateway.common.config.Rule;
import org.example.gateway.common.constants.FilterConst;
import org.example.gateway.core.context.GatewayContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class GatewayFilterChainFactory implements FilterFactory{

    private static final Logger logger = LoggerFactory.getLogger(GatewayFilterChainFactory.class);

    private Map<String, Filter> processorFilterIdMap = new ConcurrentHashMap<>();

    private Cache<String,GatewayFilterChain> chainCache = Caffeine.newBuilder().recordStats()
            .expireAfterWrite(10, TimeUnit.MINUTES).build(); // 缓存的过期时间10min

    private static class SingletonInstance {
        private static final GatewayFilterChainFactory INSTANCE  = new GatewayFilterChainFactory();
    }

    public static GatewayFilterChainFactory getInstance() {
        return SingletonInstance.INSTANCE;
    }


    /**
     * 加载所有配置的filter
     */
    public GatewayFilterChainFactory() {
        final ServiceLoader<Filter> filterServiceLoader = ServiceLoader.load(Filter.class);
        for(Filter filter : filterServiceLoader) {
            final FilterAspect annotation = filter.getClass().getAnnotation(FilterAspect.class);
            logger.info("load filter success:{},{},{},{}",filter.getClass(), annotation.id(),annotation.name(),annotation.order());
            if(annotation != null) {
                String filterId = annotation.id();
                if(StringUtils.isEmpty(filterId)) {
                    filterId = filter.getClass().getName();
                }
                processorFilterIdMap.put(filterId, filter);
            }
        }
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
        // 添加监控过滤器
        filters.add(getFilterInfo(FilterConst.MONITOR_FILTER_ID));
        filters.add(getFilterInfo(FilterConst.MONITOR_END_FILTER_ID));
        if(rule != null) {
            for(Rule.FilterConfig filterConfig : rule.getFilterConfigs()) {
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
        // 添加路由过滤器
        filters.add(getFilterInfo(FilterConst.ROUTER_FILTER_ID));
        filters.sort(Comparator.comparingInt(Filter::getOrder));
        gatewayFilterChain.addFilterList(filters);
        return gatewayFilterChain;
    }


    @Override
    public Filter getFilterInfo(String filterId){
        return processorFilterIdMap.get(filterId);
    }
}
