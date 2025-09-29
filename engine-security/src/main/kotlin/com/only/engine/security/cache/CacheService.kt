package com.only.engine.security.cache

import java.time.Duration

interface CacheService {

    fun get(key: String): Any?

    fun set(key: String, value: Any?, timeout: Duration? = null)

    fun delete(key: String): Boolean

    fun exists(key: String): Boolean

    fun expire(key: String, timeout: Duration): Boolean

    fun getExpire(key: String): Duration?

    fun keys(pattern: String): Set<String>

    fun clear()

    fun size(): Long

    fun getAndSet(key: String, value: Any?): Any?

    fun setIfAbsent(key: String, value: Any?, timeout: Duration? = null): Boolean
}