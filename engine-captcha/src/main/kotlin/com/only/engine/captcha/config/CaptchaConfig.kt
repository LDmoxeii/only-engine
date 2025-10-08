package com.only.engine.captcha.config

import com.only.engine.captcha.CaptchaService
import com.only.engine.captcha.config.properties.CaptchaProperties
import com.only.engine.captcha.core.CaptchaStore
import com.only.engine.captcha.generate.ImageCaptchaGenerator
import com.only.engine.captcha.generate.TextCaptchaGenerator
import com.only.engine.captcha.send.EmailSender
import com.only.engine.captcha.send.InlineResponseSender
import com.only.engine.captcha.send.SmsSender
import com.only.engine.captcha.store.InMemoryCaptchaStore
import org.springframework.context.annotation.Bean

class CaptchaConfig(
    private val properties: CaptchaProperties,
) {

    @Bean
    fun captchaStore(): CaptchaStore = InMemoryCaptchaStore()

    @Bean
    fun captchaService(store: CaptchaStore): CaptchaService =
        CaptchaService(
            generators = listOf(TextCaptchaGenerator(), ImageCaptchaGenerator()),
            senders = listOf(InlineResponseSender(), SmsSender(), EmailSender()),
            store = store,
            config = properties
        )
}
