package com.only.engine.oss.factory

import com.only.engine.exception.KnownException
import com.only.engine.json.misc.JsonUtils
import com.only.engine.oss.config.OssProperties
import com.only.engine.oss.constant.OssConstant
import com.only.engine.oss.core.OssClient
import com.only.engine.redis.misc.CacheUtils
import com.only.engine.redis.misc.RedisUtils
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * OSS 客户端工厂
 */
object OssFactory {

    private val log = LoggerFactory.getLogger(OssFactory::class.java)
    private val clientCache = ConcurrentHashMap<String, OssClient>()
    private val lock = ReentrantLock()

    /**
     * 获取默认配置的 OssClient
     */
    @JvmStatic
    fun instance(): OssClient {
        val configKey = RedisUtils.getCacheObject<String>(OssConstant.DEFAULT_CONFIG_KEY)
            ?: throw KnownException("文件存储服务类型无法找到!")
        return instance(configKey)
    }

    /**
     * 根据配置 key 获取 OssClient
     */
    @JvmStatic
    fun instance(configKey: String): OssClient {
        val json = CacheUtils.get<String>(OssConstant.OSS_CONFIG_CACHE, configKey)
            ?: throw KnownException("系统异常, '$configKey' 配置信息不存在!")
        val properties = JsonUtils.parseObject(json, OssProperties::class.java)
            ?: throw KnownException("OSS 配置信息解析失败")

        val cacheKey = buildCacheKey(configKey, properties)
        var client = clientCache[cacheKey]
        if (client == null || !client.checkPropertiesSame(properties)) {
            lock.withLock {
                client = clientCache[cacheKey]
                if (client == null || !client.checkPropertiesSame(properties)) {
                    val newClient = OssClient(configKey, properties)
                    clientCache[cacheKey] = newClient
                    log.info("创建 OSS 实例 key => {}", cacheKey)
                    client = newClient
                }
            }
        }
        return client!!
    }

    private fun buildCacheKey(configKey: String, properties: OssProperties): String {
        val tenantId = properties.tenantId
        return if (!tenantId.isNullOrBlank()) "$tenantId:$configKey" else configKey
    }
}
