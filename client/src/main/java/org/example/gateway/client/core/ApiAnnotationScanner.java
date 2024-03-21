package org.example.gateway.client.core;




import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.ProviderConfig;
import org.apache.dubbo.config.spring.ServiceBean;
import org.example.gateway.client.support.dubbo.DubboConstants;
import org.example.gateway.common.config.DubboServiceInvoker;
import org.example.gateway.common.config.HttpServiceInvoker;
import org.example.gateway.common.config.ServiceDefinition;
import org.example.gateway.common.config.ServiceInvoker;
import org.example.gateway.common.constants.BasicConst;
import org.example.gateway.common.utils.JSONUtil;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 扫描注解
 */
public class ApiAnnotationScanner {

    private ApiAnnotationScanner() {

    }

    private static class SingletonHolder {
        static final ApiAnnotationScanner INSTANCE = new ApiAnnotationScanner();
    }

    public static ApiAnnotationScanner getInstance() {
        return SingletonHolder.INSTANCE;
    }


    /**
     * 扫描注解，返回服务定义
     * @param bean
     * @param args
     * @return
     */
    public ServiceDefinition scanner(Object bean, Object... args) {
        final Class<?> aClass = bean.getClass();
        if(!aClass.isAnnotationPresent(ApiService.class)) {
            return null;
        }
        // 拿到ApiService注解相关信息
        final ApiService apiServiceAnnotation = aClass.getAnnotation(ApiService.class);
        final String serviceId = apiServiceAnnotation.serviceId();
        final ApiProtocol protocol = apiServiceAnnotation.protocol();
        final String patternPath = apiServiceAnnotation.patternPath();
        String version = apiServiceAnnotation.version();
        // 拿到ApiInvoker注解相关信息
        final ServiceDefinition serviceDefinition = new ServiceDefinition();
//        Map<String, ServiceInvoker> invokerMap = new HashMap<>();
        Map<String, String> invokerMap = new HashMap<>();
        final Method[] methods = aClass.getMethods();
        if(methods != null && methods.length > 0) {
            for(Method method : methods) {
                final ApiInvoker apiInvokerAnnotation = method.getAnnotation(ApiInvoker.class);
                if(apiInvokerAnnotation == null) {
                    continue;
                }
                final String path = apiInvokerAnnotation.path();
                switch (protocol) {
                    case HTTP:
                        final HttpServiceInvoker httpServiceInvoker = createHttpServiceInvoker(path);
                        invokerMap.put(path, JSONUtil.toJSONString(httpServiceInvoker));
                        break;
                    case DUBBO:
                        ServiceBean<?> serviceBean = (ServiceBean<?>) args[0];
                        final DubboServiceInvoker dubboServiceInvoker = createDubboServiceInvoker(path, serviceBean, method);
                        final String dubboServiceInvokerVersion = dubboServiceInvoker.getVersion();
                        if(!StringUtils.isBlank(dubboServiceInvokerVersion)) {
                            version = dubboServiceInvokerVersion;
                        }
                        invokerMap.put(path, JSONUtil.toJSONString(dubboServiceInvoker));
                        break;
                    default:
                        break;
                }
            }
            serviceDefinition.setUniqueId(serviceId + BasicConst.COLON_SEPARATOR + version);
            serviceDefinition.setServiceId(serviceId);
            serviceDefinition.setVersion(version);
            serviceDefinition.setProtocol(protocol.getCode());
            serviceDefinition.setPatternPath(patternPath);
            serviceDefinition.setEnable(true);
            serviceDefinition.setInvokerMap(invokerMap);
            return serviceDefinition;
        }
        return null;
    }

    /**
     * 构建HttpServiceInvoker对象
     * @param path
     * @return
     */
    private HttpServiceInvoker createHttpServiceInvoker(String path) {
        final HttpServiceInvoker httpServiceInvoker = new HttpServiceInvoker();
        httpServiceInvoker.setInvokerPath(path);
        return httpServiceInvoker;
    }

    /**
     * 创建DubboServiceinvoker对象
     * @param path
     * @param serviceBean
     * @param method
     * @return
     */
    private DubboServiceInvoker createDubboServiceInvoker(String path, ServiceBean<?> serviceBean, Method method) {
        final DubboServiceInvoker dubboServiceInvoker = new DubboServiceInvoker();
        dubboServiceInvoker.setInvokerPath(path);
        final String methodName = method.getName();
        final String anInterface = serviceBean.getInterface();
        final String registerAddress = serviceBean.getRegistry().getAddress();
        dubboServiceInvoker.setMethodName(methodName);
        dubboServiceInvoker.setInterfaceClass(anInterface);
        dubboServiceInvoker.setRegisterAddress(registerAddress);
        final String[] parameterTypes = new String[method.getParameterCount()];
        Class<?>[] classes = method.getParameterTypes();
        for (int i = 0; i < classes.length; i++) {
            parameterTypes[i] = classes[i].getName();
        }
        dubboServiceInvoker.setParameterTypes(parameterTypes);
        Integer serviceBeanTimeout = serviceBean.getTimeout();
        if (serviceBeanTimeout == null || serviceBeanTimeout.intValue() == 0) {
            ProviderConfig providerConfig = serviceBean.getProvider();
            if (providerConfig != null) {
                Integer providerTimeout = providerConfig.getTimeout();
                if (providerTimeout == null || providerTimeout.intValue() == 0) {
                    serviceBeanTimeout = DubboConstants.DUBBO_TIMEOUT;
                } else {
                    serviceBeanTimeout = providerTimeout;
                }
            }
        }
        dubboServiceInvoker.setTimeout(serviceBeanTimeout);
        String dubboVersion = serviceBean.getVersion();
        dubboServiceInvoker.setVersion(dubboVersion);
        return dubboServiceInvoker;
    }


}
