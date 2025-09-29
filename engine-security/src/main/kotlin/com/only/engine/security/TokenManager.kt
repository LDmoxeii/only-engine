package com.only.engine.security

import com.only.engine.security.model.TokenInfo

interface TokenManager {

    fun createToken(loginId: Any, deviceType: String? = null): String

    fun parseToken(token: String): TokenInfo?

    fun validateToken(token: String): Boolean

    fun refreshToken(token: String): String

    fun removeToken(token: String)

    fun getTokenTimeout(token: String): Long

    fun getTokenValue(): String?

    fun setTokenValue(token: String)

    fun clearTokenValue()
}