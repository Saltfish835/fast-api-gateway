package org.example.gateway.core.filter.flowCtl;


import com.alibaba.fastjson.JSONObject;
import org.example.gateway.common.config.FilterConfig;
import org.example.gateway.common.constants.FilterConst;
import org.example.gateway.core.context.GatewayContext;
import org.example.gateway.core.filter.Filter;
import org.example.gateway.core.filter.FilterAspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




@FilterAspect(id= FilterConst.FLOW_CTL_FILTER_ID, name=FilterConst.FLOW_CTL_FILTER_NAME, order = FilterConst.FLOW_CTL_FILTER_ORDER)
public class FlowCtlFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(FlowCtlFilter.class);

    @Override
    public void doFilter(GatewayContext ctx) throws Exception {
        // 从当前请求的规则中拿到限流配置
        FlowCtlFilterConfig flowCtlConfig = (FlowCtlFilterConfig)ctx.getRule().getFilterConfig(FilterConst.FLOW_CTL_FILTER_ID);
        if(flowCtlConfig == null) {
            return;
        }
        // 拿到当前请求的规则
        IGatewayFlowCtlRule flowCtlRule = null;
        String path = ctx.getRequest().getPath();
        String serviceId = ctx.getRule().getServiceId();
        if(flowCtlConfig.getType().equals(FilterConst.FLOW_CTL_TYPE_PATH) && flowCtlConfig.getValue().equals(path)) {
            // 根据路径限流
            flowCtlRule = FlowCtlByPathRule.getInstance(ctx.getRule().getServiceId(), path);
        }else if(flowCtlConfig.getType().equals(FilterConst.FLOW_CTL_TYPE_SERVICE) && flowCtlConfig.getValue().equals(serviceId)) {
            // TODO 根据服务id进行限流

        }
        // 拿到了限流规则
        if(flowCtlRule != null) {
            flowCtlRule.doFlowCtlFilter(flowCtlConfig, ctx.getRule().getServiceId());
        }

    }

    @Override
    public FilterConfig toFilterConfig(JSONObject filterConfigJsonObj) {
        final FlowCtlFilterConfig flowCtlConfig = new FlowCtlFilterConfig();
        flowCtlConfig.setId(filterConfigJsonObj.getString("id"));
        flowCtlConfig.setType(filterConfigJsonObj.getString("type"));
        flowCtlConfig.setValue(filterConfigJsonObj.getString("value"));
        flowCtlConfig.setModel(filterConfigJsonObj.getString("model"));
        final JSONObject innerConf = filterConfigJsonObj.getJSONObject("config");
        flowCtlConfig.setConfig(new FlowCtlFilterConfig.Config(innerConf.getInteger("duration"), innerConf.getInteger("permits")));
        return flowCtlConfig;
    }
}
