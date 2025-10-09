package com.only.engine.captcha.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("only.engine.captcha")
class CaptchaProperties(

    var enable: Boolean = true,

    var verifyPolicy: VerifyPolicyConfig = VerifyPolicyConfig(),
) {
    data class VerifyPolicyConfig(
        val onceOnly: Boolean = true,          // 成功后删除
        val deleteOnFail: Boolean = false,     // 失败即删除
        val caseInsensitive: Boolean = true,
    )
}
