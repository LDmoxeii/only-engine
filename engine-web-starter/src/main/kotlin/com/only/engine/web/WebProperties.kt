package com.only.engine.web

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Web模块配置属性
 * 提供类型安全的配置选项，支持JVM兼容性
 */
@ConfigurationProperties(prefix = "only.web")
data class WebProperties(

    /** 是否启用 Web 模块，默认 true */
    var enable: Boolean = true,

    /** 返回结果包装器配置 */
    var resultWrapper: ResultWrapperProperties = ResultWrapperProperties(),

    /** 全局异常处理器配置 */
    var exceptionHandler: ExceptionHandlerProperties = ExceptionHandlerProperties(),

    /** 审计处理器配置 */
    var audit: AuditProperties = AuditProperties(),

    /** 过滤器相关配置 */
    var filter: FilterProperties = FilterProperties(),

    /** 国际化相关配置 */
    var i18n: I18nProperties = I18nProperties()
) {

    /**
     * 返回结果包装器配置
     */
    data class ResultWrapperProperties(
        /** 是否启用返回结果包装，默认 true */
        var enable: Boolean = true,
        /** 忽略包装的路径集合，默认空 */
        var ignorePaths: Set<String> = emptySet(),
        /** 忽略包装的Controller类名集合，默认空 */
        var ignoreControllers: Set<String> = emptySet()
    )

    /**
     * 全局异常处理器配置
     */
    data class ExceptionHandlerProperties(
        /** 是否启用全局异常拦截，默认 true */
        var enable: Boolean = true,
        /** 异常日志级别，默认 INFO */
        var logLevel: LogLevel = LogLevel.INFO,
        /** 是否打印异常堆栈，默认 true */
        var printStackTrace: Boolean = true,
        /** 敏感异常信息替换文本，默认为"系统繁忙，请稍后重试" */
        var sensitiveMessageReplacement: String = "系统繁忙，请稍后重试"
    )

    /**
     * 审计处理器配置
     */
    data class AuditProperties(
        /** 是否启用返回结果审计处理，默认 true */
        var enable: Boolean = true,
        /** 审计日志级别，默认 DEBUG */
        var logLevel: LogLevel = LogLevel.DEBUG
    )

    /**
     * 国际化配置
     */
    data class I18nProperties(
        /** 是否启用国际化，默认 false */
        var enable: Boolean = false,
        /** 默认语言，默认 zh_CN */
        var defaultLocale: String = "zh_CN",
        /** 消息文件基础名，默认 messages */
        var basename: String = "messages",
        /** 缓存时间（秒），默认 3600 */
        var cacheSeconds: Int = 3600
    )

    /**
     * 过滤器配置
     */
    data class FilterProperties(

        /** 健康检查过滤器配置 */
        var healthCheck: HealthCheckFilterProperties = HealthCheckFilterProperties(),

        /** 线程本地变量过滤器配置 */
        var threadLocal: ThreadLocalFilterProperties = ThreadLocalFilterProperties(),

        /** 用户登录过滤器配置 */
        var userLogin: UserLoginFilterProperties = UserLoginFilterProperties(),

        /** 请求体包装过滤器配置 */
        var requestBody: RequestBodyFilterProperties = RequestBodyFilterProperties(),

        /** 请求追踪过滤器配置 */
        var track: TrackFilterProperties = TrackFilterProperties()
    )

    /**
     * 健康检查过滤器配置
     */
    data class HealthCheckFilterProperties(
        /** 是否启用健康检查，默认 true */
        var enable: Boolean = true,
        /** 健康检查路径，默认 /health */
        var healthPath: String = "/health"
    )

    /**
     * 线程本地变量过滤器配置
     */
    data class ThreadLocalFilterProperties(
        /** 是否启用线程本地变量，默认 true */
        var enable: Boolean = true
    )

    /**
     * 用户登录过滤器配置
     */
    data class UserLoginFilterProperties(
        /** 是否启用登录拦截，默认 false */
        var enable: Boolean = false,
        /** 不走拦截器的路径集合，默认空 */
        var skipPaths: MutableSet<String> = mutableSetOf(),
        /** 请求白名单 URL 列表，默认空 */
        var whitelistUrls: List<String> = emptyList()
    )

    /**
     * 请求追踪过滤器配置
     */
    data class TrackFilterProperties(

        /** 是否启用请求打印，默认 true */
        var enable: Boolean = true,

        /** 打印类型，默认 LOG */
        var printType: PrintType = PrintType.LOG,

        /** 日志级别，默认 INFO */
        var logLevel: LogLevel = LogLevel.INFO,

        /** 是否打印请求体，默认 true */
        var printRequestBody: Boolean = true,

        /** 是否打印响应体，默认 true */
        var printResponseBody: Boolean = true,

        /** 敏感字段（默认空） */
        var sensitiveKeys: Set<String> = emptySet(),

        /** 过滤 URI 集合（默认空） */
        var filterUris: Set<String> = emptySet(),

        /** 请求体最大打印长度，默认 1024 */
        var maxRequestBodyLength: Int = 1024,

        /** 响应体最大打印长度，默认 1024 */
        var maxResponseBodyLength: Int = 1024
    )

    /**
     * 请求体包装过滤器配置
     */
    data class RequestBodyFilterProperties(

        /** 是否启用请求体包装，默认 true */
        var enable: Boolean = true,

        /** 不需要包装的 URI 路径集合（默认空） */
        var filterUris: Set<String> = emptySet(),

        /** 不需要包装的Content-Type集合（默认空） */
        var filterContentTypes: Set<String> = emptySet()
    )

    /**
     * 打印类型枚举
     */
    enum class PrintType {
        /** 控制台输出 */
        CONSOLE,
        /** 日志输出 */
        LOG;
        
        companion object {
            @JvmStatic
            fun fromInt(value: Int): PrintType = when(value) {
                1 -> CONSOLE
                2 -> LOG
                else -> LOG
            }
        }
    }

    /**
     * 日志级别枚举
     */
    enum class LogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR;
        
        companion object {
            @JvmStatic
            fun fromString(level: String): LogLevel = 
                valueOf(level.uppercase())
        }
    }
    
    // ===================== Java兼容性方法 ===================== //
    
    /**
     * 兼容旧版本的boolean属性getter
     */
    @Deprecated("Use resultWrapper.enable instead", ReplaceWith("resultWrapper.enable"))
    fun getEnableResultWrapper(): Boolean = resultWrapper.enable
    
    @Deprecated("Use exceptionHandler.enable instead", ReplaceWith("exceptionHandler.enable"))
    fun getEnableExceptionHandler(): Boolean = exceptionHandler.enable
    
    @Deprecated("Use audit.enable instead", ReplaceWith("audit.enable"))
    fun getEnableAuditHandler(): Boolean = audit.enable
    
    /**
     * Java风格的构建器模式支持
     */
    class Builder {
        private val properties = WebProperties()
        
        fun enable(enable: Boolean) = apply { properties.enable = enable }
        fun resultWrapper(block: ResultWrapperProperties.() -> Unit) = apply {
            properties.resultWrapper.block()
        }
        fun exceptionHandler(block: ExceptionHandlerProperties.() -> Unit) = apply {
            properties.exceptionHandler.block()
        }
        fun build() = properties
    }
    
    companion object {
        @JvmStatic
        fun builder() = Builder()
    }
}
