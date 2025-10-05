package com.only.engine.redis.manager

import cn.hutool.extra.spring.SpringUtil
import org.springframework.cache.Cache
import java.util.concurrent.Callable

/**
 * Cache 装饰器模式(用于扩展 Caffeine 一级缓存)
 *
 * @author LionLi
 */
class CaffeineCacheDecorator(
    private val name: String,
    private val cache: Cache,
) : Cache {

    companion object {
        private val CAFFEINE: com.github.benmanes.caffeine.cache.Cache<Any, Any> by lazy {
            SpringUtil.getBean("caffeine")
        }
    }

    override fun getName(): String = name

    override fun getNativeCache(): Any = cache.nativeCache

    private fun getUniqueKey(key: Any): String = "$name:$key"

    override fun get(key: Any): Cache.ValueWrapper? {
        val o = CAFFEINE.get(getUniqueKey(key)) { cache.get(key) }
        return o as? Cache.ValueWrapper
    }

    override fun <T> get(key: Any, type: Class<T>?): T? {
        val o = CAFFEINE.get(getUniqueKey(key)) { cache.get(key, type) }
        @Suppress("UNCHECKED_CAST")
        return o as? T
    }

    override fun <T> get(key: Any, valueLoader: Callable<T>): T? {
        val o = CAFFEINE.get(getUniqueKey(key)) { cache.get(key, valueLoader) }
        @Suppress("UNCHECKED_CAST")
        return o as? T
    }

    override fun put(key: Any, value: Any?) {
        CAFFEINE.invalidate(getUniqueKey(key))
        cache.put(key, value)
    }

    override fun putIfAbsent(key: Any, value: Any?): Cache.ValueWrapper? {
        CAFFEINE.invalidate(getUniqueKey(key))
        return cache.putIfAbsent(key, value)
    }

    override fun evict(key: Any) {
        evictIfPresent(key)
    }

    override fun evictIfPresent(key: Any): Boolean {
        val result = cache.evictIfPresent(key)
        if (result) {
            CAFFEINE.invalidate(getUniqueKey(key))
        }
        return result
    }

    override fun clear() = cache.clear()

    override fun invalidate(): Boolean = cache.invalidate()
}
