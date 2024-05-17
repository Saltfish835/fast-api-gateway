package org.example.gateway.client.core.autoconfigure;

import org.apache.dubbo.config.spring.ServiceBean;
import org.example.gateway.client.core.ApiProperties;
import org.example.gateway.client.support.dubbo.Dubbo27ClientRegisterManager;
import org.example.gateway.client.support.springmvc.SpringMVCClientRegisterManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;
import javax.servlet.Servlet;

/**
 * SpringBoot自动配置
 */
@Configuration
@EnableConfigurationProperties(ApiProperties.class)
@ConditionalOnProperty(prefix = "api-gateway", name = {"registerAddress"}) // 如果有配置registerAddress才会自动装配
public class ApiClientAutoConfiguration {

    @Resource
    private ApiProperties apiProperties;

    @Bean
    @ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class}) // 如果有SpringMVC相关类才注入这个类
    @ConditionalOnMissingBean(SpringMVCClientRegisterManager.class) // 如果已经注入就不再注入
    public SpringMVCClientRegisterManager springMVCClientRegisterManager() {
        return new SpringMVCClientRegisterManager(apiProperties);
    }

//    @Bean
//    @ConditionalOnClass({ServiceBean.class})
//    @ConditionalOnMissingBean(Dubbo27ClientRegisterManager.class)
//    public Dubbo27ClientRegisterManager dubbo27ClientRegisterManager() {
//        return new Dubbo27ClientRegisterManager(apiProperties);
//    }
}
