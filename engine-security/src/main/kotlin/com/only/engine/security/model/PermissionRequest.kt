package com.only.engine.security.model

data class PermissionRequest(
    val resource: String,
    val action: String,
    val extra: Map<String, Any> = emptyMap(),
)
