package com.only.engine.redis.misc

import cn.hutool.extra.spring.SpringUtil
import org.redisson.api.*
import java.util.concurrent.CompletionStage
import java.util.concurrent.TimeUnit
import java.util.function.Function

/**
 * 分布式队列工具
 * 轻量级队列,重量级数据量请使用 MQ
 * 要求 Redis 5.X 以上
 *
 * @author LD_moxeii
 * @version 3.6.0 新增
 */
object QueueUtils {

    private val CLIENT: RedissonClient by lazy {
        SpringUtil.getBean(RedissonClient::class.java)
    }

    /**
     * 获取客户端实例
     */
    @JvmStatic
    fun getClient(): RedissonClient = CLIENT

    /**
     * 添加普通队列数据
     *
     * @param queueName 队列名
     * @param data      数据
     */
    @JvmStatic
    fun <T : Any> addQueueObject(queueName: String, data: T): Boolean {
        val queue: RBlockingQueue<T> = CLIENT.getBlockingQueue(queueName)
        return queue.offer(data)
    }

    /**
     * 通用获取一个队列数据,没有数据返回 null(不支持延迟队列)
     *
     * @param queueName 队列名
     */
    @JvmStatic
    fun <T : Any> getQueueObject(queueName: String): T? {
        val queue: RBlockingQueue<T> = CLIENT.getBlockingQueue(queueName)
        return queue.poll()
    }

    /**
     * 通用删除队列数据(不支持延迟队列)
     */
    @JvmStatic
    fun <T : Any> removeQueueObject(queueName: String, data: T): Boolean {
        val queue: RBlockingQueue<T> = CLIENT.getBlockingQueue(queueName)
        return queue.remove(data)
    }

    /**
     * 通用销毁队列,所有阻塞监听报错(不支持延迟队列)
     */
    @JvmStatic
    fun <T : Any> destroyQueue(queueName: String): Boolean {
        val queue: RBlockingQueue<T> = CLIENT.getBlockingQueue(queueName)
        return queue.delete()
    }

    /**
     * 添加延迟队列数据,默认毫秒
     *
     * @param queueName 队列名
     * @param data      数据
     * @param time      延迟时间
     */
    @JvmStatic
    fun <T : Any> addDelayedQueueObject(queueName: String, data: T, time: Long) {
        addDelayedQueueObject(queueName, data, time, TimeUnit.MILLISECONDS)
    }

    /**
     * 添加延迟队列数据
     *
     * @param queueName 队列名
     * @param data      数据
     * @param time      延迟时间
     * @param timeUnit  单位
     */
    @JvmStatic
    fun <T : Any> addDelayedQueueObject(queueName: String, data: T, time: Long, timeUnit: TimeUnit) {
        val queue: RBlockingQueue<T> = CLIENT.getBlockingQueue(queueName)
        val delayedQueue: RDelayedQueue<T> = CLIENT.getDelayedQueue(queue)
        delayedQueue.offer(data, time, timeUnit)
    }

    /**
     * 获取一个延迟队列数据,没有数据返回 null
     *
     * @param queueName 队列名
     */
    @JvmStatic
    fun <T : Any> getDelayedQueueObject(queueName: String): T? {
        val queue: RBlockingQueue<T> = CLIENT.getBlockingQueue(queueName)
        val delayedQueue: RDelayedQueue<T> = CLIENT.getDelayedQueue(queue)
        return delayedQueue.poll()
    }

    /**
     * 删除延迟队列数据
     */
    @JvmStatic
    fun <T : Any> removeDelayedQueueObject(queueName: String, data: T): Boolean {
        val queue: RBlockingQueue<T> = CLIENT.getBlockingQueue(queueName)
        val delayedQueue: RDelayedQueue<T> = CLIENT.getDelayedQueue(queue)
        return delayedQueue.remove(data)
    }

    /**
     * 销毁延迟队列,所有阻塞监听报错
     */
    @JvmStatic
    fun <T : Any> destroyDelayedQueue(queueName: String) {
        val queue: RBlockingQueue<T> = CLIENT.getBlockingQueue(queueName)
        val delayedQueue: RDelayedQueue<T> = CLIENT.getDelayedQueue(queue)
        delayedQueue.destroy()
    }

    /**
     * 添加优先队列数据
     *
     * @param queueName 队列名
     * @param data      数据
     */
    @JvmStatic
    fun <T : Any> addPriorityQueueObject(queueName: String, data: T): Boolean {
        val priorityBlockingQueue: RPriorityBlockingQueue<T> = CLIENT.getPriorityBlockingQueue(queueName)
        return priorityBlockingQueue.offer(data)
    }

