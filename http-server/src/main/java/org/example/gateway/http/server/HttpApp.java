package org.example.gateway.http.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 模拟下游是http服务
 *
 */
@SpringBootApplication(scanBasePackages = "org.example")
public class HttpApp
{

    public static void main( String[] args )
    {
        SpringApplication.run(HttpApp.class, args);
    }
}
