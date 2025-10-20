package com.only.engine.captcha.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("only.engine.captcha")
class CaptchaProperties(

    var enable: Boolean = false,

    var provider: ProviderConfig = ProviderConfig(),

    var verifyPolicy: VerifyPolicyConfig = VerifyPolicyConfig(),
) {
    class ProviderConfig {
        var generator: String = ""

        var sender: String = ""

        var store: String = "redis"
    }

    class VerifyPolicyConfig {
        var onceOnly: Boolean = true
        var deleteOnFail: Boolean = false
        var caseInsensitive: Boolean = true
    }
}
