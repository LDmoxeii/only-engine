package com.only.engine.satoken.adapter

import cn.dev33.satoken.exception.NotLoginException
import cn.dev33.satoken.stp.StpUtil
import com.only.engine.security.AuthenticationService
import com.only.engine.security.model.LoginRequest
import com.only.engine.security.model.LoginResult
import com.only.engine.security.model.UserInfo
import org.slf4j.LoggerFactory

class SaTokenAuthenticationService(
    private val userDetailsProvider: SaTokenUserDetailsProvider,
) : AuthenticationService {

    companion object {
        private val log = LoggerFactory.getLogger(SaTokenAuthenticationService::class.java)
    }

    override fun login(request: LoginRequest): LoginResult {
        val userInfo = userDetailsProvider.loadUserByCredentials(request.username, request.password)
            ?: throw com.only.engine.exception.ErrorException(401, "用户名或密码错误")

        StpUtil.login(userInfo.id, request.deviceType)
        val token = StpUtil.getTokenValue()
        val timeout = StpUtil.getTokenTimeout()

        log.debug("User {} logged in with token: {}", userInfo.username, token)

        return LoginResult(
            token = token,
            tokenTimeout = timeout,
            userInfo = userInfo,
            extra = request.extra
        )
    }

    override fun logout(token: String) {
        try {
            val oldToken = StpUtil.getTokenValue()
            StpUtil.setTokenValue(token)
            val loginId = StpUtil.getLoginIdDefaultNull()
            if (loginId != null) {
                StpUtil.logout(loginId)
                log.debug("User {} logged out", loginId)
            }
            StpUtil.setTokenValue(oldToken)
        } catch (e: Exception) {
            log.warn("Logout failed for token: {}", token, e)
        }
    }

    override fun checkLogin(token: String): Boolean {
        return try {
            val oldToken = StpUtil.getTokenValue()
            StpUtil.setTokenValue(token)
            val result = StpUtil.isLogin()
            StpUtil.setTokenValue(oldToken)
            result
        } catch (e: Exception) {
            false
        }
    }

    override fun getCurrentUser(): UserInfo? {
        return try {
            val loginId = StpUtil.getLoginIdDefaultNull() ?: return null
            userDetailsProvider.loadUserById(loginId)
        } catch (e: Exception) {
            log.warn("Failed to get current user", e)
            null
        }
    }

    override fun getTokenInfo(token: String): Map<String, Any?> {
        return try {
            val oldToken = StpUtil.getTokenValue()
            StpUtil.setTokenValue(token)
            val tokenInfo = mapOf(
                "loginId" to StpUtil.getLoginIdDefaultNull(),
                "tokenValue" to StpUtil.getTokenValue(),
                "timeout" to StpUtil.getTokenTimeout(),
                "activeTimeout" to 0L,
                "device" to try {
                    StpUtil.getExtra("device")
                } catch (e: NotLoginException) {
                    null
                }
            )
            StpUtil.setTokenValue(oldToken)
            tokenInfo
        } catch (e: Exception) {
            emptyMap()
        }
    }

    override fun refreshToken(token: String): String {
        val oldToken = StpUtil.getTokenValue()
        return try {
            StpUtil.setTokenValue(token)
            StpUtil.renewTimeout(StpUtil.getTokenTimeout())
            StpUtil.getTokenValue()
        } finally {
            StpUtil.setTokenValue(oldToken)
        }
    }

    override fun isLogin(): Boolean {
        return StpUtil.isLogin()
    }

    override fun getLoginId(): Any? {
        return StpUtil.getLoginIdDefaultNull()
    }
}
