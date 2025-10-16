package com.only.engine.web.advice

import com.only.engine.entity.Result
import com.only.engine.json.misc.JsonUtils
import org.springframework.core.MethodParameter
import org.springframework.core.annotation.Order
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice

@Order(5)
@RestControllerAdvice
class StringResponseAdvice(
    private val basePackages: List<String>,
) : ResponseBodyAdvice<Any> {

    override fun supports(
        returnType: MethodParameter,
        converterType: Class<out HttpMessageConverter<*>>,
    ): Boolean {
        val controllerPackage = returnType.containingClass.`package`?.name ?: ""
        return basePackages.any { controllerPackage.startsWith(it) }
    }

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
        JsonUtils.toJsonString(body)
    } else {
        body
    }
}
