package com.only.engine.sms.core.dao

import com.only.engine.constants.GlobalConstants
import com.only.engine.redis.misc.RedisUtils
import org.dromara.sms4j.api.dao.SmsDao
import java.time.Duration

/**
 * SmsDao 缓存实现，基于 RedisUtils
 *
 * 主要用于短信发送过程中的重试与拦截等场景
 */
class OnlySmsDao : SmsDao {

    /**
     * 存储带过期时间的缓存数据
     *
     * @param key       键
     * @param value     值
     * @param cacheTime 缓存时间（单位：秒）
     */
    override fun set(key: String, value: Any?, cacheTime: Long) {
        val realKey = GlobalConstants.GLOBAL_REDIS_KEY + key
        RedisUtils.setCacheObject(realKey, value, Duration.ofSeconds(cacheTime))
    }

    /**
     * 存储缓存数据，保留当前 TTL
     *
     * @param key   键
     * @param value 值
     */
    override fun set(key: String, value: Any?) {
        val realKey = GlobalConstants.GLOBAL_REDIS_KEY + key
        RedisUtils.setCacheObject(realKey, value, true)
    }

    /**
     * 读取缓存数据
     *
     * @param key 键
     * @return 值
     */
    override fun get(key: String): Any? {
        val realKey = GlobalConstants.GLOBAL_REDIS_KEY + key
        return RedisUtils.getCacheObject<Any>(realKey)
    }

    /**
     * 根据 key 移除缓存
     *
     * @param key 缓存键
     * @return 被删除的值
     */
    override fun remove(key: String): Any {
        val realKey = GlobalConstants.GLOBAL_REDIS_KEY + key
        return RedisUtils.deleteObject(realKey)
    }

    /**
     * 清空短信相关缓存
     */
    override fun clean() {
        RedisUtils.deleteKeys(GlobalConstants.GLOBAL_REDIS_KEY + "sms:*")
    }
}

