package org.example.gateway.core.disruptor;


public interface ParallelQueue<E> {

    /**
     * 添加单个元素
     * @param event
     */
    void add(E event);


    /**
     * 添加多个元素
     * @param events
     */
    void add(E... events);


    /**
     * 添加单个元素
     * @param event
     * @return
     */
    boolean tryAdd(E event);


    /**
     * 添加多个元素
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
