package org.example.gateway.core.filter.router.executor;

import org.asynchttpclient.Request;
import org.asynchttpclient.Response;
import org.example.gateway.common.config.ServiceInstance;
import org.example.gateway.common.constants.BasicConst;
import org.example.gateway.common.exception.NotFoundException;
import org.example.gateway.core.ConfigLoader;
import org.example.gateway.core.context.GatewayContext;
import org.example.gateway.core.filter.Filter;
import org.example.gateway.core.helper.AsyncHttpHelper;
import org.example.gateway.core.request.GatewayRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

import static org.example.gateway.common.enums.ResponseCode.SERVICE_INSTANCE_NOT_FOUND;

public class HttpExecutor extends BaseExecutor{

    private static final Logger logger = LoggerFactory.getLogger(HttpExecutor.class);

    private static HttpExecutor instance;

    private HttpExecutor(Filter filter) {
        super(filter);
    }

    public static HttpExecutor getInstance(Filter filter) {
        if(instance == null) {
            synchronized (HttpExecutor.class) {
                instance = new HttpExecutor(filter);
            }
        }
        return instance;
    }


    /**
     * 调用下游的http服务
     * @param ctx
     * @return
     */
    public void route(GatewayContext ctx) {
        final ServiceInstance serviceInstance = ctx.getServiceInstance();
        final GatewayRequest request = ctx.getRequest();
        if (serviceInstance != null && request != null) {
            String host = serviceInstance.getIp() + BasicConst.COLON_SEPARATOR + serviceInstance.getPort();
            request.setModifyHost(host);
        } else {
            logger.warn("No instance available for :{}", ctx.getUniqueId());
            throw new NotFoundException(SERVICE_INSTANCE_NOT_FOUND);
        }
        // 构建http请求对象
        final Request req = ctx.getRequest().build();
        // 向下游服务发送http请求
        final CompletableFuture<Response> future = AsyncHttpHelper.getInstance().executeRequest(req);
        final boolean whenComplete = ConfigLoader.getConfig().isWhenComplete();
        if(whenComplete) {
            // 使用与发送请求相同的线程来处理下游服务的响应
            future.whenComplete(((response, throwable) -> {
                // 处理响应
                complete(req, response, throwable, ctx);
            }));
        }else {
            // 使用与发送请求不同的线程来处理下游服务的响应
            future.whenCompleteAsync(((response, throwable) -> {
                // 处理响应
                complete(req, response, throwable, ctx);
            }));
        }
    }
}
