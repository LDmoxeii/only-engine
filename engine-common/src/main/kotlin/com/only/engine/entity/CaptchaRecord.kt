package com.only.engine.entity

import com.only.engine.enums.CaptchaType
import java.time.Instant

data class CaptchaRecord(
    val id: String,
    val bizType: String,
    val type: CaptchaType,
    val contentHash: String,          // 可明文存储也可哈希
    val expireAt: Instant,
    val used: Boolean = false,
    val failCount: Int = 0,
    val metadata: Map<String, Any?> = emptyMap(),
)
