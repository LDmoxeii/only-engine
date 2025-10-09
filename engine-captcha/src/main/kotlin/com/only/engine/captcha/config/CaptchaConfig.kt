package com.only.engine.captcha.config

import com.only.engine.captcha.CaptchaManager
import com.only.engine.captcha.config.properties.CaptchaProperties
import com.only.engine.captcha.generate.ImageCaptchaGenerator
import com.only.engine.captcha.generate.TextCaptchaGenerator
import com.only.engine.captcha.send.EmailSender
import com.only.engine.captcha.send.InlineResponseSender
import com.only.engine.captcha.send.SmsSender
import com.only.engine.captcha.store.InMemoryCaptchaStore
import com.only.engine.spi.captcha.CaptchaStore
import org.springframework.context.annotation.Bean

class CaptchaConfig(
    private val properties: CaptchaProperties,
) {

    @Bean
    fun captchaStore(): CaptchaStore = InMemoryCaptchaStore()

    @Bean
    fun captchaService(store: CaptchaStore): CaptchaManager =
        CaptchaManager(
            generators = listOf(TextCaptchaGenerator(), ImageCaptchaGenerator()),
            senders = listOf(InlineResponseSender(), SmsSender(), EmailSender()),
            store = store,
            config = properties
        )
}
