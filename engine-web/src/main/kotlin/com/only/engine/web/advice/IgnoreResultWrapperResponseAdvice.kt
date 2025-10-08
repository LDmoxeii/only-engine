package com.only.engine.web.advice

import com.only.engine.entity.Result
import com.only.engine.web.WebInitPrinter
import com.only.engine.web.annotation.IgnoreResultWrapper
import com.only.engine.web.misc.AdviceUtils
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

@Order(3)
@AutoConfiguration
@RestControllerAdvice(basePackages = ["edu.only"])
@ConditionalOnProperty(prefix = "only.engine.web.result-wrapper", name = ["enable"], havingValue = "true")
class IgnoreResultWrapperResponseAdvice : ResponseBodyAdvice<Any>, WebInitPrinter {

    companion object {
        private val log = LoggerFactory.getLogger(IgnoreResultWrapperResponseAdvice::class.java)
    }

    init {
        printInit(IgnoreResultWrapperResponseAdvice::class.java, log)
    }

    override fun supports(returnType: MethodParameter, converterType: Class<out HttpMessageConverter<*>>): Boolean {
        return returnType.hasAnnotationOrClassAnnotation<IgnoreResultWrapper>()
                || AdviceUtils.realHasAnnotation(returnType, IgnoreResultWrapper::class.java)
    }

    override fun beforeBodyWrite(
        body: Any?,
        returnType: MethodParameter,
        selectedContentType: MediaType,
        selectedConverterType: Class<out HttpMessageConverter<*>>,
        request: ServerHttpRequest,
        response: ServerHttpResponse
    ): Any? = (body as? Result<*>)?.data ?: body

}

inline fun <reified T : Annotation> MethodParameter.hasAnnotationOrClassAnnotation(): Boolean {
    return this.hasMethodAnnotation(T::class.java) || AnnotatedElementUtils.hasAnnotation(
        this.containingClass,
        T::class.java
    )
}
