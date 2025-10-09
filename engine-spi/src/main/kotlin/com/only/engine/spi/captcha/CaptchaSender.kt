package com.only.engine.spi.captcha

import com.only.engine.captcha.core.entity.SendContext
import com.only.engine.captcha.core.enums.CaptchaChannel


interface CaptchaSender {
    fun supports(channel: CaptchaChannel): Boolean
    fun send(ctx: SendContext)
}
