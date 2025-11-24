package com.only.engine.web.config

import cn.dev33.satoken.exception.NotLoginException
import com.only.engine.web.WebInitPrinter
import com.only.engine.web.advice.GlobalExceptionHandlerAdvice
import com.only.engine.web.advice.I18nResponseAdvice
import com.only.engine.web.advice.IgnoreResultWrapperResponseAdvice
import com.only.engine.web.advice.RedisExceptionHandler
import com.only.engine.web.advice.ResponseAdvice
import com.only.engine.web.advice.SaTokenExceptionHandlerAdvice
import com.only.engine.web.advice.SmsExceptionHandler
import com.only.engine.web.advice.StringResponseAdvice
import com.only.engine.web.config.properties.AdviceProperties
import com.only.engine.web.i18n.I18nMessageDefaultHandler
import com.only.engine.web.i18n.I18nMessageHandler
import com.baomidou.lock.exception.LockFailureException
import org.dromara.sms4j.comm.exception.SmsBlendException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.support.ResourceBundleMessageSource
import java.nio.charset.StandardCharsets

@AutoConfiguration
@EnableConfigurationProperties(AdviceProperties::class)
class AdviceAutoConfiguration : WebInitPrinter {

    companion object {
        private val log = LoggerFactory.getLogger(AdviceAutoConfiguration::class.java)

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
    @ConditionalOnProperty(prefix = "only.engine.web.advice.i18n", name = ["enable"], havingValue = "true")
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
    @ConditionalOnProperty(prefix = "only.engine.web.i18n", name = ["enable"], havingValue = "true")
    fun i18nMessageHandler(@Qualifier(I18N_MESSAGE_SOURCE) messageSource: MessageSource): I18nMessageHandler {
        printInit(I18nMessageDefaultHandler::class.java, log)
        return I18nMessageDefaultHandler(messageSource)
    }

    @Bean
    @ConditionalOnProperty(
        prefix = "only.engine.web.advice.global-exception-handler",
        name = ["enable"],
        havingValue = "true"
    )
    fun globalExceptionHandlerAdvice(): GlobalExceptionHandlerAdvice {
        printInit(GlobalExceptionHandlerAdvice::class.java, log)
        return GlobalExceptionHandlerAdvice()
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(NotLoginException::class)
    @ConditionalOnProperty(prefix = "only.engine.sa-token", name = ["enable"], havingValue = "true")
    fun saTokenExceptionHandlerAdvice(): SaTokenExceptionHandlerAdvice {
        printInit(SaTokenExceptionHandlerAdvice::class.java, log)
        return SaTokenExceptionHandlerAdvice()
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(LockFailureException::class)
    fun redisExceptionHandler(): RedisExceptionHandler {
        printInit(RedisExceptionHandler::class.java, log)
        return RedisExceptionHandler()
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(SmsBlendException::class)
    fun smsExceptionHandler(): SmsExceptionHandler {
        printInit(SmsExceptionHandler::class.java, log)
        return SmsExceptionHandler()
    }

    @Bean
    @ConditionalOnProperty(prefix = "only.engine.web.advice.response-wrapper", name = ["enable"], havingValue = "true")
    fun ignoreResultWrapperResponseAdvice(properties: AdviceProperties): IgnoreResultWrapperResponseAdvice {
        printInit(IgnoreResultWrapperResponseAdvice::class.java, log)
        return IgnoreResultWrapperResponseAdvice(properties.basePackages)
    }

    @Bean
    @ConditionalOnProperty(prefix = "only.engine.web.advice.response-wrapper", name = ["enable"], havingValue = "true")
    fun responseAdvice(properties: AdviceProperties): ResponseAdvice {
        printInit(ResponseAdvice::class.java, log)
        return ResponseAdvice(properties.basePackages)
    }

    @Bean
    @ConditionalOnProperty(prefix = "only.engine.web.advice.response-wrapper", name = ["enable"], havingValue = "true")
    fun stringResponseAdvice(properties: AdviceProperties): StringResponseAdvice {
        printInit(StringResponseAdvice::class.java, log)
        return StringResponseAdvice(properties.basePackages)
    }

    @Bean
    @ConditionalOnProperty(prefix = "only.engine.web.advice.i18n", name = ["enable"], havingValue = "true")
    fun i18nResponseAdvice(
        i18nMessageHandlerObjectProvider: ObjectProvider<I18nMessageHandler>,
        properties: AdviceProperties
    ): I18nResponseAdvice {
        printInit(I18nResponseAdvice::class.java, log)
        return I18nResponseAdvice(i18nMessageHandlerObjectProvider, properties.basePackages)
    }
}
