package com.only.engine.web.advice

import com.only.engine.entity.Result
import com.only.engine.web.WebInitPrinter
import com.only.engine.web.misc.WebMessageConverterUtils
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
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
@AutoConfiguration
@RestControllerAdvice(basePackages = ["edu.only"])
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
        converterType: Class<out HttpMessageConverter<*>>,
    ): Boolean = true

    override fun beforeBodyWrite(
        body: Any?,
        returnType: MethodParameter,
        selectedContentType: MediaType,
        selectedConverterType: Class<out HttpMessageConverter<*>>,
        request: ServerHttpRequest,
        response: ServerHttpResponse,
    ): Any? = if (body is Result<*> &&
        returnType.method!!.returnType.isAssignableFrom(String::class.java)
    ) {
        WebMessageConverterUtils.toJsonString(body)
    } else {
        body
    }
}
