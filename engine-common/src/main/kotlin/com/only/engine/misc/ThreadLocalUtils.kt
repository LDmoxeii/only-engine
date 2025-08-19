package com.only.engine.misc

import com.alibaba.fastjson.JSONObject
import com.alibaba.ttl.TransmittableThreadLocal
import com.only.engine.constants.HeaderConstants
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.java

object ThreadLocalUtils {
    private val CURRENT_CONTEXT: ThreadLocal<MutableMap<String, String>> =
        TransmittableThreadLocal()

    fun setTokenInfo(tokenInfo: Any) = set(HeaderConstants.X_ONLY_TOKEN_INFO, serializer(tokenInfo))

    fun setTenantId(tenantId: String) = set(HeaderConstants.X_ONLY_TENANT_ID, tenantId)

    fun setJwt(jwt: String?) = set(HeaderConstants.X_ONLY_JWT_INFO, jwt)

    fun setLocale(locale: Locale) = set(HeaderConstants.X_ONLY_LOCALE_INFO, serializer(locale))

    fun setBizTrackCode(bizTrackCode: String) = set(HeaderConstants.X_ONLY_BIZ_TRACK_CODE, bizTrackCode)

    fun setUserInfo(userInfo: Any) = set(HeaderConstants.X_ONLY_USER_CONTEXT, serializer(userInfo))

    fun set(key: String, value: Any?) {
        getCurrentContext()[key] = serializer(value) as String
    }

    // TODO: 等待TOKEN信息定义
//    fun getTokenInfo(): YmTokenInfo =
//        get(HeaderConstants.X_ONLY_TOKEN_INFO, YmTokenInfo::class.java)

    // TODO: 等待用户信息定义
//    fun getUserInfo(): YmUserInfo =
//        get(HeaderConstants.X_ONLY_USER_CONTEXT, YmUserInfo::class.java)

    fun getTenantId(): String =
        get(HeaderConstants.X_ONLY_TENANT_ID)

    fun getJwt(): String =
        get(HeaderConstants.X_ONLY_JWT_INFO)

    fun getLocale(): Locale =
        get(HeaderConstants.X_ONLY_LOCALE_INFO, Locale::class.java)

    fun getBizTrackCode(): String =
        get(HeaderConstants.X_ONLY_BIZ_TRACK_CODE)

    fun <T> get(key: String, clazz: Class<T>): T =
        get(key).let { JSONObject.parseObject(it, clazz) }

    fun get(key: String): String =
        key.let { CURRENT_CONTEXT.get()[it] ?: key }

    fun getCurrentContext(): MutableMap<String, String> =
        CURRENT_CONTEXT.get() ?: ConcurrentHashMap<String, String>().also {
            CURRENT_CONTEXT.set(it)
        }

    fun remove(key: String): String =
        key.let { CURRENT_CONTEXT.get()?.remove(it) ?: key }

    fun <T> remove(key: String, clazz: Class<T>): T =
        remove(key).let { JSONObject.parseObject(it, clazz) }

    fun clear() = CURRENT_CONTEXT.remove()

    fun serializer(obj: Any?): String? =
        when (obj) {
            null -> null
            is String -> obj
            else -> JSONObject.toJSONString(obj)
        }

    fun getTraceId(): String =
        UUID.randomUUID().toString().replace("-", "")
}
