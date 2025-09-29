package com.only.engine.security.model

data class LoginResult(
    val token: String,
    val tokenTimeout: Long,
    val userInfo: UserInfo,
    val extra: Map<String, Any> = emptyMap(),
)
