package org.example.gateway.core.netty.processor;

import com.lmax.disruptor.dsl.ProducerType;
import org.example.gateway.core.Config;
import org.example.gateway.core.context.HttpRequestWrapper;
import org.example.gateway.core.disruptor.BatchEventListenerProcessor;
import org.example.gateway.core.disruptor.ParallelQueueHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Disruptor流程处理类
 */
public class DisruptorNettyCoreProcessor implements NettyProcessor{

    private static final Logger logger = LoggerFactory.getLogger(DisruptorNettyCoreProcessor.class);

    private static final String THREAD_NAME_PREFIX = "gateway-queue-";

    private Config config;

    private NettyCoreProcessor nettyCoreProcessor;

    private ParallelQueueHandler<HttpRequestWrapper> parallelQueueHandler;

    public DisruptorNettyCoreProcessor(Config config, NettyCoreProcessor nettyCoreProcessor) {
        this.config = config;
        this.nettyCoreProcessor = nettyCoreProcessor;
        // 初始化多生产者多消费者无锁队列
        ParallelQueueHandler.Builder builder = new ParallelQueueHandler.Builder().setBufferSize(config.getBufferSize()) // 环形缓冲区的大小
                .setThreads(this.config.getProcessThread()) // 处理器线程池大小
                .setProducerType(ProducerType.MULTI) // 生产者类型 单生产者 or 多生产者
                .setNamePrefix(THREAD_NAME_PREFIX)
                .setWaitStrategy(config.getWaitStrategy()) // 消费者阻塞策略
                .setListener(new BatchEventListenerProcessor(this.nettyCoreProcessor)); // 事件处理器
        // 创建多生产者多消费者无锁队列
        this.parallelQueueHandler = builder.build();
    }

    /**
     * 处理请求
     * @param requestWrapper
     */
    @Override
    public void process(HttpRequestWrapper requestWrapper) {
        // 将请求交给并行队列处理器
        this.parallelQueueHandler.add(requestWrapper);
    }


    @Override
    public void start() {
        parallelQueueHandler.start();
    }

    @Override
    public void shutdown() {
        parallelQueueHandler.shutdown();
    }
}
