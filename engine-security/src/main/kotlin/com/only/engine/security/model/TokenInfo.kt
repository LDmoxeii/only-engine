package com.only.engine.security.model

data class TokenInfo(
    val loginId: Any,
    val deviceType: String?,
    val tokenValue: String,
    val timeout: Long,
    val extra: Map<String, Any> = emptyMap(),
)
