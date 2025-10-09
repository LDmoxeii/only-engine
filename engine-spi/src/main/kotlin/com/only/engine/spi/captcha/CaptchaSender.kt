package com.only.engine.spi.captcha

import com.only.engine.entity.SendContext
import com.only.engine.enums.CaptchaChannel


interface CaptchaSender {
    fun supports(channel: CaptchaChannel): Boolean
    fun send(ctx: SendContext)
}
