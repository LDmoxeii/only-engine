package com.only.engine.security.model

data class SecurityContext(
    val userInfo: UserInfo?,
    val token: String?,
    val tokenInfo: Map<String, Any?> = emptyMap(),
    val extra: Map<String, Any> = emptyMap(),
)
