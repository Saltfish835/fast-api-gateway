package org.example.gateway.core;

import com.lmax.disruptor.*;
import org.example.gateway.config.center.api.ConfigCenter;
import org.example.gateway.core.filter.Filter;
import org.example.gateway.core.predicates.Predicate;
import org.example.gateway.register.center.api.RegisterCenter;

import java.util.concurrent.ConcurrentHashMap;

public class Config {

    private int port = 10010;

    private int prometheusPort = 18000;

    private String applicationName = "fast-api-gateway";

    private String registryAddress = "127.0.0.1:8848";

    private String env = "dev";

    private int eventLoopGroupBossNum = 2;

    private int eventLoopGroupWorkerNum = Runtime.getRuntime().availableProcessors();

    private int maxContentLength = 64 * 1024 * 1024;

    /**
     * 单异步还是双异步， 默认为单异步
     */
    private boolean whenComplete = true;

    /**
     * 连接超时时间
     */
    private int httpConnectTimeout = 30 * 1000;

    /**
     * 请求超时时间
     */
    private int httpRequestTimeout = 30 * 1000;

    /**
     * 客户端请求重试次数
     */
    private int httpMaxRequestRetry = 2;

    /**
     * 客户端请求最大连接数
     */
    private int httpMaxContentions = 10000;

    /**
     * 客户端每个地址支持的最大连接数
     */
    private int httpConnectionsPerHost = 8000;

    /**
     * 客户端空闲连接超时时间
     */
    private int httpPooledConnectionIdleTimeout = 60 * 1000;

    /**
     * 是否开启多生产者和多消费者的并行队列
     */
    private String bufferType = "parallel";

    /**
     * 环形缓冲区大小
     */
    private int bufferSize = 1024 * 64;

    /**
     * 无锁队列线程数
     */
    private int processThread = Runtime.getRuntime().availableProcessors();

    /**
     * disruptor生成者等待消费者的策略
     */
    private String waitStrategy = "blocking";

    /**
     * 配置中心插件
     */
    private ConfigCenter configCenter;

    /**
     * 注册中心插件
     */
    private RegisterCenter registerCenter;

    /**
     * 过滤器插件
     */
    private ConcurrentHashMap<String, Filter> filterMap;

    /**
     * predicate插件
     */
    private ConcurrentHashMap<String, Predicate> predicateMap;


    public WaitStrategy getWaitStrategy() {
        switch (waitStrategy) {
            case "blocking":
                return new BlockingWaitStrategy();
            case "busySpin":
                return new BusySpinWaitStrategy();
            case "yielding":
                return new YieldingWaitStrategy();
            case "sleeping":
                return new SleepingWaitStrategy();
            default:
                return new BlockingWaitStrategy();
        }
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getRegistryAddress() {
        return registryAddress;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public int getEventLoopGroupBossNum() {
        return eventLoopGroupBossNum;
    }

    public void setEventLoopGroupBossNum(int eventLoopGroupBossNum) {
        this.eventLoopGroupBossNum = eventLoopGroupBossNum;
    }

    public int getEventLoopGroupWorkerNum() {
        return eventLoopGroupWorkerNum;
    }

    public void setEventLoopGroupWorkerNum(int eventLoopGroupWorkerNum) {
        this.eventLoopGroupWorkerNum = eventLoopGroupWorkerNum;
    }

    public int getMaxContentLength() {
        return maxContentLength;
    }

    public void setMaxContentLength(int maxContentLength) {
        this.maxContentLength = maxContentLength;
    }

    public boolean isWhenComplete() {
        return whenComplete;
    }

    public void setWhenComplete(boolean whenComplete) {
        this.whenComplete = whenComplete;
    }

    public int getHttpConnectTimeout() {
        return httpConnectTimeout;
    }

    public void setHttpConnectTimeout(int httpConnectTimeout) {
        this.httpConnectTimeout = httpConnectTimeout;
    }

    public int getHttpRequestTimeout() {
        return httpRequestTimeout;
    }

    public void setHttpRequestTimeout(int httpRequestTimeout) {
        this.httpRequestTimeout = httpRequestTimeout;
    }

    public int getHttpMaxRequestRetry() {
        return httpMaxRequestRetry;
    }

    public void setHttpMaxRequestRetry(int httpMaxRequestRetry) {
        this.httpMaxRequestRetry = httpMaxRequestRetry;
    }

    public int getHttpMaxContentions() {
        return httpMaxContentions;
    }

    public void setHttpMaxContentions(int httpMaxContentions) {
        this.httpMaxContentions = httpMaxContentions;
    }

    public int getHttpConnectionsPerHost() {
        return httpConnectionsPerHost;
    }

    public void setHttpConnectionsPerHost(int httpConnectionsPerHost) {
        this.httpConnectionsPerHost = httpConnectionsPerHost;
    }

    public int getHttpPooledConnectionIdleTimeout() {
        return httpPooledConnectionIdleTimeout;
    }

    public void setHttpPooledConnectionIdleTimeout(int httpPooledConnectionIdleTimeout) {
        this.httpPooledConnectionIdleTimeout = httpPooledConnectionIdleTimeout;
    }

    public int getPrometheusPort() {
        return prometheusPort;
    }

    public void setPrometheusPort(int prometheusPort) {
        this.prometheusPort = prometheusPort;
    }

    public String getBufferType() {
        return bufferType;
    }

    public void setBufferType(String bufferType) {
        this.bufferType = bufferType;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public int getProcessThread() {
        return processThread;
    }

    public void setProcessThread(int processThread) {
        this.processThread = processThread;
    }

    public ConfigCenter getConfigCenter() {
        return configCenter;
    }

    public void setConfigCenter(ConfigCenter configCenter) {
        this.configCenter = configCenter;
    }

    public RegisterCenter getRegisterCenter() {
        return registerCenter;
    }

    public void setRegisterCenter(RegisterCenter registerCenter) {
        this.registerCenter = registerCenter;
    }

    public ConcurrentHashMap<String, Filter> getFilterMap() {
        return filterMap;
    }

    public void setFilterMap(ConcurrentHashMap<String, Filter> filterMap) {
        this.filterMap = filterMap;
    }

    public ConcurrentHashMap<String, Predicate> getPredicateMap() {
        return predicateMap;
    }

    public void setPredicateMap(ConcurrentHashMap<String, Predicate> predicateMap) {
        this.predicateMap = predicateMap;
    }
}
