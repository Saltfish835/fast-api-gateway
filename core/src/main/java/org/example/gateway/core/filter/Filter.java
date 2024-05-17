package org.example.gateway.core.filter;


import com.alibaba.fastjson.JSONObject;
import org.example.gateway.common.config.FilterConfig;
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


    /**
     * 将JSONObjet对象转换成FilterConfig对象
     * @param filterConfigJsonObj
     * @return
     */
    FilterConfig toFilterConfig(JSONObject filterConfigJsonObj);
}
