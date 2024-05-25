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

    private static Integer retryCount = 0;

    private static Integer sayHiCount = 0;

    /**
     * 正常流程
     * @return
     */
    @ApiInvoker(path = "/http-server/ping")
    @GetMapping("/http-server/ping")
    public String ping() throws InterruptedException {
        logger.info("/http-server/ping");
        return "pong";
    }


    /**
     * 测试限流
     * @param id
     * @return
     */
    @ApiInvoker(path = "/http-server/getNameById")
    @GetMapping("/http-server/getNameById")
    public String getNameById(@RequestParam("id") Integer id) {
        logger.info("/http-server/getNameById");
        return "id:"+id+", name:zhangsan";
    }


    /**
     * 测试服务降级
     * @param name
     * @return
     * @throws InterruptedException
     */
    @ApiInvoker(path = "/http-server/sayHi")
    @PostMapping("/http-server/sayHi")
    public String sayHi(@RequestParam("name") String name) throws InterruptedException {
        logger.info("/http-server/sayHi, {}", name);
        sayHiCount++;
        if(sayHiCount <= 5) {
            logger.info("/http-server/sayHi timeout");
            Thread.sleep(60 * 1000);
        }
        logger.info("/http-server/sayHi finish");
        return "hello "+name;
    }


    /**
     * 测试重试
     * @return
     * @throws InterruptedException
     */
    @ApiInvoker(path = "/http-server/testRetry")
    @PostMapping("/http-server/testRetry")
    public String testRetry() throws InterruptedException {
        retryCount++;
        logger.info("/http-server/testRetry");
        if(retryCount < 3) {
            logger.info("/http-server/testTimeout, {}s", 30);
            Thread.sleep(30 * 1000);
        }
        logger.info("/http-server/testRetry finish");
        return "has sleep " + 30 + "s";
    }
}
