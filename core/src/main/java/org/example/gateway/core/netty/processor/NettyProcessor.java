package org.example.gateway.core.netty.processor;

import org.example.gateway.core.context.HttpRequestWrapper;

/**
 * 处理netty请求的核心逻辑
 */
public interface NettyProcessor {

    void process(HttpRequestWrapper requestWrapper);
}
