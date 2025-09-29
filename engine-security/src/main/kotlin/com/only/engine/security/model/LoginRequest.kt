package com.only.engine.security.model

data class LoginRequest(
    val username: String,
    val password: String,
    val deviceType: String? = null,
    val extra: Map<String, Any> = emptyMap(),
)
