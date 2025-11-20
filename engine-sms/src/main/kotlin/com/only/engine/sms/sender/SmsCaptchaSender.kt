package com.only.engine.sms.sender

import com.only.engine.entity.SendContext
import com.only.engine.enums.CaptchaChannel
import com.only.engine.spi.captcha.CaptchaSender
import org.slf4j.LoggerFactory

/**
 * CaptchaSender 实现：基于短信通道发送验证码。
 *
 * 当前实现只是将发送意图打到日志中，方便后续对接具体短信服务商。
 */
class SmsCaptchaSender : CaptchaSender {

    private val log = LoggerFactory.getLogger(SmsCaptchaSender::class.java)

    override fun supports(channel: CaptchaChannel): Boolean = channel == CaptchaChannel.SMS

    override fun send(ctx: SendContext) {
        // 这里预留对接第三方短信网关的扩展点：
        // - ctx.targets: 目标手机号列表
        // - ctx.displayContent(): 实际验证码内容
        // - ctx.templateCode: 可选的短信模板编码
        // - ctx.record.bizType: 业务类型（如 login-sms、bind-phone 等）
        log.info(
            "Send SMS captcha, bizType={}, targets={}, templateCode={}, content={}",
            ctx.record.bizType,
            ctx.targets,
            ctx.templateCode,
            ctx.displayContent(),
        )
    }
}

