package com.only.engine.captcha.config

import com.only.engine.captcha.CaptchaInitPrinter
import com.only.engine.captcha.CaptchaManager
import com.only.engine.captcha.config.properties.CaptchaProperties
import com.only.engine.captcha.generate.DefaultCaptchaGenerator
import com.only.engine.captcha.send.InlineResponseSender
import com.only.engine.spi.captcha.CaptchaGenerator
import com.only.engine.spi.captcha.CaptchaSender
import com.only.engine.spi.captcha.CaptchaStore
import com.only.engine.spi.idempotent.TokenProvider
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.core.annotation.Order

@AutoConfiguration
@EnableConfigurationProperties(CaptchaProperties::class)
class CaptchaConfig(
    private val properties: CaptchaProperties,
) : CaptchaInitPrinter {

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(CaptchaConfig::class.java)
    }

    @Bean
    @Order
    fun imageCaptchaGenerator(): CaptchaGenerator {
        printInit(DefaultCaptchaGenerator::class.java, log)
        return DefaultCaptchaGenerator()
    }

    @Bean
    @Order
    fun inlineCaptchaSender(): CaptchaSender {
        printInit(CaptchaSender::class.java, log)
        return InlineResponseSender()
    }

    @Bean
    @ConditionalOnBean(TokenProvider::class)
    fun captchaService(
        generators: ObjectProvider<CaptchaGenerator>,
        store: CaptchaStore,
        senders: ObjectProvider<CaptchaSender>,
    ): CaptchaManager {
        printInit(CaptchaManager::class.java, log)
        return CaptchaManager(
            generators = generators.orderedStream().toList(),
            store = store,
            senders = senders.orderedStream().toList(),
            config = properties
        )
    }
}
