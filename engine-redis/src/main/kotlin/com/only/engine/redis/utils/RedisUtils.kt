package com.only.engine.redis.utils

import cn.hutool.extra.spring.SpringUtil
import org.redisson.api.*
import java.time.Duration
import java.util.function.Consumer
import java.util.stream.Collectors
import java.util.stream.Stream

/**
 * Redis 工具类
 *
 * @author LD_moxeii
 * @version 3.1.0 新增
 */
@Suppress("UNCHECKED_CAST")
object RedisUtils {

    private val CLIENT: RedissonClient by lazy {
        SpringUtil.getBean(RedissonClient::class.java)
    }

    /**
     * 限流
     *
     * @param key          限流 key
     * @param rateType     限流类型
     * @param rate         速率
     * @param rateInterval 速率间隔
     * @return -1 表示失败
     */
    @JvmStatic
    fun rateLimiter(key: String, rateType: RateType, rate: Int, rateInterval: Int): Long =
        rateLimiter(key, rateType, rate, rateInterval, 0)

    /**
     * 限流
     *
     * @param key          限流 key
     * @param rateType     限流类型
     * @param rate         速率
     * @param rateInterval 速率间隔
     * @param timeout      超时时间
     * @return -1 表示失败
     */
    @JvmStatic
    fun rateLimiter(key: String, rateType: RateType, rate: Int, rateInterval: Int, timeout: Int): Long {
        val rateLimiter = CLIENT.getRateLimiter(key)
        rateLimiter.trySetRate(rateType, rate.toLong(), rateInterval.toLong(), RateIntervalUnit.SECONDS)
        return if (rateLimiter.tryAcquire()) {
            rateLimiter.availablePermits()
        } else {
            -1L
        }
    }

    /**
     * 获取客户端实例
     */
    @JvmStatic
    fun getClient(): RedissonClient = CLIENT

    /**
     * 发布通道消息
     *
     * @param channelKey 通道 key
     * @param msg        发送数据
     * @param consumer   自定义处理
     */
    @JvmStatic
    fun <T> publish(channelKey: String, msg: T, consumer: Consumer<T>) {
        val topic: RTopic = CLIENT.getTopic(channelKey)
        topic.publish(msg)
        consumer.accept(msg)
    }

    /**
     * 发布消息到指定的频道
     *
     * @param channelKey 通道 key
     * @param msg        发送数据
     */
    @JvmStatic
    fun <T> publish(channelKey: String, msg: T) {
        val topic: RTopic = CLIENT.getTopic(channelKey)
        topic.publish(msg)
    }

    /**
     * 订阅通道接收消息
     *
     * @param channelKey 通道 key
     * @param clazz      消息类型
     * @param consumer   自定义处理
     */
    @JvmStatic
    fun <T> subscribe(channelKey: String, clazz: Class<T>, consumer: Consumer<T>) {
        val topic: RTopic = CLIENT.getTopic(channelKey)
        topic.addListener(clazz) { _, msg -> consumer.accept(msg) }
    }

    /**
     * 缓存基本的对象,Integer、String、实体类等
     *
     * @param key   缓存的键值
     * @param value 缓存的值
     */
    @JvmStatic
    fun <T> setCacheObject(key: String, value: T) = setCacheObject(key, value, false)

    /**
     * 缓存基本的对象,保留当前对象 TTL 有效期
     *
     * @param key       缓存的键值
     * @param value     缓存的值
     * @param isSaveTtl 是否保留 TTL 有效期(例如: set 之前 ttl 剩余 90, set 之后还是为 90)
     * @since Redis 6.X 以上使用 setAndKeepTTL 兼容 5.X 方案
     */
    @JvmStatic
    fun <T> setCacheObject(key: String, value: T, isSaveTtl: Boolean) {
        val bucket: RBucket<T> = CLIENT.getBucket(key)
        if (isSaveTtl) {
            try {
                bucket.setAndKeepTTL(value)
            } catch (e: Exception) {
                val timeToLive = bucket.remainTimeToLive()
                if (timeToLive == -1L) {
                    setCacheObject(key, value)
                } else {
                    setCacheObject(key, value, Duration.ofMillis(timeToLive))
                }
            }
        } else {
            bucket.set(value)
        }
    }

