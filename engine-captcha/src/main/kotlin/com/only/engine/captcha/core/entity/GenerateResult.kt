package com.only.engine.captcha.core.entity

import com.only.engine.captcha.core.enums.CaptchaChannel


data class GenerateResult(
    val captchaId: String,
    val channel: CaptchaChannel,
    val inlineContent: CaptchaContent?, // 若是 INLINE_RESPONSE 可直接给前端
)
