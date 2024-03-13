package org.example.gateway.core.filter.router;

import com.netflix.hystrix.*;
import org.apache.commons.lang3.StringUtils;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;
import org.example.gateway.common.config.Rule;
import org.example.gateway.common.constants.FilterConst;
import org.example.gateway.common.enums.ResponseCode;
import org.example.gateway.common.exception.ConnectException;
import org.example.gateway.common.exception.ResponseException;
import org.example.gateway.core.ConfigLoader;
import org.example.gateway.core.context.GatewayContext;
import org.example.gateway.core.filter.Filter;
import org.example.gateway.core.filter.FilterAspect;
import org.example.gateway.core.helper.AsyncHttpHelper;
import org.example.gateway.core.helper.ResponseHelper;
import org.example.gateway.core.response.GatewayResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

/**
 * 路由过滤器
 */
@FilterAspect(id= FilterConst.ROUTER_FILTER_ID, name=FilterConst.ROUTER_FILTER_NAME, order = FilterConst.ROUTER_FILTER_ORDER)
public class RouterFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(RouterFilter.class);

    @Override
    public void doFilter(GatewayContext ctx) throws Exception {

        final Optional<Rule.HystrixConfig> hystrixConfig = getHystrixConfig(ctx);
        if(hystrixConfig.isPresent()) {
            // 当前请求配置了熔断策略
            routeWithHystrix(ctx, hystrixConfig);
        }else {
            // 当前请求没有配置熔断策略
            route(ctx, hystrixConfig);
        }


    }


    /**
     * 获取当前请求的熔断配置
     * @param gatewayContext
     * @return
     */
    private static Optional<Rule.HystrixConfig> getHystrixConfig(GatewayContext gatewayContext) {
        final Rule rule = gatewayContext.getRule();
        final Optional<Rule.HystrixConfig> firstConfig = rule.getHystrixConfigs().stream().filter(hystrixConfig -> {
            // 如果当前的请求路径有配置熔断规则
            return StringUtils.equals(hystrixConfig.getPath(), gatewayContext.getRequest().getPath());
        }).findFirst();
        return firstConfig;
    }


    /**
     * 当前请求未配置熔断器，走此方法进行转发
     * @param ctx
     * @param hystrixConfig
     * @return
     */
    private CompletableFuture<Response> route(GatewayContext ctx, Optional<Rule.HystrixConfig> hystrixConfig) {
        final Request request = ctx.getRequest().build();
        final CompletableFuture<Response> future = AsyncHttpHelper.getInstance().executeRequest(request);
        final boolean whenComplete = ConfigLoader.getConfig().isWhenComplete();
        if(whenComplete) {
            future.whenComplete(((response, throwable) -> {
                complete(request, response, throwable, ctx, hystrixConfig);
            }));
        }else {
            future.whenCompleteAsync(((response, throwable) -> {
                complete(request, response, throwable, ctx, hystrixConfig);
            }));
        }
        return future;
    }


    /**
     * 当前请求配置了熔断，走此方法进行转发
     * @param gatewayContext
     * @param hystrixConfig
     */
    private void routeWithHystrix(GatewayContext gatewayContext, Optional<Rule.HystrixConfig> hystrixConfig) {
        final HystrixCommand.Setter setter = HystrixCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(gatewayContext.getUniqueId()))
                .andCommandKey(HystrixCommandKey.Factory.asKey(gatewayContext.getRequest().getPath()))
                .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter()
                        .withCoreSize(hystrixConfig.get().getThreadCoreSize()))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                        .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.THREAD)
                        .withExecutionTimeoutInMilliseconds(hystrixConfig.get().getTimeoutInMilliseconds())
                        .withExecutionIsolationThreadInterruptOnTimeout(true)
                        .withExecutionTimeoutEnabled(true));
        final HystrixCommand<Object> hystrixCommand = new HystrixCommand<Object>(setter) {
            @Override
            protected Object run() throws Exception {
                route(gatewayContext, hystrixConfig).get();
                return null;
            }

            @Override
            protected Object getFallback() {
                gatewayContext.setResponse(hystrixConfig);
                gatewayContext.written();
                return null;
            }
        };
        hystrixCommand.execute();
    }



    /**
     * 向客户端返回响应
     * @param request
     * @param response
     * @param throwable
     * @param gatewayContext
     */
    private void complete(Request request, Response response, Throwable throwable, GatewayContext gatewayContext, Optional<Rule.HystrixConfig> hystrixConfig) {
        // 释放请求资源
        gatewayContext.releaseRequest();
        final int currentRetryTimes = gatewayContext.getCurrentRetryTimes();
        final int confRetryTimes = gatewayContext.getRule().getRetryConfig().getTimes();
        // 当前请求超时或者出现IO异常 且 没有超过最大重试次数 且 没有熔断 才进行重试
        if((throwable instanceof TimeoutException || throwable instanceof IOException)
                && currentRetryTimes <= confRetryTimes && !hystrixConfig.isPresent()) {
            doRetry(gatewayContext, currentRetryTimes);
            return;
        }
        try {
            if(Objects.nonNull(throwable)) {
                // 处理异常
                String url = request.getUrl();
                if(throwable instanceof TimeoutException) {
                    logger.warn("complete time out {}", url);
                    gatewayContext.setThrowable(new ResponseException(ResponseCode.REQUEST_TIMEOUT));
                }else {
                    gatewayContext.setThrowable(new ConnectException(throwable, gatewayContext.getUniqueId(), url, ResponseCode.HTTP_RESPONSE_ERROR));
                }
            }else {
                // 使用AsyncHttp response对象构建网关response对象
                gatewayContext.setResponse(GatewayResponse.buildGatewayResponse(response));
            }
        }catch (Throwable t) {
            // 捕获其它异常
            gatewayContext.setThrowable(new ResponseException(ResponseCode.INTERNAL_ERROR));
            logger.error("complete error", t);
        }finally {
            // 不管成功失败都需要向客户端响应请求
            gatewayContext.written(); // 记录请求阶段
            ResponseHelper.writeResponse(gatewayContext); // 响应
        }
    }


    /**
     * 请求重试
     * @param gatewayContext
     * @param currentRetryTimes
     */
    private void doRetry(GatewayContext gatewayContext, int currentRetryTimes) {
        logger.info("current retry times {}", currentRetryTimes);
        gatewayContext.setCurrentRetryTimes(currentRetryTimes+1);
        try {
            doFilter(gatewayContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
