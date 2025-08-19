package com.only.engine.web.filter

import cn.hutool.json.JSONObject
import cn.hutool.jwt.JWTUtil
import com.only.engine.constants.HeaderConstants
import com.only.engine.misc.ThreadLocalUtils
import com.only.engine.web.WebInitPrinter
import com.only.engine.web.misc.WebMessageConverterUtils
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.http.HttpHeaders
import java.util.*
import kotlin.jvm.java

class ThreadLocalFilter : Filter, WebInitPrinter {

    companion object {
        private val log = LoggerFactory.getLogger(ThreadLocalFilter::class.java)
        const val BEAN_NAME = "threadLocalFilter"
        const val AUTHENTICATION_PREFIX = "Bearer "
        private const val SECRET_KEY = "h2MycACBnf4UfHWUqTvA0zwRWU4iDVyzq8488P3l3FNUt78L6Yo5kwnhRRAdT3wWriEsLH2I"
    }

    init {
        printInit(ThreadLocalFilter::class.java, log)
    }

    override fun doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, filterChain: FilterChain) {
        try {
            if (servletRequest is HttpServletRequest) {
                val httpRequest = servletRequest

                // 解析 tenantId
                val tenantId = httpRequest.getHeader(HeaderConstants.X_ONLY_TENANT_ID)
                ThreadLocalUtils.setTenantId(tenantId)

                // 配置国际化
                ThreadLocalUtils.setLocale(LocaleContextHolder.getLocale())

                // 配置单次请求追踪码
                val bizTrackCode = UUID.randomUUID().toString().replace("-", "")
                ThreadLocalUtils.setBizTrackCode(bizTrackCode)

                // 日志打印添加
                MDC.put("bid", bizTrackCode)

                // 获取 jwt from header
                val jwtToken = getTokenFromHeader(httpRequest)
                val payloads = verifyJwtAndGetPayload(jwtToken)

                // 存储 tokenInfo 使用 Jackson 序列化
                ThreadLocalUtils.setTokenInfo(WebMessageConverterUtils.toJsonString(payloads))

                // 兼容网关透传的用户信息
                getTokenFromUserInfo(httpRequest)

                // 存储 jwt
                ThreadLocalUtils.setJwt(jwtToken)
            }

            filterChain.doFilter(servletRequest, servletResponse)
        } finally {
            // 全局清空
            ThreadLocalUtils.clear()
            MDC.clear()
        }
    }

    private fun getTokenFromHeader(request: HttpServletRequest): String? {
        val header = request.getHeader(HttpHeaders.AUTHORIZATION)
        return if (!header.isNullOrBlank() && header.startsWith(AUTHENTICATION_PREFIX)) {
            header.removePrefix(AUTHENTICATION_PREFIX)
        } else null
    }

    private fun verifyJwtAndGetPayload(jwtToken: String?): JSONObject? {
        if (jwtToken.isNullOrBlank()) return null

        return try {
            val jwt = JWTUtil.parseToken(jwtToken)

            if (!jwt.setKey(SECRET_KEY.toByteArray()).verify()) {
                null
            }
            return jwt.payloads

        } catch (e: Exception) {
            log.warn("parse token info from jwt error: {}", e.message, e)
            null
        }
    }

    private fun getTokenFromUserInfo(request: HttpServletRequest) {
        TODO("等待 UserInfo 标准定义")
//        request.getHeader(HeaderConstants.X_ONLY_USER_CONTEXT)
//            ?.let { String(Base64.getDecoder().decode(it)) }
//            ?.let { WebMessageConverterUtils.OBJECT_MAPPER.readValue(it, UserInfo::class.java) }
//            ?.let { userInfo ->
//                ThreadLocalUtils.setUserInfo(userInfo)
//
//                val userTokenInfo = TokenInfo().apply {
//                    userId = userInfo.id
//                    tenantId = userInfo.tenantId
//                    nickname = userInfo.fullName
//                    t = userInfo.terminalType
//                }
//                ThreadLocalUtils.setTokenInfo(userTokenInfo)
//            }
    }
}

