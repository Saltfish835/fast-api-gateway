package org.example.gateway.core.filter.router.executor;

import org.example.gateway.core.context.GatewayContext;


public interface IExecutor {

    void execute(GatewayContext ctx);
}
