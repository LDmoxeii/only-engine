package com.only.engine.captcha.send

import com.only.engine.captcha.core.CaptchaSender
import com.only.engine.captcha.enums.CaptchaChannel

class InlineResponseSender : CaptchaSender {
    override fun supports(channel: CaptchaChannel) = channel == CaptchaChannel.INLINE_RESPONSE
    override fun send(ctx: SendContext) {
        // 同步返回时一般不做实际发送，这里留空或记录日志
    }
}

