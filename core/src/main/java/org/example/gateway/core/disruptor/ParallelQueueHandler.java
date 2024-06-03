package org.example.gateway.core.disruptor;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.ProducerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 基于Disruptor实现的多生产者多消费者的无锁队列
 */
public class ParallelQueueHandler<E> implements ParallelQueue<E> {


    private static final Logger logger = LoggerFactory.getLogger(ParallelQueueHandler.class);


    private RingBuffer ringBuffer;

    private EventListener<E> eventListener;

    private WorkerPool<Holder> workerPool;

    private ExecutorService executorService;

    private EventTranslatorOneArg<Holder, E> eventTranslatorOneArg;


    /**
     * 创建多生产者多消费者模式
     * @param builder
     */
    public ParallelQueueHandler(Builder<E> builder) {
        this.executorService = Executors.newFixedThreadPool(builder.threads,
                new ThreadFactoryBuilder().setNameFormat("ParallelQueueHandler "+builder.namePrefix+"-pool-%d").build());
        this.eventListener = builder.listener;
        this.eventTranslatorOneArg = new HolderEventTranslator();
        // 创建ring buffer
        final RingBuffer<Holder> ringBuffer = RingBuffer.create(builder.producerType, new HolderEventFactory(), builder.bufferSize, builder.waitStrategy);
        // 通过ring buffer创建屏障
        final SequenceBarrier sequenceBarrier = ringBuffer.newBarrier();
        // 创建多个消费者组
        final WorkHandler[] workHandlers = new WorkHandler[builder.threads];
        for(int i=0; i < workHandlers.length; i++) {
            workHandlers[i] = new HolderWorkHandler();
        }
        // 创建多个消费者线程池
        final WorkerPool<Holder> workerPool = new WorkerPool<>(ringBuffer, sequenceBarrier, new HolderExceptionHandler(), workHandlers);
        // 设置多个消费者的sequence序号，用于统计消费进度
        ringBuffer.addGatingSequences(workerPool.getWorkerSequences());
        this.workerPool = workerPool;
    }


    /**
     * 生产消息
     * @param event
     */
    @Override
    public void add(E event) {
        RingBuffer<Holder> holderRingBuffer = ringBuffer;
        if(holderRingBuffer == null) {
            process(this.eventListener, new IllegalStateException("ParallelQueueHandler is close"), event);
        }
        try {
            // 往环形缓冲区发布事件
            logger.info("publish message: {}", event);
            ringBuffer.publishEvent(this.eventTranslatorOneArg, event);
        }catch (NullPointerException e) {
            process(this.eventListener, new IllegalStateException("ParallelQueueHandler is close"), event);
        }
    }


    /**
     * 消费消息
     */
    private class HolderWorkHandler implements WorkHandler<Holder>{
        // 当有消息推送到环形队列时，此方法会收到通知被调用
        @Override
        public void onEvent(Holder holder) throws Exception {
            // 调用自定义的具体处理消息的方法
            logger.info("consume message: {}", holder.getEvent());
            eventListener.onEvent(holder.getEvent());
            holder.setEvent(null);
        }
    }

    @Override
    public void add(E... events) {
        RingBuffer<Holder> holderRingBuffer = ringBuffer;
        if(holderRingBuffer == null) {
            process(this.eventListener, new IllegalStateException("ParallelQueueHandler is close"), events);
        }
        try {
            ringBuffer.publishEvents(this.eventTranslatorOneArg, events);
        }catch (NullPointerException e) {
            process(this.eventListener, new IllegalStateException("ParallelQueueHandler is close"), events);
        }
    }

    @Override
    public boolean tryAdd(E event) {
        RingBuffer<Holder> holderRingBuffer = ringBuffer;
        if(holderRingBuffer == null) {
            return false;
        }
        try {
            return ringBuffer.tryPublishEvent(this.eventTranslatorOneArg, event);
        }catch (NullPointerException e) {
            return false;
        }
    }

