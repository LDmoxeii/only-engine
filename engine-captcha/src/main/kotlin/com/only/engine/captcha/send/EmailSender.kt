package com.only.engine.captcha.send

import com.only.engine.captcha.core.entity.SendContext
import com.only.engine.captcha.core.enums.CaptchaChannel
import com.only.engine.spi.captcha.CaptchaSender


class EmailSender : CaptchaSender {
    override fun supports(channel: CaptchaChannel) = channel == CaptchaChannel.EMAIL
    override fun send(ctx: SendContext) {
        // 集成邮件服务
    }
}
