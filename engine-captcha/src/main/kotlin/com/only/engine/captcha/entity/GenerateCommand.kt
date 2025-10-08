package com.only.engine.captcha.entity

import com.only.engine.captcha.enums.CaptchaChannel
import com.only.engine.captcha.enums.CaptchaType

data class GenerateCommand(
    val bizType: String,
    val type: CaptchaType,
    val channel: CaptchaChannel,
    val length: Int = 4,
    val width: Int? = null,
    val height: Int? = null,
    val charsetPolicy: CharsetPolicy = CharsetPolicy.MIXED,
    val ttlSeconds: Long = 300,
    val targets: List<String> = emptyList(),    // 手机 / 邮箱
    val templateCode: String? = null,
    val metadata: Map<String, Any?> = emptyMap(),
) {
    enum class CharsetPolicy { NUMERIC, ALPHA, MIXED }
}
