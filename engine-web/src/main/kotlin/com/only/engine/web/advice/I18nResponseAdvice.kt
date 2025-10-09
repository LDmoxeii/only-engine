package com.only.engine.web.advice

import com.only.engine.entity.Result
import com.only.engine.web.annotation.IgnoreI18n
import com.only.engine.web.i18n.I18nMessageHandler
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.MethodParameter
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.core.annotation.Order
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice

@Order(4)
@AutoConfiguration
@RestControllerAdvice
@ConditionalOnProperty(prefix = "only.engine.web.i18n", name = ["enable"], havingValue = "true")
class I18nResponseAdvice(
    i18nMessageHandlerObjectProvider: ObjectProvider<I18nMessageHandler>,
) : ResponseBodyAdvice<Any> {

    private val i18nMessageHandler: I18nMessageHandler? =
        i18nMessageHandlerObjectProvider.ifAvailable

    override fun supports(
        returnType: MethodParameter,
        converterType: Class<out HttpMessageConverter<*>>,
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
        response: ServerHttpResponse,
    ): Any? = if (body is Result<*> && i18nMessageHandler != null) {
        val message = body.message
        body.message
        body.message = i18nMessageHandler.getMessage(code = message, defaultMessage = message)
    } else body

}
