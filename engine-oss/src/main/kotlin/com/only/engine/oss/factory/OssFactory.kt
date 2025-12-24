package com.only.engine.oss.factory

import com.only.engine.exception.KnownException
import com.only.engine.json.misc.JsonUtils
import com.only.engine.oss.config.properties.OssProperties
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
    @Volatile
    private var defaultProperties: OssProperties? = null

    private const val DEFAULT_LOCAL_KEY = "default"

    @JvmStatic
    fun registerDefaultProperties(props: OssProperties) {
        defaultProperties = props
    }

    /**
     * 获取默认配置的 OssClient
     */
    @JvmStatic
    fun instance(): OssClient {
        val configKey = RedisUtils.getCacheObject<String>(OssConstant.DEFAULT_CONFIG_KEY)
        if (configKey.isNullOrBlank()) {
            return fallbackInstance(DEFAULT_LOCAL_KEY)
        }
        return instance(configKey)
    }

    /**
     * 根据配置 key 获取 OssClient
     */
    @JvmStatic
    fun instance(configKey: String): OssClient {
        val json = CacheUtils.get<String>(OssConstant.OSS_CONFIG_CACHE, configKey)
        if (json.isNullOrBlank()) {
            return fallbackInstance(configKey)
        }
        val properties = JsonUtils.parseObject(json, OssProperties::class.java)
            ?: return fallbackInstance(configKey)
        return instanceFromProperties(configKey, properties)
    }

    private fun fallbackInstance(configKey: String): OssClient {
        val properties = defaultProperties
            ?: throw KnownException("文件存储服务类型无法找到!")
        if (configKey != DEFAULT_LOCAL_KEY) {
            log.info("OSS 配置 '{}' 不存在，使用本地 OssProperties 配置", configKey)
        }
        return instanceFromProperties(configKey, properties)
    }

    private fun instanceFromProperties(configKey: String, properties: OssProperties): OssClient {
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
