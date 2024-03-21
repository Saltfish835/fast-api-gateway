package org.example.gateway.core.filter.router;

import org.apache.commons.lang3.StringUtils;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;
import org.example.gateway.common.config.Rule;
import org.example.gateway.common.constants.FilterConst;
import org.example.gateway.common.enums.ResponseCode;
import org.example.gateway.common.exception.ConnectException;
import org.example.gateway.common.exception.NotFoundException;
import org.example.gateway.common.exception.ResponseException;
import org.example.gateway.core.context.GatewayContext;
import org.example.gateway.core.filter.Filter;
import org.example.gateway.core.filter.FilterAspect;
import org.example.gateway.core.filter.router.executor.DubboExecutor;
import org.example.gateway.core.filter.router.executor.HttpExecutor;
import org.example.gateway.core.filter.router.executor.IExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.Optional;

@FilterAspect(id= FilterConst.ROUTER_FILTER_ID, name=FilterConst.ROUTER_FILTER_NAME, order = FilterConst.ROUTER_FILTER_ORDER)
public class MyRouterFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(MyRouterFilter.class);

    @Override
    public void doFilter(GatewayContext ctx) throws Exception {
        final IExecutor executor = getExecutor(ctx);
        final Optional<Rule.HystrixConfig> hystrixConfig = getHystrixConfig(ctx);
        executor.execute(ctx, hystrixConfig);
    }


    /**
     * 获取执行器
     * @param ctx
     * @return
     */
    public IExecutor getExecutor(GatewayContext ctx) {
        IExecutor executor = null;
        final String protocol = ctx.getProtocol();
        if(StringUtils.isEmpty(protocol)) {
            throw new NotFoundException(ResponseCode.PROTOCOL_PARSE_ERROR_IS_EMPTY);
        }
        switch (protocol) {
            case FilterConst.ROUTE_PROTOCOL_HTTP:
                executor = HttpExecutor.getInstance(this);
                break;
            case FilterConst.ROUTE_PROTOCOL_DUBBO:
                executor = DubboExecutor.getInstance(this);
                break;
            default:
                throw new NotFoundException(ResponseCode.PROTOCOL_NO_MATCHED);
        }
        return executor;
    }


    /**
     * 获取当前请求的熔断配置
     * @param gatewayContext
     * @return
     */
    private static Optional<Rule.HystrixConfig> getHystrixConfig(GatewayContext gatewayContext) {
        final Rule rule = gatewayContext.getRule();
        final Optional<Rule.HystrixConfig> firstConfig = rule.getHystrixConfigs().stream().filter(hystrixConfig -> {
            // 如果当前的请求路径有配置熔断规则
            return StringUtils.equals(hystrixConfig.getPath(), gatewayContext.getRequest().getPath());
        }).findFirst();
        return firstConfig;
    }
}
