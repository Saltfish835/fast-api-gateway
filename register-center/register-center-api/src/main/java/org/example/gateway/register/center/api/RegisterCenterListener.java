package org.example.gateway.register.center.api;

import org.example.gateway.common.config.ServiceDefinition;
import org.example.gateway.common.config.ServiceInstance;

import java.util.Set;

public interface RegisterCenterListener {

    void onChange(ServiceDefinition serviceDefinition, Set<ServiceInstance> serviceInstanceSet);
}
