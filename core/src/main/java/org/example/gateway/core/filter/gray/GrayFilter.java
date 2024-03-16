package org.example.gateway.core.filter.gray;

import org.example.gateway.common.constants.FilterConst;
import org.example.gateway.core.context.GatewayContext;
import org.example.gateway.core.filter.Filter;
import org.example.gateway.core.filter.FilterAspect;

/**
 * 选取灰度服务
 */
@FilterAspect(id= FilterConst.GRAY_FILTER_ID, name = FilterConst.GRAY_FILTER_NAME, order = FilterConst.GRAY_FILTER_ORDER)
public class GrayFilter implements Filter {

    @Override
    public void doFilter(GatewayContext ctx) throws Exception {
        // 测试用：请求头中带有gray，认定其为灰度流量
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
}
