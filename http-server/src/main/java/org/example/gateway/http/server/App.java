package org.example.gateway.http.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 模拟下游是http服务
 *
 */
@SpringBootApplication
@RestController
public class App 
{
    @GetMapping("/http-demo/ping")
    public String ping() {
        return "pong";
    }

    public static void main( String[] args )
    {
        SpringApplication.run(App.class, args);
    }
}