    @Override
    public boolean tryAdd(E... events) {
        RingBuffer<Holder> holderRingBuffer = ringBuffer;
        if(holderRingBuffer == null) {
            return false;
        }
        try {
            return ringBuffer.tryPublishEvents(this.eventTranslatorOneArg, events);
        }catch (NullPointerException e) {
            return false;
        }
    }

    @Override
    public void start() {
        this.ringBuffer = workerPool.start(executorService);
        logger.info("disruptor start");
    }

    @Override
    public void shutdown() {
        RingBuffer<Holder> holderRingBuffer = ringBuffer;
        ringBuffer = null;
        if(holderRingBuffer == null) {
            return;
        }
        if(workerPool != null) {
            workerPool.drainAndHalt();
        }
        if(executorService != null) {
            executorService.shutdown();
        }
    }

    @Override
    public boolean isShutdown() {
        return ringBuffer == null;
    }

    private static <E> void process(EventListener<E> listener, Throwable e, E event) {
        listener.onException(e, -1, event);
    }

    private static <E> void process(EventListener<E> listener, Throwable e, E... events) {
        for(E event : events) {
            process(listener, e, event);
        }
    }


    /**
     * 消息对象
     */
    private class Holder {
        private E event;

        public Holder() {
        }

        public Holder(E event) {
            this.event = event;
        }

        public E getEvent() {
            return event;
        }

        public void setEvent(E event) {
            this.event = event;
        }

        @Override
        public String toString() {
            return "Holder{" +
                    "event=" + event.toString() +
                    '}';
        }
    }


    /**
     * 建造者模式
     * @param <E>
     */
    public static class Builder<E> {
        private ProducerType producerType = ProducerType.MULTI;
        private int bufferSize = 1024 * 16;
        private int threads = 1;
        private String namePrefix = "";
        private WaitStrategy waitStrategy = new BlockingWaitStrategy();
        private EventListener<E> listener;

        public Builder setProducerType(ProducerType producerType) {
            Preconditions.checkNotNull(producerType);
            this.producerType = producerType;
            return this;
        }

        public Builder setBufferSize(int bufferSize) {
            Preconditions.checkArgument(Integer.bitCount(bufferSize) == 1);
            this.bufferSize = bufferSize;
            return this;
        }

        public Builder setThreads(int threads) {
            Preconditions.checkArgument(threads > 0);
            this.threads = threads;
            return this;
        }

        public Builder setNamePrefix(String namePrefix) {
            Preconditions.checkNotNull(namePrefix);
            this.namePrefix = namePrefix;
            return this;
        }

        public Builder setWaitStrategy(WaitStrategy waitStrategy) {
            Preconditions.checkNotNull(waitStrategy);
            this.waitStrategy = waitStrategy;
            return this;
        }

        public Builder setListener(EventListener<E> listener) {
            Preconditions.checkNotNull(listener);
            this.listener = listener;
            return this;
        }

        public ParallelQueueHandler<E> build() {
            return new ParallelQueueHandler<>(this);
        }
    }

    private class HolderEventTranslator implements  EventTranslatorOneArg<Holder,E>{
        @Override
        public void translateTo(Holder holder, long l, E e) {
            holder.setEvent(e);
        }
    }

    private class HolderEventFactory implements  EventFactory<Holder>{

        @Override
        public Holder newInstance() {
            return new Holder();
        }
    }

    /**
     * 事件处理器
     */
    private class HolderExceptionHandler implements ExceptionHandler<Holder> {
        @Override
        public void handleEventException(Throwable throwable, long l, Holder holder) {
            try{
                eventListener.onException(throwable, l, holder.getEvent());
            }catch (Exception e) {

            }finally {
                holder.setEvent(null);
            }
        }

        @Override
        public void handleOnStartException(Throwable throwable) {
            throw new UnsupportedOperationException(throwable);
        }

        @Override
        public void handleOnShutdownException(Throwable throwable) {
            throw new UnsupportedOperationException(throwable);
        }
    }
}
