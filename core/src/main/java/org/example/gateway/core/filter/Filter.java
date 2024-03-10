package org.example.gateway.core.filter;


import org.example.gateway.core.context.GatewayContext;

/**
 * 过滤器的顶级接口
 */
public interface Filter {

    void doFilter(GatewayContext ctx) throws Exception;

    default int getOrder() {
        final FilterAspect filterAspect = this.getClass().getAnnotation(FilterAspect.class);
        if(filterAspect != null) {
            return filterAspect.order();
        }
        return Integer.MAX_VALUE;
    }
}
