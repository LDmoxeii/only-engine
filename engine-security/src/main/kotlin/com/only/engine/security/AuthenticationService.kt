package com.only.engine.security

import com.only.engine.security.model.LoginRequest
import com.only.engine.security.model.LoginResult
import com.only.engine.security.model.UserInfo

interface AuthenticationService {

    fun login(request: LoginRequest): LoginResult

    fun logout(token: String)

    fun checkLogin(token: String): Boolean

    fun getCurrentUser(): UserInfo?

    fun getTokenInfo(token: String): Map<String, Any?>

    fun refreshToken(token: String): String

    fun isLogin(): Boolean

    fun getLoginId(): Any?
}