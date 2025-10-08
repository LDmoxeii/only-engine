package com.only.engine.web.config

import com.only.engine.web.WebInitPrinter
import com.only.engine.web.config.properties.I18nProperties
import com.only.engine.web.i18n.I18nMessageDefaultHandler
import com.only.engine.web.i18n.I18nMessageHandler
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.support.ResourceBundleMessageSource
import java.nio.charset.StandardCharsets

@AutoConfiguration
@ConditionalOnProperty(prefix = "only.web.i18n", name = ["enable"], havingValue = "true")
@EnableConfigurationProperties(I18nProperties::class)
class I18nConfiguration : WebInitPrinter {

    companion object {
        private val log = LoggerFactory.getLogger(I18nConfiguration::class.java)

        /** 国际化消息源 Bean 名称 */
        const val I18N_MESSAGE_SOURCE = "i18nMessageSource"

        /** 国际化消息处理器 Bean 名称 */
        const val I18N_MESSAGE_HANDLER = "i18nMessageHandler"
    }

    /**
     * 创建并配置国际化消息源
     */
    @Bean(name = [I18N_MESSAGE_SOURCE])
    @ConditionalOnMissingBean(name = [I18N_MESSAGE_SOURCE])
    fun i18nMessageSource(): MessageSource {
        printInit(I18N_MESSAGE_SOURCE, log)
        return ResourceBundleMessageSource().apply {
            setBasename("i18n/messages")
            setDefaultEncoding(StandardCharsets.UTF_8.name())
        }
    }

    /**
     * 创建国际化消息处理器
     */
    @Bean(name = [I18N_MESSAGE_HANDLER])
    @ConditionalOnMissingBean(name = [I18N_MESSAGE_HANDLER])
    fun i18nMessageHandler(@Qualifier(I18N_MESSAGE_SOURCE) messageSource: MessageSource): I18nMessageHandler {
        printInit(I18nMessageDefaultHandler::class.java, log)
        return I18nMessageDefaultHandler(messageSource)
    }
}
