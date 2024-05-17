package org.example.gateway.http.server.controller;


import org.example.gateway.client.core.ApiInvoker;
import org.example.gateway.client.core.ApiProperties;
import org.example.gateway.client.core.ApiProtocol;
import org.example.gateway.client.core.ApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@ApiService(serviceId = "backend-http-server", protocol = ApiProtocol.HTTP, patternPath = "/http-server/**")
public class PingController {

    private static final Logger logger = LoggerFactory.getLogger(PingController.class);

    @Resource
    private ApiProperties apiProperties;

    @ApiInvoker(path = "/http-server/ping")
    @GetMapping("/http-server/ping")
    public String ping() {
        logger.info("{}", apiProperties);
        return "pong";
    }


    @ApiInvoker(path = "/http-server/sayHi")
    @GetMapping("/http-server/sayHi")
    public String mock(String name) {
        logger.info("{}", apiProperties);
        return "hello "+name;
    }
}
