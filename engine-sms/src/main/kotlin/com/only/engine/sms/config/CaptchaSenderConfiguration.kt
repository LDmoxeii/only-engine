package com.only.engine.sms.config

import com.only.engine.sms.SmsInitPrinter
import com.only.engine.sms.sender.SmsCaptchaSender
import com.only.engine.spi.captcha.CaptchaSender
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean

@AutoConfiguration
class CaptchaSenderConfiguration : SmsInitPrinter {

    companion object {
        private val log = LoggerFactory.getLogger(SmsCaptchaSender::class.java)
    }

    @Bean
    fun smsCaptchaSender(): CaptchaSender {
        printInit(SmsCaptchaSender::class.java, log)
        return SmsCaptchaSender()
    }
}
