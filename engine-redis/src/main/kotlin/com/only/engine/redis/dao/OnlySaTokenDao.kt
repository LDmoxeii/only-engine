package com.only.engine.redis.dao

import cn.dev33.satoken.dao.SaTokenDao.NEVER_EXPIRE
import cn.dev33.satoken.dao.SaTokenDao.NOT_VALUE_EXPIRE
import cn.dev33.satoken.dao.auto.SaTokenDaoBySessionFollowObject
import cn.dev33.satoken.util.SaFoxUtil
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.only.engine.redis.misc.RedisUtils
import java.time.Duration
import java.util.ArrayList
import java.util.concurrent.TimeUnit

/**
 * Sa-Token 持久层实现：Caffeine + Redis 多级缓存
 */
class OnlySaTokenDao : SaTokenDaoBySessionFollowObject {

    companion object {
        private val CACHE: Cache<String, Any?> = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .initialCapacity(100)
            .maximumSize(1000)
            .build()
    }

    override fun get(key: String): String? {
        val value = CACHE.get(key) { RedisUtils.getCacheObject<String>(it) }
        return value as? String
    }

    override fun set(key: String, value: String, timeout: Long) {
        if (timeout == 0L || timeout <= NOT_VALUE_EXPIRE) {
            return
        }
        if (timeout == NEVER_EXPIRE) {
            RedisUtils.setCacheObject(key, value)
        } else {
            RedisUtils.setCacheObject(key, value, Duration.ofSeconds(timeout))
        }
        CACHE.invalidate(key)
    }

    override fun update(key: String, value: String) {
        if (RedisUtils.hasKey(key)) {
            RedisUtils.setCacheObject(key, value, true)
            CACHE.invalidate(key)
        }
    }

    override fun delete(key: String) {
        if (RedisUtils.deleteObject(key)) {
            CACHE.invalidate(key)
        }
    }

    override fun getTimeout(key: String): Long {
        val timeout = RedisUtils.getTimeToLive<Any>(key)
        return if (timeout < 0) timeout else timeout / 1000 + 1
    }

    override fun updateTimeout(key: String, timeout: Long) {
        RedisUtils.expire(key, Duration.ofSeconds(timeout))
    }

    override fun getObject(key: String): Any? {
        return CACHE.get(key) { RedisUtils.getCacheObject<Any>(it) }
    }

    override fun <T> getObject(key: String, classType: Class<T>): T? {
        @Suppress("UNCHECKED_CAST")
        val obj = CACHE.get(key) { RedisUtils.getCacheObject<Any>(it) }
        return obj as T?
    }

    override fun setObject(key: String, value: Any, timeout: Long) {
        if (timeout == 0L || timeout <= NOT_VALUE_EXPIRE) {
            return
        }
        if (timeout == NEVER_EXPIRE) {
            RedisUtils.setCacheObject(key, value)
        } else {
            RedisUtils.setCacheObject(key, value, Duration.ofSeconds(timeout))
        }
        CACHE.invalidate(key)
    }

    override fun updateObject(key: String, value: Any) {
        if (RedisUtils.hasKey(key)) {
            RedisUtils.setCacheObject(key, value, true)
            CACHE.invalidate(key)
        }
    }

    override fun deleteObject(key: String) {
        if (RedisUtils.deleteObject(key)) {
            CACHE.invalidate(key)
        }
    }

    override fun getObjectTimeout(key: String): Long {
        val timeout = RedisUtils.getTimeToLive<Any>(key)
        return if (timeout < 0) timeout else timeout / 1000 + 1
    }

    override fun updateObjectTimeout(key: String, timeout: Long) {
        RedisUtils.expire(key, Duration.ofSeconds(timeout))
    }

    override fun searchData(prefix: String, keyword: String, start: Int, size: Int, sortType: Boolean): List<String> {
        val pattern = "$prefix*$keyword*"
        @Suppress("UNCHECKED_CAST")
        return CACHE.get(pattern) {
            val keys = RedisUtils.keys(pattern)
            val list = ArrayList(keys)
            SaFoxUtil.searchList(list, start, size, sortType)
        } as? List<String> ?: emptyList()
    }
}