    /**
     * 缓存基本的对象,Integer、String、实体类等
     *
     * @param key      缓存的键值
     * @param value    缓存的值
     * @param duration 时间
     */
    @JvmStatic
    fun <T> setCacheObject(key: String, value: T, duration: Duration) {
        val batch = CLIENT.createBatch()
        val bucket: RBucketAsync<T> = batch.getBucket(key)
        bucket.setAsync(value)
        bucket.expireAsync(duration)
        batch.execute()
    }

    /**
     * 如果不存在则设置并返回 true,如果存在则返回 false
     *
     * @param key   缓存的键值
     * @param value 缓存的值
     * @return set 成功或失败
     */
    @JvmStatic
    fun <T> setObjectIfAbsent(key: String, value: T, duration: Duration): Boolean {
        val bucket: RBucket<T> = CLIENT.getBucket(key)
        return bucket.setIfAbsent(value, duration)
    }

    /**
     * 如果存在则设置并返回 true,如果不存在则返回 false
     *
     * @param key   缓存的键值
     * @param value 缓存的值
     * @return set 成功或失败
     */
    @JvmStatic
    fun <T> setObjectIfExists(key: String, value: T, duration: Duration): Boolean {
        val bucket: RBucket<T> = CLIENT.getBucket(key)
        return bucket.setIfExists(value, duration)
    }

    /**
     * 注册对象监听器
     *
     * key 监听器需开启 `notify-keyspace-events` 等 redis 相关配置
     *
     * @param key      缓存的键值
     * @param listener 监听器配置
     */
    @JvmStatic
    fun <T> addObjectListener(key: String, listener: ObjectListener) {
        val result: RBucket<T> = CLIENT.getBucket(key)
        result.addListener(listener)
    }

    /**
     * 设置有效时间
     *
     * @param key     Redis 键
     * @param timeout 超时时间
     * @return true=设置成功; false=设置失败
     */
    @JvmStatic
    fun expire(key: String, timeout: Long): Boolean {
        return expire(key, Duration.ofSeconds(timeout))
    }

    /**
     * 设置有效时间
     *
     * @param key      Redis 键
     * @param duration 超时时间
     * @return true=设置成功; false=设置失败
     */
    @JvmStatic
    fun expire(key: String, duration: Duration): Boolean {
        val rBucket: RBucket<Any> = CLIENT.getBucket(key)
        return rBucket.expire(duration)
    }

    /**
     * 获得缓存的基本对象
     *
     * @param key 缓存键值
     * @return 缓存键值对应的数据
     */
    @JvmStatic
    fun <T> getCacheObject(key: String): T? {
        val rBucket: RBucket<T> = CLIENT.getBucket(key)
        return rBucket.get()
    }

    /**
     * 获得 key 剩余存活时间
     *
     * @param key 缓存键值
     * @return 剩余存活时间
     */
    @JvmStatic
    fun <T> getTimeToLive(key: String): Long {
        val rBucket: RBucket<T> = CLIENT.getBucket(key)
        return rBucket.remainTimeToLive()
    }

    /**
     * 删除单个对象
     *
     * @param key 缓存的键值
     */
    @JvmStatic
    fun deleteObject(key: String): Boolean = CLIENT.getBucket<Any>(key).delete()

    /**
     * 删除集合对象
     *
     * @param collection 多个对象
     */
    @JvmStatic
    fun deleteObject(collection: Collection<*>) {
        val batch = CLIENT.createBatch()
        collection.forEach { t ->
            batch.getBucket<Any>(t.toString()).deleteAsync()
        }
        batch.execute()
    }

    /**
     * 检查缓存对象是否存在
     *
     * @param key 缓存的键值
     */
    @JvmStatic
    fun isExistsObject(key: String): Boolean = CLIENT.getBucket<Any>(key).isExists

    /**
     * 缓存 List 数据
     *
     * @param key      缓存的键值
     * @param dataList 待缓存的 List 数据
     * @return 缓存的对象
     */
    @JvmStatic
    fun <T> setCacheList(key: String, dataList: List<T>): Boolean {
        val rList: RList<T> = CLIENT.getList(key)
        return rList.addAll(dataList)
    }

