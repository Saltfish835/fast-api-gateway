package org.example.gateway.core.filter.router.executor;

import org.example.gateway.common.config.Rule;
import org.example.gateway.core.context.GatewayContext;

import java.util.Optional;

public interface IExecutor {

    void execute(GatewayContext ctx, Optional<Rule.HystrixConfig> hystrixConfig);
}
