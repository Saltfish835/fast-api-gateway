package org.example.gateway.core.disruptor;


/**
 * 多生产者多消费者队列
 * @param <E>
 */
public interface ParallelQueue<E> {

    /**
     * 往队列中添加单个元素
     * @param event
     */
    void add(E event);


    /**
     * 往队列中添加多个元素
     * @param events
     */
    void add(E... events);


    /**
     * 往队列中添加单个元素
     * @param event
     * @return
     */
    boolean tryAdd(E event);


    /**
     * 往队列中添加多个元素
     * @param events
     * @return
     */
    boolean tryAdd(E... events);


    /**
     * 启动
     */
    void start();


    /**
     * 销毁
     */
    void shutdown();


    /**
     * 是否被销毁
     * @return
     */
    boolean isShutdown();

}
