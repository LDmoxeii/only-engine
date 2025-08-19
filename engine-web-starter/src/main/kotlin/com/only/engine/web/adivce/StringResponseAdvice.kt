package com.only.engine.web.adivce

import com.only.engine.web.WebInitPrinter
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.MethodParameter
import org.springframework.core.annotation.Order
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice

@Order(5)
@RestControllerAdvice(basePackages = ["com.only"])
@ConditionalOnProperty(prefix = "only.web", name = ["enable-result-wrapper"], matchIfMissing = true)
class StringResponseAdvice : ResponseBodyAdvice<Any>, WebInitPrinter {

    companion object {
        private val log = LoggerFactory.getLogger(StringResponseAdvice::class.java)
    }

    init {
        printInit(StringResponseAdvice::class.java, log)
    }

    override fun supports(
        returnType: MethodParameter,
        converterType: Class<out HttpMessageConverter<*>>
    ): Boolean = true

    override fun beforeBodyWrite(
        body: Any?,
        returnType: MethodParameter,
        selectedContentType: MediaType,
        selectedConverterType: Class<out HttpMessageConverter<*>>,
        request: ServerHttpRequest,
        response: ServerHttpResponse
    ): Any? = TODO("等待标准响应格式定义")
//        if (body is YmResult &&
//        returnType.method?.returnType?.isAssignableFrom(String::class.java) == true
//    ) {
//        YmWebMessageConverterUtils.toJsonString(body)
//    } else {
//        body
//    }
}
