package com.only.engine.web.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "only.engine.web.advice")
class AdviceProperties(
    var i18n: I18nProperties = I18nProperties(),
    var globalExceptionHandler: GlobalExceptionProperties = GlobalExceptionProperties(),
    var responseWrapper: ResponseWrapperProperties = ResponseWrapperProperties(),
    var basePackages: List<String>,
) {
    class I18nProperties {
        /** 是否启用国际化，默认 false */
        var enable: Boolean = false
    }

    class GlobalExceptionProperties {
        /** 是否启用全局异常处理，默认 true */
        var enable: Boolean = true
    }

    class ResponseWrapperProperties {
        /** 是否启用响应包装，默认 true */
        var enable: Boolean = true
    }
}
