package org.example.gateway.core.filter;

import org.example.gateway.core.context.GatewayContext;

public interface FilterFactory {


    /**
     * 构建过滤器链
     * @param ctx
     * @return
     * @throws Exception
     */
    GatewayFilterChain buildFilterChain(GatewayContext ctx) throws Exception;

    /**
     * 通过过滤器ID获取过滤器
     * @param filterId
     * @param <T>
     * @return
     * @throws Exception
     */
    <T> T getFilterInfo(String filterId) throws Exception;
}
