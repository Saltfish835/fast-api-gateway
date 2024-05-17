package org.example.gateway.core.filter.flowCtl;


/**
 * 执行限流的接口
 */
public interface IGatewayFlowCtlRule {

    void doFlowCtlFilter(FlowCtlFilterConfig flowCtlConfig, String serviceId);
}
