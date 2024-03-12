package org.example.gateway.core.filter.flowCtl;

import org.example.gateway.common.config.Rule;

/**
 * 执行限流的接口
 */
public interface IGatewayFlowCtlRule {

    void doFlowCtlFilter(Rule.FlowCtlConfig flowCtlConfig, String serviceId);
}