    /**
     * 优先队列获取一个队列数据,没有数据返回 null(不支持延迟队列)
     *
     * @param queueName 队列名
     */
    @JvmStatic
    fun <T : Any> getPriorityQueueObject(queueName: String): T? {
        val queue: RPriorityBlockingQueue<T> = CLIENT.getPriorityBlockingQueue(queueName)
        return queue.poll()
    }

    /**
     * 优先队列删除队列数据(不支持延迟队列)
     */
    @JvmStatic
    fun <T : Any> removePriorityQueueObject(queueName: String, data: T): Boolean {
        val queue: RPriorityBlockingQueue<T> = CLIENT.getPriorityBlockingQueue(queueName)
        return queue.remove(data)
    }

    /**
     * 优先队列销毁队列,所有阻塞监听报错(不支持延迟队列)
     */
    @JvmStatic
    fun <T : Any> destroyPriorityQueue(queueName: String): Boolean {
        val queue: RPriorityBlockingQueue<T> = CLIENT.getPriorityBlockingQueue(queueName)
        return queue.delete()
    }

    /**
     * 尝试设置有界队列容量,用于限制数量
     *
     * @param queueName 队列名
     * @param capacity  容量
     */
    @JvmStatic
    fun <T : Any> trySetBoundedQueueCapacity(queueName: String, capacity: Int): Boolean {
        val boundedBlockingQueue: RBoundedBlockingQueue<T> = CLIENT.getBoundedBlockingQueue(queueName)
        return boundedBlockingQueue.trySetCapacity(capacity)
    }

    /**
     * 尝试设置有界队列容量,用于限制数量
     *
     * @param queueName 队列名
     * @param capacity  容量
     * @param destroy   是否销毁
     */
    @JvmStatic
    fun <T : Any> trySetBoundedQueueCapacity(queueName: String, capacity: Int, destroy: Boolean): Boolean {
        val boundedBlockingQueue: RBoundedBlockingQueue<T> = CLIENT.getBoundedBlockingQueue(queueName)
        if (destroy) {
            boundedBlockingQueue.delete()
        }
        return boundedBlockingQueue.trySetCapacity(capacity)
    }

    /**
     * 添加有界队列数据
     *
     * @param queueName 队列名
     * @param data      数据
     * @return 添加成功 true,已达到界限 false
     */
    @JvmStatic
    fun <T : Any> addBoundedQueueObject(queueName: String, data: T): Boolean {
        val boundedBlockingQueue: RBoundedBlockingQueue<T> = CLIENT.getBoundedBlockingQueue(queueName)
        return boundedBlockingQueue.offer(data)
    }

    /**
     * 有界队列获取一个队列数据,没有数据返回 null(不支持延迟队列)
     *
     * @param queueName 队列名
     */
    @JvmStatic
    fun <T : Any> getBoundedQueueObject(queueName: String): T? {
        val queue: RBoundedBlockingQueue<T> = CLIENT.getBoundedBlockingQueue(queueName)
        return queue.poll()
    }

    /**
     * 有界队列删除队列数据(不支持延迟队列)
     */
    @JvmStatic
    fun <T : Any> removeBoundedQueueObject(queueName: String, data: T): Boolean {
        val queue: RBoundedBlockingQueue<T> = CLIENT.getBoundedBlockingQueue(queueName)
        return queue.remove(data)
    }

    /**
     * 有界队列销毁队列,所有阻塞监听报错(不支持延迟队列)
     */
    @JvmStatic
    fun <T : Any> destroyBoundedQueue(queueName: String): Boolean {
        val queue: RBoundedBlockingQueue<T> = CLIENT.getBoundedBlockingQueue(queueName)
        return queue.delete()
    }

    /**
     * 订阅阻塞队列(可订阅所有实现类,例如: 延迟、优先、有界等)
     */
    @JvmStatic
    fun <T : Any> subscribeBlockingQueue(
        queueName: String,
        consumer: Function<T, CompletionStage<Void>>,
        isDelayed: Boolean,
    ) {
        val queue: RBlockingQueue<T> = CLIENT.getBlockingQueue(queueName)
        if (isDelayed) {
            // 订阅延迟队列
            CLIENT.getDelayedQueue(queue)
        }
        queue.subscribeOnElements(consumer)
    }
}
