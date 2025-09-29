package com.only.engine.security.cache.impl

import com.only.engine.security.cache.CacheService
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

@Component
@ConditionalOnMissingBean(name = ["redisCacheService"])
class LocalCacheService : CacheService {

    companion object {
        private val log = LoggerFactory.getLogger(LocalCacheService::class.java)
        private const val CLEANUP_INTERVAL_MINUTES = 5L
    }

    private val cache = ConcurrentHashMap<String, CacheItem>()
    private val cleanupExecutor: ScheduledExecutorService = Executors.newScheduledThreadPool(1) { r ->
        Thread(r, "local-cache-cleanup").apply { isDaemon = true }
    }

    data class CacheItem(
        val value: Any?,
        val expireTime: Long = -1L, // -1表示永不过期
    ) {
        fun isExpired(): Boolean {
            return expireTime > 0 && System.currentTimeMillis() > expireTime
        }

        fun getExpireDuration(): Duration? {
            return if (expireTime == -1L) null
            else if (expireTime <= System.currentTimeMillis()) Duration.ZERO
            else Duration.ofMillis(expireTime - System.currentTimeMillis())
        }
    }

    init {
        log.info("LocalCacheService initialized")
        // 定期清理过期缓存
        cleanupExecutor.scheduleWithFixedDelay(
            ::cleanExpiredItems,
            CLEANUP_INTERVAL_MINUTES,
            CLEANUP_INTERVAL_MINUTES,
            TimeUnit.MINUTES
        )
    }

    override fun get(key: String): Any? {
        val item = cache[key]
        return if (item?.isExpired() == true) {
            cache.remove(key)
            null
        } else {
            item?.value
        }
    }

    override fun set(key: String, value: Any?, timeout: Duration?) {
        val expireTime = if (timeout == null) -1L
        else System.currentTimeMillis() + timeout.toMillis()
        cache[key] = CacheItem(value, expireTime)
    }

    override fun delete(key: String): Boolean {
        return cache.remove(key) != null
    }

    override fun exists(key: String): Boolean {
        val item = cache[key]
        return if (item?.isExpired() == true) {
            cache.remove(key)
            false
        } else {
            item != null
        }
    }

    override fun expire(key: String, timeout: Duration): Boolean {
        val item = cache[key]
        return if (item != null && !item.isExpired()) {
            val expireTime = System.currentTimeMillis() + timeout.toMillis()
            cache[key] = item.copy(expireTime = expireTime)
            true
        } else {
            false
        }
    }

    override fun getExpire(key: String): Duration? {
        val item = cache[key]
        return if (item?.isExpired() == true) {
            cache.remove(key)
            null
        } else {
            item?.getExpireDuration()
        }
    }

    override fun keys(pattern: String): Set<String> {
        val regex = pattern.replace("*", ".*").toRegex()
        return cache.keys.filter { key ->
            val item = cache[key]
            if (item?.isExpired() == true) {
                cache.remove(key)
                false
            } else {
                key.matches(regex)
            }
        }.toSet()
    }

    override fun clear() {
        cache.clear()
    }

    override fun size(): Long {
        cleanExpiredItems()
        return cache.size.toLong()
    }

    override fun getAndSet(key: String, value: Any?): Any? {
        val oldItem = cache[key]
        cache[key] = CacheItem(value, -1L)
        return if (oldItem?.isExpired() == true) {
            null
        } else {
            oldItem?.value
        }
    }

    override fun setIfAbsent(key: String, value: Any?, timeout: Duration?): Boolean {
        val existingItem = cache[key]
        return if (existingItem?.isExpired() == true) {
            cache.remove(key)
            set(key, value, timeout)
            true
        } else if (existingItem == null) {
            set(key, value, timeout)
            true
        } else {
            false
        }
    }

    private fun cleanExpiredItems() {
        val currentTime = System.currentTimeMillis()
        var cleanedCount = 0

        val iterator = cache.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.value.isExpired()) {
                iterator.remove()
                cleanedCount++
            }
        }

        if (cleanedCount > 0) {
            log.debug("Cleaned {} expired cache items", cleanedCount)
        }
    }

    fun destroy() {
        log.info("Shutting down LocalCacheService")
        cleanupExecutor.shutdown()
        try {
            if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupExecutor.shutdownNow()
            }
        } catch (e: InterruptedException) {
            cleanupExecutor.shutdownNow()
            Thread.currentThread().interrupt()
        }
        cache.clear()
    }
}