    /**
     * 追加缓存 List 数据
     *
     * @param key  缓存的键值
     * @param data 待缓存的数据
     * @return 缓存的对象
     */
    @JvmStatic
    fun <T> addCacheList(key: String, data: T): Boolean {
        val rList: RList<T> = CLIENT.getList(key)
        return rList.add(data)
    }

    /**
     * 注册 List 监听器
     *
     * key 监听器需开启 `notify-keyspace-events` 等 redis 相关配置
     *
     * @param key      缓存的键值
     * @param listener 监听器配置
     */
    @JvmStatic
    fun <T> addListListener(key: String, listener: ObjectListener) {
        val rList: RList<T> = CLIENT.getList(key)
        rList.addListener(listener)
    }

    /**
     * 获得缓存的 list 对象
     *
     * @param key 缓存的键值
     * @return 缓存键值对应的数据
     */
    @JvmStatic
    fun <T> getCacheList(key: String): List<T> {
        val rList: RList<T> = CLIENT.getList(key)
        return rList.readAll()
    }

    /**
     * 获得缓存的 list 对象(范围)
     *
     * @param key  缓存的键值
     * @param from 起始下标
     * @param to   截止下标
     * @return 缓存键值对应的数据
     */
    @JvmStatic
    fun <T> getCacheListRange(key: String, from: Int, to: Int): List<T> {
        val rList: RList<T> = CLIENT.getList(key)
        return rList.range(from, to)
    }

    /**
     * 缓存 Set
     *
     * @param key     缓存键值
     * @param dataSet 缓存的数据
     * @return 缓存数据的对象
     */
    @JvmStatic
    fun <T> setCacheSet(key: String, dataSet: Set<T>): Boolean {
        val rSet: RSet<T> = CLIENT.getSet(key)
        return rSet.addAll(dataSet)
    }

    /**
     * 追加缓存 Set 数据
     *
     * @param key  缓存的键值
     * @param data 待缓存的数据
     * @return 缓存的对象
     */
    @JvmStatic
    fun <T> addCacheSet(key: String, data: T): Boolean {
        val rSet: RSet<T> = CLIENT.getSet(key)
        return rSet.add(data)
    }

    /**
     * 注册 Set 监听器
     *
     * key 监听器需开启 `notify-keyspace-events` 等 redis 相关配置
     *
     * @param key      缓存的键值
     * @param listener 监听器配置
     */
    @JvmStatic
    fun <T> addSetListener(key: String, listener: ObjectListener) {
        val rSet: RSet<T> = CLIENT.getSet(key)
        rSet.addListener(listener)
    }

    /**
     * 获得缓存的 set
     *
     * @param key 缓存的 key
     * @return set 对象
     */
    @JvmStatic
    fun <T> getCacheSet(key: String): Set<T> {
        val rSet: RSet<T> = CLIENT.getSet(key)
        return rSet.readAll()
    }

    /**
     * 缓存 Map
     *
     * @param key     缓存的键值
     * @param dataMap 缓存的数据
     */
    @JvmStatic
    fun <T> setCacheMap(key: String, dataMap: Map<String, T>?) {
        if (dataMap != null) {
            val rMap: RMap<String, T> = CLIENT.getMap(key)
            rMap.putAll(dataMap)
        }
    }

    /**
     * 注册 Map 监听器
     *
     * key 监听器需开启 `notify-keyspace-events` 等 redis 相关配置
     *
     * @param key      缓存的键值
     * @param listener 监听器配置
     */
    @JvmStatic
    fun <T> addMapListener(key: String, listener: ObjectListener) {
        val rMap: RMap<String, T> = CLIENT.getMap(key)
        rMap.addListener(listener)
    }

    /**
     * 获得缓存的 Map
     *
     * @param key 缓存的键值
     * @return map 对象
     */
    @JvmStatic
    fun <T> getCacheMap(key: String): Map<String, T> {
        val rMap: RMap<String, T> = CLIENT.getMap(key)
        return rMap.readAllMap()
    }

