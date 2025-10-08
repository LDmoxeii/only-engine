package com.only.engine.captcha.send

import com.only.engine.captcha.core.CaptchaSender
import com.only.engine.captcha.enums.CaptchaChannel

class SmsSender : CaptchaSender {
    override fun supports(channel: CaptchaChannel) = channel == CaptchaChannel.SMS
    override fun send(ctx: SendContext) {
        // 集成短信网关
        // ctx.targets, ctx.displayContent()
    }
}
