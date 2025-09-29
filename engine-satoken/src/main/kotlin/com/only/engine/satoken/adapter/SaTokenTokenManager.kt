package com.only.engine.satoken.adapter

import cn.dev33.satoken.stp.StpUtil
import com.only.engine.security.TokenManager
import com.only.engine.security.model.TokenInfo
import org.slf4j.LoggerFactory

class SaTokenTokenManager : TokenManager {

    companion object {
        private val log = LoggerFactory.getLogger(SaTokenTokenManager::class.java)
    }

    override fun createToken(loginId: Any, deviceType: String?): String {
        StpUtil.login(loginId, deviceType)
        if (deviceType != null) {
            try {
                StpUtil.getSession().set("device", deviceType)
            } catch (e: Exception) {
                log.warn("Failed to set device type", e)
            }
        }
        return StpUtil.getTokenValue()
    }

    override fun parseToken(token: String): TokenInfo? {
        return try {
            val oldToken = StpUtil.getTokenValue()
            StpUtil.setTokenValue(token)

            val loginId = StpUtil.getLoginIdDefaultNull() ?: return null
            val device = try {
                StpUtil.getSession().get("device")?.toString()
            } catch (e: Exception) {
                null
            }
            val timeout = StpUtil.getTokenTimeout()

            StpUtil.setTokenValue(oldToken)

            TokenInfo(
                loginId = loginId,
                deviceType = device,
                tokenValue = token,
                timeout = timeout
            )
        } catch (e: Exception) {
            log.warn("Failed to parse token: {}", token, e)
            null
        }
    }

    override fun validateToken(token: String): Boolean {
        return try {
            val oldToken = StpUtil.getTokenValue()
            StpUtil.setTokenValue(token)
            val isValid = StpUtil.isLogin()
            StpUtil.setTokenValue(oldToken)
            isValid
        } catch (e: Exception) {
            false
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

    override fun removeToken(token: String) {
        try {
            val oldToken = StpUtil.getTokenValue()
            StpUtil.setTokenValue(token)
            val loginId = StpUtil.getLoginIdDefaultNull()
            if (loginId != null) {
                StpUtil.logout(loginId)
            }
            StpUtil.setTokenValue(oldToken)
        } catch (e: Exception) {
            log.warn("Failed to remove token: {}", token, e)
        }
    }

    override fun getTokenTimeout(token: String): Long {
        return try {
            val oldToken = StpUtil.getTokenValue()
            StpUtil.setTokenValue(token)
            val timeout = StpUtil.getTokenTimeout()
            StpUtil.setTokenValue(oldToken)
            timeout
        } catch (e: Exception) {
            0L
        }
    }

    override fun getTokenValue(): String? {
        return try {
            StpUtil.getTokenValue()
        } catch (e: Exception) {
            null
        }
    }

    override fun setTokenValue(token: String) {
        StpUtil.setTokenValue(token)
    }

    override fun clearTokenValue() {
        StpUtil.setTokenValue(null)
    }
}