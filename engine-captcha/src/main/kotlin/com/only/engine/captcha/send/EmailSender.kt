package com.only.engine.captcha.send

import com.only.engine.captcha.core.CaptchaSender
import com.only.engine.captcha.enums.CaptchaChannel

class EmailSender : CaptchaSender {
    override fun supports(channel: CaptchaChannel) = channel == CaptchaChannel.EMAIL
    override fun send(ctx: SendContext) {
        // 集成邮件服务
    }
}
