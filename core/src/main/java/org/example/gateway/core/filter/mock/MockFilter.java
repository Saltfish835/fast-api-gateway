package org.example.gateway.core.filter.mock;


import org.example.gateway.common.config.Rule;
import org.example.gateway.common.constants.FilterConst;
import org.example.gateway.common.utils.JSONUtil;
import org.example.gateway.core.context.GatewayContext;
import org.example.gateway.core.filter.Filter;
import org.example.gateway.core.filter.FilterAspect;
import org.example.gateway.core.helper.ResponseHelper;
import org.example.gateway.core.response.GatewayResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@FilterAspect(id = FilterConst.MOCK_FILTER_ID, name = FilterConst.MOCK_FILTER_NAME)
public class MockFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(MockFilter.class);

    @Override
    public void doFilter(GatewayContext ctx) throws Exception {
        final Rule.FilterConfig filterConfig = ctx.getRule().getFilterConfig(FilterConst.MOCK_FILTER_ID);
        if(filterConfig == null) {
            return;
        }
        final Map<String, String> map = JSONUtil.parse(filterConfig.getConfig(), Map.class);
        final String value = map.get(ctx.getRequest().getHttpMethod().name() + " " + ctx.getRequest().getPath());
        // 命中了mock规则
        if(value != null) {
            ctx.setResponse(GatewayResponse.buildGatewayResponse(value));
            ctx.written();
            ResponseHelper.writeResponse(ctx);
            logger.info("mock {} {} {}", ctx.getRequest().getHttpMethod(), ctx.getRequest().getPath(), value);
            ctx.terminated();
        }
    }
}
