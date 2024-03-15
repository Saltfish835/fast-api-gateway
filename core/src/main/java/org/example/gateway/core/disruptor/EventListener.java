package org.example.gateway.core.disruptor;

/**
 * 监听器接口
 * @param <E>
 */
public interface EventListener<E> {

    /**
     * 正常回调
     * @param event
     */
    void onEvent(E event);


    /**
     * 异常回调
     * @param ex
     * @param sequence
     * @param event
     */
    void onException(Throwable ex, long sequence, E event);

}
