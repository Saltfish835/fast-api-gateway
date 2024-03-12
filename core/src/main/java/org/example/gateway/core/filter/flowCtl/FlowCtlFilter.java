package org.example.gateway.core.filter.flowCtl;

import org.example.gateway.common.config.Rule;
import org.example.gateway.common.constants.FilterConst;
import org.example.gateway.core.context.GatewayContext;
import org.example.gateway.core.filter.Filter;
import org.example.gateway.core.filter.FilterAspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;


@FilterAspect(id= FilterConst.FLOW_CTL_FILTER_ID, name=FilterConst.FLOW_CTL_FILTER_NAME, order = FilterConst.FLOW_CTL_FILTER_ORDER)
public class FlowCtlFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(FlowCtlFilter.class);

    @Override
    public void doFilter(GatewayContext ctx) throws Exception {
        final Rule rule = ctx.getRule();
        if(rule == null) {
            return;
        }
        final Set<Rule.FlowCtlConfig> flowCtlConfigs = rule.getFlowCtlConfigs();
        for(Rule.FlowCtlConfig flowCtlConfig : flowCtlConfigs) {
            IGatewayFlowCtlRule flowCtlRule = null;
            if(flowCtlConfig == null) {
                continue;
            }
            final String path = ctx.getRequest().getPath();
            if(flowCtlConfig.getType().equals(FilterConst.FLOW_CTL_TYPE_PATH) && path.equals(flowCtlConfig.getValue())) {
                // 根据路径限流
                flowCtlRule = FlowCtlByPathRule.getInstance(rule.getServiceId(), path);
            }else if(flowCtlConfig.getType().equalsIgnoreCase(FilterConst.FLOW_CTL_TYPE_SERVICE)) {
                // TODO 根据服务id进行限流

            }
            if(flowCtlRule != null) {
                flowCtlRule.doFlowCtlFilter(flowCtlConfig, rule.getServiceId());
            }
        }
    }
}
