package org.example.gateway.core.filter.loadbalance;

import com.alibaba.fastjson.JSONObject;
import org.example.gateway.common.config.FilterConfig;
import org.example.gateway.common.config.ServiceInstance;
import org.example.gateway.common.constants.FilterConst;
import org.example.gateway.common.enums.ResponseCode;
import org.example.gateway.common.exception.NotFoundException;
import org.example.gateway.core.context.GatewayContext;
import org.example.gateway.core.filter.Filter;
import org.example.gateway.core.filter.FilterAspect;
import org.example.gateway.core.request.GatewayRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@FilterAspect(id=FilterConst.LOAD_BALANCE_FILTER_ID, name=FilterConst.LOAD_BALANCE_FILTER_NAME, order=FilterConst.LOAD_BALANCE_FILTER_ORDER)
public class LoadBalanceFilter implements Filter {

    private static Logger logger = LoggerFactory.getLogger(LoadBalanceFilter.class);

    @Override
    public void doFilter(GatewayContext ctx) throws Exception {
        logger.debug("LoadBalanceFilter: {}", ctx.toString());
        // 获取负载均衡的规则
        final IGatewayLoadBalanceRule loadBalanceRule = getLoadBalanceRule(ctx);
        // 根据负载均衡的规则拿到一个下游服务实例
        final String uniqueId = ctx.getUniqueId();
        final ServiceInstance serviceInstance = loadBalanceRule.choose(uniqueId, ctx.isGray());
        final GatewayRequest request = ctx.getRequest();
        if(serviceInstance != null && request != null) {
            // 保存选出的下游服务实例
            ctx.setServiceInstance(serviceInstance);
        }else {
            logger.warn("No instance available for : {}", uniqueId);
            throw new NotFoundException(ResponseCode.SERVICE_INSTANCE_NOT_FOUND);
        }
    }

    @Override
    public FilterConfig toFilterConfig(JSONObject filterConfigJsonObj) {
        final LoadBalanceFilterConfig loadBalanceConfig = new LoadBalanceFilterConfig();
        loadBalanceConfig.setId(filterConfigJsonObj.getString("id"));
        loadBalanceConfig.setValue(filterConfigJsonObj.getString("value"));
        return loadBalanceConfig;
    }


    /**
     * 获取负载均衡器
     * @param ctx
     * @return
     */
    public IGatewayLoadBalanceRule getLoadBalanceRule(GatewayContext ctx) {
        // 从当前请求规则中拿到负载均衡配置
        LoadBalanceFilterConfig loadBalanceConfig = (LoadBalanceFilterConfig)ctx.getRule().getFilterConfig(FilterConst.LOAD_BALANCE_FILTER_ID);
        String strategy = loadBalanceConfig.getValue();
        IGatewayLoadBalanceRule loadBalanceRule = null;
        switch (strategy){
            case FilterConst.LOAD_BALANCE_STRATEGY_RANDOM:
                loadBalanceRule = RandomLoadBalanceRule.getInstance(ctx.getRule().getServiceId());
                break;
            case FilterConst.LOAD_BALANCE_STRATEGY_ROUND_ROBIN:
                loadBalanceRule = RoundRobinLoadBalanceRule.getInstance(ctx.getRule().getServiceId());
                break;
            default:
                loadBalanceRule = RandomLoadBalanceRule.getInstance(ctx.getRule().getServiceId());
                break;
        }
        return loadBalanceRule;
    }
}
