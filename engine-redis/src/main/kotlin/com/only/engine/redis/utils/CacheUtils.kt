package com.only.engine.redis.utils

import cn.hutool.extra.spring.SpringUtil
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager

/**
 * 缓存操作工具类
 *
 * @author Michelle.Chung
 */
@Suppress("UNCHECKED_CAST")
object CacheUtils {

    private val CACHE_MANAGER: CacheManager by lazy {
        SpringUtil.getBean(CacheManager::class.java)
    }

    /**
     * 获取缓存值
     *
     * @param cacheNames 缓存组名称
     * @param key        缓存 key
     */
    @JvmStatic
    fun <T> get(cacheNames: String, key: Any): T? {
        val wrapper: Cache.ValueWrapper? = CACHE_MANAGER.getCache(cacheNames)?.get(key)
        return wrapper?.get() as? T
    }

    /**
     * 保存缓存值
     *
     * @param cacheNames 缓存组名称
     * @param key        缓存 key
     * @param value      缓存值
     */
    @JvmStatic
    fun put(cacheNames: String, key: Any, value: Any?) {
        CACHE_MANAGER.getCache(cacheNames)?.put(key, value)
    }

    /**
     * 删除缓存值
     *
     * @param cacheNames 缓存组名称
     * @param key        缓存 key
     */
    @JvmStatic
    fun evict(cacheNames: String, key: Any) {
        CACHE_MANAGER.getCache(cacheNames)?.evict(key)
    }

    /**
     * 清空缓存值
     *
     * @param cacheNames 缓存组名称
     */
    @JvmStatic
    fun clear(cacheNames: String) {
        CACHE_MANAGER.getCache(cacheNames)?.clear()
    }
}
