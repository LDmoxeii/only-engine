package com.only.engine.captcha.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("only.engine.captcha")
class CaptchaProperties(

    var enable: Boolean = false,

    var provider: ProviderConfig = ProviderConfig(),

    var verifyPolicy: VerifyPolicyConfig = VerifyPolicyConfig(),
) {
    data class ProviderConfig(
        val generator: String = "",

        val sender: String = "",

        val store: String = "redis",
    )

    data class VerifyPolicyConfig(
        val onceOnly: Boolean = true,
        val deleteOnFail: Boolean = false,
        val caseInsensitive: Boolean = true,
    )
}
