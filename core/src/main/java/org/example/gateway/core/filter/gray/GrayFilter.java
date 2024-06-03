package org.example.gateway.core.filter.gray;

import com.alibaba.fastjson.JSONObject;
import org.example.gateway.common.config.FilterConfig;
import org.example.gateway.common.constants.FilterConst;
import org.example.gateway.core.context.GatewayContext;
import org.example.gateway.core.filter.Filter;
import org.example.gateway.core.filter.FilterAspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 选取灰度服务
 */
@FilterAspect(id= FilterConst.GRAY_FILTER_ID, name = FilterConst.GRAY_FILTER_NAME, order = FilterConst.GRAY_FILTER_ORDER)
public class GrayFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(GrayFilter.class);

    @Override
    public void doFilter(GatewayContext ctx) throws Exception {
        logger.debug("GrayFilter: {}", ctx.toString());
        // 测试用：请求头中带有gray，则直接认定其为灰度流量
        final String gray = ctx.getRequest().getHttpHeaders().get("gray");
        if("true".equalsIgnoreCase(gray)) {
            // 后续会转发给灰度服务
            ctx.setGray(true);
        }else {
            // 随机选取小部分流量作为灰度流量
            final String clientIp = ctx.getRequest().getClientIp();
            final int mod = clientIp.hashCode() & (1024 - 1); // 等价于对1024取模
            if(mod == 1) {
                // 1024分之一的概率
                ctx.setGray(true);
            }
        }
    }

    @Override
    public FilterConfig toFilterConfig(JSONObject filterConfigJsonObj) {
        final GrayFilterConfig grayFilterConfig = new GrayFilterConfig();
        grayFilterConfig.setId(filterConfigJsonObj.getString("id"));
        return grayFilterConfig;
    }
}
