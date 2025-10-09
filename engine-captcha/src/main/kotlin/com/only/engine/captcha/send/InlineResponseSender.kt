package com.only.engine.captcha.send

import com.only.engine.captcha.core.entity.SendContext
import com.only.engine.captcha.core.enums.CaptchaChannel
import com.only.engine.spi.captcha.CaptchaSender


class InlineResponseSender : CaptchaSender {
    override fun supports(channel: CaptchaChannel) = channel == CaptchaChannel.INLINE
    override fun send(ctx: SendContext) {
        // 同步返回时一般不做实际发送，这里留空或记录日志
    }
}

