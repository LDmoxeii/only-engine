package com.only.engine.redis.config

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.only.engine.redis.RedisInitPrinter
import com.only.engine.redis.manager.OnlySpringCacheManager
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import java.util.concurrent.TimeUnit

/**
 * 缓存自动配置
 *
 * 集成了以下功能:
 * - 启用 Spring Cache 注解支持
 * - 配置 Caffeine 本地缓存
 * - 配置自定义缓存管理器(Redis + Caffeine 两级缓存)
 *
 * @author LD_moxeii
 */
@AutoConfiguration
@EnableCaching
@ConditionalOnProperty(prefix = "only.engine.redis", name = ["enable"], havingValue = "true")
class CacheAutoConfiguration : RedisInitPrinter {

    companion object {
        private val log = LoggerFactory.getLogger(CacheAutoConfiguration::class.java)
    }

    /**
     * Caffeine 本地缓存处理器
     */
    @Bean
    fun caffeine(): Cache<Any, Any> {
        printInit(Cache::class.java, log)
        return Caffeine.newBuilder()
            // 设置最后一次写入或访问后经过固定时间过期
            .expireAfterWrite(30, TimeUnit.SECONDS)
            // 初始的缓存空间大小
            .initialCapacity(100)
            // 缓存的最大条数
            .maximumSize(1000)
            .build()
    }

    /**
     * 自定义缓存管理器,整合 Spring Cache
     */
    @Bean
    fun cacheManager(): CacheManager {
        printInit(OnlySpringCacheManager::class.java, log)
        return OnlySpringCacheManager()
    }
}
