package org.example;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Hello world!
 *
 */
@SpringBootApplication(scanBasePackages = "org.example")
@EnableDubbo
public class DubboWebApp
{
    public static void main( String[] args )
    {
        SpringApplication.run(DubboWebApp.class, args);
    }
}
