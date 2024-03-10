package org.example.gateway.core.filter;

import org.example.gateway.core.context.GatewayContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class GatewayFilterChain {

    private static final Logger logger = LoggerFactory.getLogger(GatewayFilterChain.class);

    private List<Filter> filters = new ArrayList<>();

    public GatewayFilterChain addFilter(Filter filter) {
        filters.add(filter);
        return this;
    }

    public GatewayFilterChain addFilterList(List<Filter> filters) {
        this.filters.addAll(filters);
        return this;
    }


    public GatewayContext doFilter(GatewayContext ctx) throws Exception {
        if(filters.isEmpty()) {
            return ctx;
        }
        try{
            for(Filter filter : filters) {
                filter.doFilter(ctx);
            }
        }catch (Exception e) {
            logger.error("执行过滤器发生异常,异常信息：{}",e.getMessage());
            throw e;
        }
        return ctx;
    }

}
