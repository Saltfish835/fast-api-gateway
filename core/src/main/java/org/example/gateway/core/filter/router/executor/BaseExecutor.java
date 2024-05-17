package org.example.gateway.core.filter.router.executor;

import com.netflix.hystrix.*;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;
import org.example.gateway.common.constants.FilterConst;
import org.example.gateway.common.enums.ResponseCode;
import org.example.gateway.common.exception.ConnectException;
import org.example.gateway.common.exception.ResponseException;
import org.example.gateway.core.context.GatewayContext;
import org.example.gateway.core.filter.Filter;
import org.example.gateway.core.helper.ResponseHelper;
import org.example.gateway.core.response.GatewayResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

public abstract class BaseExecutor implements IExecutor{


    private static final Logger logger = LoggerFactory.getLogger(BaseExecutor.class);

    private static final Logger accessLog = LoggerFactory.getLogger("accessLog");

    private Filter filter;

    protected BaseExecutor(Filter filter) {
        this.filter = filter;
    }


    @Override
    public void execute(GatewayContext ctx) {
        // 获取熔断器配置
        HystrixFilterConfig hystrixConfig = (HystrixFilterConfig)ctx.getRule().getFilterConfig(FilterConst.HYSTRIX_ID);
        if(hystrixConfig != null) {
            // 当前请求配置了熔断策略
            ctx.setHystrix(true);
            routeWithHystrix(ctx, hystrixConfig);
        }else {
            // 当前请求没有配置熔断策略
            ctx.setHystrix(false);
            route(ctx);
        }
    }


    /**
     * 当前请求配置了熔断，走此方法进行转发
     * @param gatewayContext
     * @param hystrixConfig
     */
    protected void routeWithHystrix(GatewayContext gatewayContext, HystrixFilterConfig hystrixConfig) {
        final HystrixCommand.Setter setter = HystrixCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(gatewayContext.getUniqueId()))
                .andCommandKey(HystrixCommandKey.Factory.asKey(gatewayContext.getRequest().getPath()))
                .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter()
                        .withCoreSize(hystrixConfig.getThreadCoreSize()))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                        .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.THREAD)
                        .withExecutionTimeoutInMilliseconds(hystrixConfig.getTimeoutInMilliseconds())
                        .withExecutionIsolationThreadInterruptOnTimeout(true)
                        .withExecutionTimeoutEnabled(true));
        final HystrixCommand<Object> hystrixCommand = new HystrixCommand<Object>(setter) {
            @Override
            protected Object run() throws Exception {
                // 实际执行路由
                route(gatewayContext);
                return null;
            }

            @Override
            protected Object getFallback() {
                // 当熔断发生时，执行降级逻辑
                gatewayContext.setResponse(hystrixConfig.getFallbackResponse());
                gatewayContext.written();
                return null;
            }
        };
        // 执行熔断降级
        hystrixCommand.execute();
    }


    /**
     * 调用下游服务
     * @param ctx
     */
    protected abstract void route(GatewayContext ctx);



    /**
     * 处理下游服务的响应
     * @param request
     * @param response
     * @param throwable
     * @param gatewayContext
     */
    protected void complete(Request request, Object response, Throwable throwable, GatewayContext gatewayContext) {
        // 释放请求资源
        gatewayContext.releaseRequest();
        final int currentRetryTimes = gatewayContext.getCurrentRetryTimes();
        final int confRetryTimes = gatewayContext.getRule().getRetry();
        // 当前请求超时或者出现IO异常 且 没有超过最大重试次数 且 没有配置熔断 才进行重试
        if((throwable instanceof TimeoutException || throwable instanceof IOException)
                && currentRetryTimes <= confRetryTimes && gatewayContext.isHystrix() == false) {
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
                if(response instanceof Response) {
                    // 使用AsyncHttp response对象构建网关response对象
                    gatewayContext.setResponse(GatewayResponse.buildGatewayResponse((Response) response));
                }else {
                    // 使用dubbo 构建网关response对象
                    gatewayContext.setResponse(GatewayResponse.buildGatewayResponse(response));
                }
            }
        }catch (Throwable t) {
            // 捕获其它异常
            gatewayContext.setThrowable(new ResponseException(ResponseCode.INTERNAL_ERROR));
            logger.error("complete error", t);
        }finally {
            // 不管成功失败都需要向客户端响应请求
            gatewayContext.written(); // 记录请求阶段
            ResponseHelper.writeResponse(gatewayContext); // 响应
            // 记录访问日志
            accessLog.info("{} {} {} {} {} {} {}",
                    System.currentTimeMillis() - gatewayContext.getRequest().getBeginTime(),
                    gatewayContext.getRequest().getClientIp(),
                    gatewayContext.getUniqueId(),
                    gatewayContext.getRequest().getHttpMethod(),
                    gatewayContext.getRequest().getPath(),
                    gatewayContext.getResponse().getHttpResponseStatus().code(),
                    gatewayContext.getResponse().getFutureResponse().getResponseBodyAsBytes().length);
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
            filter.doFilter(gatewayContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
