package com.only.engine.entity

import com.only.engine.enums.CaptchaChannel

data class GenerateResult(
    val captchaId: String,
    val channel: CaptchaChannel,
    val inlineContent: CaptchaContent?, // 若是 INLINE_RESPONSE 可直接给前端
)
