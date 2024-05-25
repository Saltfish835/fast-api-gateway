package org.example.gateway.core;

import org.apache.commons.lang3.StringUtils;
import org.example.gateway.config.center.api.ConfigCenter;
import org.example.gateway.core.filter.Filter;
import org.example.gateway.core.filter.FilterAspect;
import org.example.gateway.core.predicates.Predicate;
import org.example.gateway.core.predicates.PredicateAspect;
import org.example.gateway.register.center.api.RegisterCenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

public class PluginLoader {

    private static final Logger logger = LoggerFactory.getLogger(PluginLoader.class);

    private static final PluginLoader INSTANCE = new PluginLoader();

    private PluginLoader() {
    }

    public static PluginLoader getInstance() {
        return INSTANCE;
    }

    /**
     * 加载所有插件
     * @param config
     */
    public void load(Config config) {
        // 加载并保存配置中心插件
        config.setConfigCenter(loadConfigCenter());
        // 加载并保存注册中心插件
        config.setRegisterCenter(loadRegisterCenter());
        // 加载并保存所有过滤器
        config.setFilterMap(loadFilter());
        // 加载并保存所有predicate
        config.setPredicateMap(loadPredicate());
    }


    /**
     * 加载配置中心插件
     * @return
     */
    private ConfigCenter loadConfigCenter() {
        ConfigCenter configCenter = null;
        final ServiceLoader<ConfigCenter> configCenterServiceLoader = ServiceLoader.load(ConfigCenter.class);
        for(ConfigCenter configCenterTmp : configCenterServiceLoader) {
            configCenter = configCenterTmp;
            logger.info("load configCenter success: {}",configCenter.getClass());
            break;
        }
        if(configCenter == null) {
            logger.error("not found ConfigCenter impl");
            throw new RuntimeException("not found ConfigCenter impl");
        }
        return configCenter;
    }


    /**
     * 加载注册中心插件
     * @return
     */
    private RegisterCenter loadRegisterCenter() {
        RegisterCenter registerCenter = null;
        final ServiceLoader<RegisterCenter> registerCenterServiceLoader = ServiceLoader.load(RegisterCenter.class);
        for(RegisterCenter registerCenterTmp : registerCenterServiceLoader) {
            registerCenter = registerCenterTmp;
            logger.info("load registerCenter success: {}",registerCenter.getClass());
            break;
        }
        if(registerCenter == null) {
            logger.error("not found RegisterCenter impl");
            throw  new RuntimeException("not found RegisterCenter impl");
        }
        return registerCenter;
    }


    /**
     * 加载过滤器
     * @return
     */
    private ConcurrentHashMap<String, Filter> loadFilter() {
        ConcurrentHashMap filterMap = new ConcurrentHashMap();
        final ServiceLoader<Filter> filterServiceLoader = ServiceLoader.load(Filter.class);
        for(Filter filter : filterServiceLoader) {
            final FilterAspect annotation = filter.getClass().getAnnotation(FilterAspect.class);
            logger.info("load filter success: {},{},{},{}",filter.getClass(), annotation.id(),annotation.name(),annotation.order());
            if(annotation != null) {
                String filterId = annotation.id();
                // 如果id为空，则使用类名作为key
                filterId = StringUtils.isEmpty(filterId) ? filter.getClass().getName() : filterId;
                filterMap.put(filterId, filter);
            }
        }
        return filterMap;
    }


    /**
     * 加载predicate
     * @return
     */
    private ConcurrentHashMap<String, Predicate> loadPredicate() {
        ConcurrentHashMap predicateMap = new ConcurrentHashMap();
        final ServiceLoader<Predicate> predicateServiceLoader = ServiceLoader.load(Predicate.class);
        for(Predicate predicate : predicateServiceLoader) {
            final PredicateAspect annotation = predicate.getClass().getAnnotation(PredicateAspect.class);
            logger.info("load predicate success: {},{},{}",predicate.getClass(), annotation.id(), annotation.name());
            if (annotation != null) {
                predicateMap.put(annotation.id(), predicate);
            }
        }
        return predicateMap;
    }
}
