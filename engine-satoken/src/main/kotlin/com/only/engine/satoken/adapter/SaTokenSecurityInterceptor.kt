package com.only.engine.satoken.adapter

import cn.dev33.satoken.exception.NotLoginException
import cn.dev33.satoken.stp.StpUtil
import com.only.engine.security.SecurityInterceptor
import com.only.engine.security.url.UrlCollector
import com.only.engine.security.url.UrlMatcher
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory

class SaTokenSecurityInterceptor(
    private val userDetailsProvider: SaTokenUserDetailsProvider,
    private val securityContextHolder: SaTokenSecurityContextHolder,
    private val urlCollector: UrlCollector,
) : SecurityInterceptor {

    companion object {
        private val log = LoggerFactory.getLogger(SaTokenSecurityInterceptor::class.java)
    }

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val requestUri = request.requestURI

        // 检查当前请求是否需要安全检查
        if (!needsSecurityCheck(requestUri)) {
            return true
        }

        return try {
            // 检查登录状态
            StpUtil.checkLogin()

            // 设置安全上下文
            val loginId = StpUtil.getLoginId()
            val userInfo = userDetailsProvider.loadUserById(loginId)
            val context = com.only.engine.security.model.SecurityContext(
                userInfo = userInfo,
                token = StpUtil.getTokenValue(),
                tokenInfo = mapOf(
                    "loginId" to loginId,
                    "timeout" to StpUtil.getTokenTimeout(),
                    "device" to try {
                        StpUtil.getSession().get("device")
                    } catch (e: NotLoginException) {
                        null
                    }
                )
            )
            securityContextHolder.setContext(context)

            true
        } catch (e: NotLoginException) {
            log.debug("Authentication failed for request: {}", requestUri)
            throw com.only.engine.exception.ErrorException(401, "未登录或登录已过期")
        } catch (e: Exception) {
            log.error("Security check failed for request: {}", requestUri, e)
            throw com.only.engine.exception.ErrorException(500, "安全检查失败")
        }
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?,
    ) {
        // 清理安全上下文
        securityContextHolder.clearContext()
    }

    override fun getOrder(): Int = 100

    private fun needsSecurityCheck(requestUri: String): Boolean {
        // 检查URL是否在收集的URL列表中
        return UrlMatcher.matchesAny(urlCollector.urls, requestUri)
    }
}