    /**
     * 获得缓存 Map 的 key 列表
     *
     * @param key 缓存的键值
     * @return key 列表
     */
    @JvmStatic
    fun <T> getCacheMapKeySet(key: String): Set<String> {
        val rMap: RMap<String, T> = CLIENT.getMap(key)
        return rMap.readAllKeySet()
    }

    /**
     * 往 Hash 中存入数据
     *
     * @param key   Redis 键
     * @param hKey  Hash 键
     * @param value 值
     */
    @JvmStatic
    fun <T> setCacheMapValue(key: String, hKey: String, value: T) {
        val rMap: RMap<String, T> = CLIENT.getMap(key)
        rMap[hKey] = value
    }

    /**
     * 获取 Hash 中的数据
     *
     * @param key  Redis 键
     * @param hKey Hash 键
     * @return Hash 中的对象
     */
    @JvmStatic
    fun <T> getCacheMapValue(key: String, hKey: String): T? {
        val rMap: RMap<String, T> = CLIENT.getMap(key)
        return rMap[hKey]
    }

    /**
     * 删除 Hash 中的数据
     *
     * @param key  Redis 键
     * @param hKey Hash 键
     * @return Hash 中的对象
     */
    @JvmStatic
    fun <T> delCacheMapValue(key: String, hKey: String): T? {
        val rMap: RMap<String, T> = CLIENT.getMap(key)
        return rMap.remove(hKey)
    }

    /**
     * 删除 Hash 中的数据
     *
     * @param key   Redis 键
     * @param hKeys Hash 键
     */
    @JvmStatic
    fun <T> delMultiCacheMapValue(key: String, hKeys: Set<String>) {
        val batch = CLIENT.createBatch()
        val rMap: RMapAsync<String, T> = batch.getMap(key)
        for (hKey in hKeys) {
            rMap.removeAsync(hKey)
        }
        batch.execute()
    }

    /**
     * 获取多个 Hash 中的数据
     *
     * @param key   Redis 键
     * @param hKeys Hash 键集合
     * @return Hash 对象集合
     */
    @JvmStatic
    fun <K, V> getMultiCacheMapValue(key: String, hKeys: Set<K>): Map<K, V> {
        val rMap: RMap<K, V> = CLIENT.getMap(key)
        return rMap.getAll(hKeys)
    }

    /**
     * 设置原子值
     *
     * @param key   Redis 键
     * @param value 值
     */
    @JvmStatic
    fun setAtomicValue(key: String, value: Long) {
        val atomic = CLIENT.getAtomicLong(key)
        atomic.set(value)
    }

    /**
     * 获取原子值
     *
     * @param key Redis 键
     * @return 当前值
     */
    @JvmStatic
    fun getAtomicValue(key: String): Long {
        val atomic = CLIENT.getAtomicLong(key)
        return atomic.get()
    }

    /**
     * 递增原子值
     *
     * @param key Redis 键
     * @return 当前值
     */
    @JvmStatic
    fun incrAtomicValue(key: String): Long {
        val atomic = CLIENT.getAtomicLong(key)
        return atomic.incrementAndGet()
    }

    /**
     * 递减原子值
     *
     * @param key Redis 键
     * @return 当前值
     */
    @JvmStatic
    fun decrAtomicValue(key: String): Long {
        val atomic = CLIENT.getAtomicLong(key)
        return atomic.decrementAndGet()
    }

    /**
     * 获得缓存的基本对象列表(全局匹配忽略租户,自行拼接租户 id)
     *
     * @param pattern 字符串前缀
     * @return 对象列表
     */
    @JvmStatic
    fun keys(pattern: String): Collection<String> {
        val keysStream: Stream<String> = CLIENT.keys.getKeysStreamByPattern(pattern)
        return keysStream.collect(Collectors.toList())
    }

    /**
     * 删除缓存的基本对象列表(全局匹配忽略租户,自行拼接租户 id)
     *
     * @param pattern 字符串前缀
     */
    @JvmStatic
    fun deleteKeys(pattern: String) {
        CLIENT.keys.deleteByPattern(pattern)
    }

    /**
     * 检查 Redis 中是否存在 key
     *
     * @param key 键
     */
    @JvmStatic
    fun hasKey(key: String): Boolean {
        val rKeys = CLIENT.keys
        return rKeys.countExists(key) > 0
    }
}
