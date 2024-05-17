package org.example.gateway.core.predicates;

import java.lang.annotation.*;

/**
 * predicate注解
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface PredicateAspect {

    /**
     * predicate ID
     * @return
     */
    String id();

    /**
     * 过滤器名称
     * @return
     */
    String name() default "";


}
