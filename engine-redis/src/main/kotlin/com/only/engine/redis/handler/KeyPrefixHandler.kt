package com.only.engine.redis.handler

import org.redisson.api.NameMapper

/**
 * Redis 缓存 key 前缀处理器
 *
 * @author ye
 * @since 4.3.0
 */
class KeyPrefixHandler(keyPrefix: String?) : NameMapper {

    // 前缀为空则返回空前缀
    private val keyPrefix: String = if (keyPrefix.isNullOrBlank()) "" else "$keyPrefix:"

    /**
     * 增加前缀
     */
    override fun map(name: String?): String? {
        if (name.isNullOrBlank()) {
            return null
        }
        if (keyPrefix.isNotBlank() && !name.startsWith(keyPrefix)) {
            return keyPrefix + name
        }
        return name
    }

    /**
     * 去除前缀
     */
    override fun unmap(name: String?): String? {
        if (name.isNullOrBlank()) {
            return null
        }
        if (keyPrefix.isNotBlank() && name.startsWith(keyPrefix)) {
            return name.substring(keyPrefix.length)
        }
        return name
    }
}
