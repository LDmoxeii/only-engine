package com.only.engine.web.advice

import com.only.engine.entity.Result
import com.only.engine.web.WebInitPrinter
import com.only.engine.web.annotation.IgnoreResultWrapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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

@Order(2)
@AutoConfiguration
@RestControllerAdvice
@ConditionalOnProperty(prefix = "only.engine.web.result-wrapper", name = ["enable"], havingValue = "true")
class ResponseAdvice : ResponseBodyAdvice<Any>, WebInitPrinter {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ResponseAdvice::class.java)
    }

    init {
        printInit(ResponseAdvice::class.java, log)
    }

    override fun supports(
        returnType: MethodParameter,
        converterType: Class<out HttpMessageConverter<*>>,
    ): Boolean = !AnnotatedElementUtils.hasAnnotation(returnType.containingClass, IgnoreResultWrapper::class.java) &&
            !returnType.hasMethodAnnotation(IgnoreResultWrapper::class.java)

    override fun beforeBodyWrite(
        body: Any?,
        returnType: MethodParameter,
        selectedContentType: MediaType,
        selectedConverterType: Class<out HttpMessageConverter<*>>,
        request: ServerHttpRequest,
        response: ServerHttpResponse,
    ): Any = body as? Result<*> ?: Result.ok(body)
}
