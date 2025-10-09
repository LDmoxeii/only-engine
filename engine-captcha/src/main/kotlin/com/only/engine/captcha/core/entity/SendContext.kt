package com.only.engine.captcha.core.entity

import com.only.engine.captcha.core.enums.CaptchaChannel


data class SendContext(
    val channel: CaptchaChannel,
    val record: CaptchaRecord,
    val rawContent: CaptchaContent,
    val targets: List<String>,
    val templateCode: String?,
) {
    fun displayContent(): String =
        when (rawContent) {
            is CaptchaContent.Image -> rawContent.text
            is CaptchaContent.Text -> rawContent.value
        }
}
