package com.only.engine.web.adivce

import com.only.engine.web.WebInitPrinter
import com.only.engine.web.annotation.IgnoreI18n
import com.only.engine.web.i18n.I18nMessageHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.beans.factory.ObjectProvider
import org.springframework.core.MethodParameter
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.core.annotation.Order
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice
import kotlin.jvm.java

@Order(4)
@RestControllerAdvice(basePackages = ["com.only"])
@ConditionalOnProperty(prefix = "only.web.i18n", name = ["enable"], havingValue = "true")
class I18nResponseAdvice(
    i18nMessageHandlerObjectProvider: ObjectProvider<I18nMessageHandler>
) : ResponseBodyAdvice<Any>, WebInitPrinter {

    private val i18nMessageHandler: I18nMessageHandler? =
        i18nMessageHandlerObjectProvider.ifAvailable

    companion object {
        private val log: Logger = LoggerFactory.getLogger(I18nResponseAdvice::class.java)
    }

    init {
        printInit(I18nResponseAdvice::class.java, log)
    }

    override fun supports(
        returnType: MethodParameter,
        converterType: Class<out HttpMessageConverter<*>>
    ): Boolean {
        return !AnnotatedElementUtils.hasAnnotation(returnType.containingClass, IgnoreI18n::class.java) &&
               !returnType.hasMethodAnnotation(IgnoreI18n::class.java)
    }

    override fun beforeBodyWrite(
        body: Any?,
        returnType: MethodParameter,
        selectedContentType: MediaType,
        selectedConverterType: Class<out HttpMessageConverter<*>>,
        request: ServerHttpRequest,
        response: ServerHttpResponse
    ): Any? = TODO("等待标准响应格式定义")
//        if (body is YmResult<*> && i18nMessageHandler != null) {
//            val message = body.message
//            body.message(i18nMessageHandler.getMessage(message, message))
//        }
//        return body

}
