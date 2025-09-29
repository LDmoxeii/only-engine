package com.only.engine.entity

data class UserInfo(
    val id: Any,
    val username: String,
    val roles: Set<String> = emptySet(),
    val permissions: Set<String> = emptySet(),
    val extra: Map<String, Any> = emptyMap(),
)
