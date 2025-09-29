package com.only.engine.security.cache.impl

import com.only.engine.security.cache.CacheService
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.TimeUnit

@Component
@Primary
@ConditionalOnClass(RedisTemplate::class)
@ConditionalOnProperty(
    prefix = "only.security.cache",
    name = ["type"],
    havingValue = "redis"
)
class RedisCacheService(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val fallbackCacheService: LocalCacheService,
) : CacheService {

    companion object {
        private val log = LoggerFactory.getLogger(RedisCacheService::class.java)
    }

    init {
        log.info("RedisCacheService initialized with fallback to LocalCacheService")
    }

    override fun get(key: String): Any? {
        return try {
            redisTemplate.opsForValue().get(key)
        } catch (e: Exception) {
            log.warn("Redis get failed, using fallback cache, key: {}", key, e)
            fallbackCacheService.get(key)
        }
    }

    override fun set(key: String, value: Any?, timeout: Duration?) {
        try {
            if (timeout == null) {
                redisTemplate.opsForValue().set(key, value)
            } else {
                redisTemplate.opsForValue().set(key, value, timeout)
            }
            // 同步到本地缓存作为备份
            fallbackCacheService.set(key, value, timeout)
        } catch (e: Exception) {
            log.warn("Redis set failed, using fallback cache, key: {}", key, e)
            fallbackCacheService.set(key, value, timeout)
        }
    }

    override fun delete(key: String): Boolean {
        return try {
            val redisResult = redisTemplate.delete(key)
            fallbackCacheService.delete(key)
            redisResult
        } catch (e: Exception) {
            log.warn("Redis delete failed, using fallback cache, key: {}", key, e)
            fallbackCacheService.delete(key)
        }
    }

    override fun exists(key: String): Boolean {
        return try {
            redisTemplate.hasKey(key) ?: false
        } catch (e: Exception) {
            log.warn("Redis exists check failed, using fallback cache, key: {}", key, e)
            fallbackCacheService.exists(key)
        }
    }

    override fun expire(key: String, timeout: Duration): Boolean {
        return try {
            val redisResult = redisTemplate.expire(key, timeout)
            fallbackCacheService.expire(key, timeout)
            redisResult
        } catch (e: Exception) {
            log.warn("Redis expire failed, using fallback cache, key: {}", key, e)
            fallbackCacheService.expire(key, timeout)
        }
    }

    override fun getExpire(key: String): Duration? {
        return try {
            val ttl = redisTemplate.getExpire(key, TimeUnit.MILLISECONDS)
            if (ttl == -1L) null // 永不过期
            else if (ttl <= 0) Duration.ZERO
            else Duration.ofMillis(ttl)
        } catch (e: Exception) {
            log.warn("Redis getExpire failed, using fallback cache, key: {}", key, e)
            fallbackCacheService.getExpire(key)
        }
    }

    override fun keys(pattern: String): Set<String> {
        return try {
            redisTemplate.keys(pattern) ?: emptySet()
        } catch (e: Exception) {
            log.warn("Redis keys search failed, using fallback cache, pattern: {}", pattern, e)
            fallbackCacheService.keys(pattern)
        }
    }

    override fun clear() {
        try {
            // 这里可以实现Redis的清理逻辑，但要谨慎使用
            log.warn("Redis clear operation is dangerous and not implemented. Use delete with specific keys instead.")
        } catch (e: Exception) {
            log.warn("Redis clear failed", e)
        }
        fallbackCacheService.clear()
    }

    override fun size(): Long {
        return try {
            redisTemplate.connectionFactory?.connection?.use { connection ->
                connection.dbSize()
            } ?: fallbackCacheService.size()
        } catch (e: Exception) {
            log.warn("Redis size check failed, using fallback cache", e)
            fallbackCacheService.size()
        }
    }

    override fun getAndSet(key: String, value: Any?): Any? {
        return try {
            val result = redisTemplate.opsForValue().getAndSet(key, value)
            fallbackCacheService.getAndSet(key, value)
            result
        } catch (e: Exception) {
            log.warn("Redis getAndSet failed, using fallback cache, key: {}", key, e)
            fallbackCacheService.getAndSet(key, value)
        }
    }

    override fun setIfAbsent(key: String, value: Any?, timeout: Duration?): Boolean {
        return try {
            val result = if (timeout == null) {
                redisTemplate.opsForValue().setIfAbsent(key, value) ?: false
            } else {
                redisTemplate.opsForValue().setIfAbsent(key, value, timeout) ?: false
            }
            if (result) {
                fallbackCacheService.setIfAbsent(key, value, timeout)
            }
            result
        } catch (e: Exception) {
            log.warn("Redis setIfAbsent failed, using fallback cache, key: {}", key, e)
            fallbackCacheService.setIfAbsent(key, value, timeout)
        }
    }
}
