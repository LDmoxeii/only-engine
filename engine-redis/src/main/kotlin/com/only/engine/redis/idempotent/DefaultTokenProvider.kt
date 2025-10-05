package com.only.engine.redis.idempotent

import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

/**
 * 默认 Token 提供者
 *
 * 从请求头中获取 Token，如果不存在则使用请求 URI
 *
 * @author LD_moxeii
 */
class DefaultTokenProvider : TokenProvider {

    override fun getToken(): String {
        val attributes = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
            ?: return ""
        val request = attributes.request
        return (request.getHeader(getTokenName()) ?: request.requestURI).trim()
    }

    override fun getTokenName(): String = "Authorization"
}
