package com.only.engine.captcha.send

import com.only.engine.entity.SendContext
import com.only.engine.enums.CaptchaChannel
import com.only.engine.spi.captcha.CaptchaSender


class SmsSender : CaptchaSender {
    override fun supports(channel: CaptchaChannel) = channel == CaptchaChannel.SMS
    override fun send(ctx: SendContext) {
        // 集成短信网关
        // ctx.targets, ctx.displayContent()
    }
}
