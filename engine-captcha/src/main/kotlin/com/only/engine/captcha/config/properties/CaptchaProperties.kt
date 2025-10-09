package com.only.engine.captcha.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("only.engine.captcha")
class CaptchaProperties(

    var enable: Boolean = false,

    // spi 提供商
    var provider: ProviderConfig = ProviderConfig(),

    var verifyPolicy: VerifyPolicyConfig = VerifyPolicyConfig(),
) {
    /**
     * SPI 提供商配置
     */
    data class ProviderConfig(
        /**
         * CaptchaGenerator 提供商名称
         */
        val generator: String = "",

        /**
         * CaptchaSender 提供商名称
         */
        val sender: String = "",

        /**
         * CaptchaStore 提供商名称
         */
        val store: String = "redis",
    )

    data class VerifyPolicyConfig(
        val onceOnly: Boolean = true,          // 成功后删除
        val deleteOnFail: Boolean = false,     // 失败即删除
        val caseInsensitive: Boolean = true,
    )
}
