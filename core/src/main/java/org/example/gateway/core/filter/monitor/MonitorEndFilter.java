package org.example.gateway.core.filter.monitor;

import com.alibaba.fastjson.JSONObject;
import com.sun.net.httpserver.HttpServer;
import io.micrometer.core.instrument.Timer;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.example.gateway.common.config.FilterConfig;
import org.example.gateway.common.constants.FilterConst;
import org.example.gateway.core.ConfigLoader;
import org.example.gateway.core.context.GatewayContext;
import org.example.gateway.core.filter.Filter;
import org.example.gateway.core.filter.FilterAspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.net.InetSocketAddress;

@FilterAspect(id= FilterConst.MONITOR_END_FILTER_ID, name = FilterConst.MONITOR_END_FILTER_NAME, order = FilterConst.MONITOR_END_FILTER_ORDER)
public class MonitorEndFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(MonitorEndFilter.class);


    // 普罗米修斯的注册表
    private final PrometheusMeterRegistry prometheusMeterRegistry;

    public MonitorEndFilter() {
        this.prometheusMeterRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        try {
            // 暴露接口来提供普罗米修斯指标数据拉取
            final HttpServer httpServer = HttpServer.create(new InetSocketAddress(ConfigLoader.getConfig().getPrometheusPort()), 0);
            // 暴露路径
            httpServer.createContext("/prometheus", httpExchange -> {
                // 从普罗米修斯注册表中获取数据
                final String scrape = prometheusMeterRegistry.scrape();
                // 响应普罗米修斯数据
                httpExchange.sendResponseHeaders(200, scrape.getBytes().length);
                try(final OutputStream outputStream = httpExchange.getResponseBody()) {
                    outputStream.write(scrape.getBytes());
                }
            });
            // 用一个单独的线程来启动 monitor server
            final Thread monitorServerThread = new Thread() {
                @Override
                public void run() {
                    httpServer.start();
                }
            };
            monitorServerThread.setName("monitor server thread");
            monitorServerThread.start();
        }catch (Exception e) {
            logger.error("prometheus http server start error", e);
            throw new RuntimeException(e);
        }
        logger.info("prometheus http server start successful, port: {}", ConfigLoader.getConfig().getPort());
    }

    @Override
    public void doFilter(GatewayContext ctx) throws Exception {
        logger.debug("MonitorEndFilter: {}", ctx.toString());
        final Timer timer = prometheusMeterRegistry.timer("gateway_request", "uniqueId", ctx.getUniqueId(),
                "protocol", ctx.getProtocol(), "path", ctx.getRequest().getPath());
        ctx.getTimerSample().stop(timer);
    }

    @Override
    public FilterConfig toFilterConfig(JSONObject filterConfigJsonObj) {
        return null;
    }
}
