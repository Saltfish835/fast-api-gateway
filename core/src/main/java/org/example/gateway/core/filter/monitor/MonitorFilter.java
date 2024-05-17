package org.example.gateway.core.filter.monitor;

import com.alibaba.fastjson.JSONObject;
import io.micrometer.core.instrument.Timer;
import org.example.gateway.common.config.FilterConfig;
import org.example.gateway.common.constants.FilterConst;
import org.example.gateway.core.context.GatewayContext;
import org.example.gateway.core.filter.Filter;
import org.example.gateway.core.filter.FilterAspect;

@FilterAspect(id= FilterConst.MONITOR_FILTER_ID, name = FilterConst.MONITOR_FILTER_NAME, order = FilterConst.MONITOR_FILTER_ORDER)
public class MonitorFilter implements Filter {

    @Override
    public void doFilter(GatewayContext ctx) throws Exception {
        // 开始采集
        ctx.setTimerSample(Timer.start());
    }

    @Override
    public FilterConfig toFilterConfig(JSONObject filterConfigJsonObj) {
        return null;
    }
}
