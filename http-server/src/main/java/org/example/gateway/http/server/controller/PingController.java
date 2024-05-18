package org.example.gateway.http.server.controller;


import org.example.gateway.client.core.ApiInvoker;
import org.example.gateway.client.core.ApiProtocol;
import org.example.gateway.client.core.ApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@ApiService(serviceId = "backend-http-server", protocol = ApiProtocol.HTTP, patternPath = "/http-server/**")
public class PingController {

    private static final Logger logger = LoggerFactory.getLogger(PingController.class);

    private static Integer timeoutCount = 0;

    private static Integer sayHiCount = 0;

    @ApiInvoker(path = "/http-server/ping")
    @GetMapping("/http-server/ping")
    public String ping() {
        logger.info("/http-server/ping");
        return "pong";
    }


    @ApiInvoker(path = "/http-server/getNameById")
    @GetMapping("/http-server/getNameById")
    public String getNameById(@RequestParam("id") Integer id) {
        logger.info("/http-server/getNameById");
        return "id:"+id+", name:zhangsan";
    }


    @ApiInvoker(path = "/http-server/sayHi")
    @PostMapping("/http-server/sayHi")
    public String sayHi(@RequestParam("name") String name) throws InterruptedException {
        logger.info("/http-server/sayHi, {}", name);
        sayHiCount++;
        if(sayHiCount <= 5) {
            logger.info("/http-server/sayHi by zero");
            Thread.sleep(60 * 1000);
        }
        logger.info("/http-server/sayHi finish");
        return "hello "+name;
    }


    @ApiInvoker(path = "/http-server/testTimeout")
    @PostMapping("/http-server/testTimeout")
    public String testTimeout(@RequestParam("timeout") Integer seconds) throws InterruptedException {
        timeoutCount++;
        logger.info("/http-server/testTimeout");
        if(timeoutCount < 3) {
            logger.info("/http-server/testTimeout, {}s", seconds);
            Thread.sleep(seconds * 1000);
        }
        logger.info("/http-server/testTimeout finish");
        return "has sleep " + seconds + "s";
    }
}
