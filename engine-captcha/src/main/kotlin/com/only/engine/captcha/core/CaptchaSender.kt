package com.only.engine.captcha.core

import com.only.engine.captcha.entity.SendContext
import com.only.engine.captcha.enums.CaptchaChannel

interface CaptchaSender {
    fun supports(channel: CaptchaChannel): Boolean
    fun send(ctx: SendContext)
}
