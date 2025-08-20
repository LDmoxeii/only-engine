package com.only.engine.web.misc

import com.only.engine.web.advice.GlobalExceptionHandlerAdvice
import org.springframework.core.MethodParameter
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerMapping
import kotlin.jvm.java

object AdviceUtils {

    fun realHasAnnotation(returnType: MethodParameter, annotatedClass: Class<out Annotation>): Boolean {
        val realHandlerMethod = getRealHandlerMethod(returnType) ?: return false
        return AnnotatedElementUtils.hasAnnotation(realHandlerMethod.beanType, annotatedClass)
                || realHandlerMethod.hasMethodAnnotation(annotatedClass)
    }

    fun getRealHandlerMethod(returnType: MethodParameter): HandlerMethod? {
        if (GlobalExceptionHandlerAdvice::class.java.isAssignableFrom(returnType.containingClass)) {
            val requestAttributes = RequestContextHolder.getRequestAttributes()
            if (requestAttributes is ServletRequestAttributes) {
                val attribute = requestAttributes.request
                    .getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE)
                if (attribute is HandlerMethod) {
                    return attribute
                }
            }
        }
        return null
    }
}
