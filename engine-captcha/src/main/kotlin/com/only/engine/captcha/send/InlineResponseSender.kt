package com.only.engine.captcha.send

import com.only.engine.entity.SendContext
import com.only.engine.enums.CaptchaChannel
import com.only.engine.spi.captcha.CaptchaSender


class InlineResponseSender : CaptchaSender {
    override fun supports(channel: CaptchaChannel) = channel == CaptchaChannel.INLINE
    override fun send(ctx: SendContext) = Unit
}

