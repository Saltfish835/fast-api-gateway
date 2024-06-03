package org.example.gateway.core.filter.router;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.example.gateway.common.config.FilterConfig;
import org.example.gateway.common.constants.FilterConst;
import org.example.gateway.common.enums.ResponseCode;
import org.example.gateway.common.exception.NotFoundException;
import org.example.gateway.core.context.GatewayContext;
import org.example.gateway.core.filter.Filter;
import org.example.gateway.core.filter.FilterAspect;
import org.example.gateway.core.filter.router.executor.DubboExecutor;
import org.example.gateway.core.filter.router.executor.HttpExecutor;
import org.example.gateway.core.filter.router.executor.IExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@FilterAspect(id= FilterConst.ROUTER_FILTER_ID, name=FilterConst.ROUTER_FILTER_NAME, order = FilterConst.ROUTER_FILTER_ORDER)
public class MyRouterFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(MyRouterFilter.class);

    @Override
    public void doFilter(GatewayContext ctx) throws Exception {
        logger.debug("MyRouterFilter: {}", ctx.toString());
        // 拿到执行器
        final IExecutor executor = getExecutor(ctx);
        if(executor == null) {
            logger.error("executor not found, uri is {}", ctx.getOriginRequest().getUri());
            throw new RuntimeException("executor not found");
        }
        // 向下游服务发送请求
        executor.execute(ctx);
    }

    @Override
    public FilterConfig toFilterConfig(JSONObject filterConfigJsonObj) {
        return null;
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
                executor = null;
        }
        return executor;
    }

}
