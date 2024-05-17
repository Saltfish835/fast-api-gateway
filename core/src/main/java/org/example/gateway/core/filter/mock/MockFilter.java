package org.example.gateway.core.filter.mock;


import com.alibaba.fastjson.JSONObject;
import org.example.gateway.common.config.FilterConfig;
import org.example.gateway.common.constants.FilterConst;
import org.example.gateway.core.context.GatewayContext;
import org.example.gateway.core.filter.Filter;
import org.example.gateway.core.filter.FilterAspect;
import org.example.gateway.core.helper.ResponseHelper;
import org.example.gateway.core.response.GatewayResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@FilterAspect(id = FilterConst.MOCK_FILTER_ID, name = FilterConst.MOCK_FILTER_NAME, order = FilterConst.MOCK_FILTER_ORDER)
public class MockFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(MockFilter.class);

    @Override
    public void doFilter(GatewayContext ctx) throws Exception {
        MockFilterConfig mockConfig = (MockFilterConfig)ctx.getRule().getFilterConfig(FilterConst.MOCK_FILTER_ID);
        if(mockConfig == null) {
            return;
        }
        // 直接向客户端返回mock结果
        String value = mockConfig.getValue();
        ctx.setResponse(GatewayResponse.buildGatewayResponse(value));
        ctx.written();
        ResponseHelper.writeResponse(ctx);
        logger.info("mock {} {} {}", ctx.getRequest().getHttpMethod(), ctx.getRequest().getPath(), value);
        ctx.terminated();
    }

    @Override
    public FilterConfig toFilterConfig(JSONObject filterConfigJsonObj) {
        final MockFilterConfig mockConfig = new MockFilterConfig();
        mockConfig.setId(filterConfigJsonObj.getString("id"));
        mockConfig.setValue(filterConfigJsonObj.getString("value"));
        return mockConfig;
    }
}
