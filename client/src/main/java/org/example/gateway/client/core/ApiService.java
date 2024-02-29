package org.example.gateway.client.core;

import java.lang.annotation.*;

/**
 * 服务定义
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiService {

    /**
     * 服务id
     * @return
     */
    String serviceId();

    /**
     * 服务版本
     * @return
     */
    String version() default "1.0.0";

    /**
     * 服务协议
     * @return
     */
    ApiProtocol protocol();

    /**
     * 匹配路径
     * @return
     */
    String patternPath();
}
