package com.only.engine.web.interceptor

import cn.hutool.core.map.MapUtil
import cn.hutool.core.util.ObjectUtil
import com.only.engine.json.misc.JsonUtils
import com.only.engine.web.WebInitPrinter
import com.only.engine.web.filter.CachedBodyHttpServletRequest
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.commons.lang3.time.StopWatch
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView

/**
 * Web 请求性能拦截器
 *
 * 记录请求的开始时间、结束时间和耗时，并打印请求参数
 *
 * @author LD_moxeii
 */
class WebPerformanceInterceptor(
    private val slowRequestThreshold: Long = 3000L,
) : HandlerInterceptor, WebInitPrinter {

    companion object {
        private val log = LoggerFactory.getLogger(WebPerformanceInterceptor::class.java)
        private val stopWatchThreadLocal = ThreadLocal<StopWatch>()
    }

    init {
        printInit(WebPerformanceInterceptor::class.java, log)
    }

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val url = "${request.method} ${request.requestURI}"

        // 打印请求参数
        if (isJsonRequest(request)) {
            val jsonParam = getJsonParam(request)
            log.info("[ONLY]开始请求 => URL[{}],参数类型[json],参数:[{}]", url, jsonParam)
        } else {
            val parameterMap = request.parameterMap
            if (MapUtil.isNotEmpty(parameterMap)) {
                val parameters = JsonUtils.toJsonString(parameterMap)
                log.info("[ONLY]开始请求 => URL[{}],参数类型[param],参数:[{}]", url, parameters)
            } else {
                log.info("[ONLY]开始请求 => URL[{}],无参数", url)
            }
        }

        // 启动计时器
        val stopWatch = StopWatch()
        stopWatchThreadLocal.set(stopWatch)
        stopWatch.start()

        return true
    }

    override fun postHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        modelAndView: ModelAndView?,
    ) {
        // 不需要在这里处理
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?,
    ) {
        val stopWatch = stopWatchThreadLocal.get()
        if (ObjectUtil.isNotNull(stopWatch)) {
            stopWatch.stop()
            val time = stopWatch.time
            val url = "${request.method} ${request.requestURI}"

            // 判断是否为慢请求
            val threshold = slowRequestThreshold
            if (time > threshold) {
                log.warn("[ONLY]慢请求告警 => URL[{}],耗时:[{}]毫秒,阈值:[{}]毫秒", url, time, threshold)
            } else {
                log.info("[ONLY]结束请求 => URL[{}],耗时:[{}]毫秒", url, time)
            }

            stopWatchThreadLocal.remove()
        }
    }

    /**
     * 判断本次请求的数据类型是否为 JSON
     */
    private fun isJsonRequest(request: HttpServletRequest): Boolean {
        val contentType = request.contentType ?: return false
        return contentType.startsWith(MediaType.APPLICATION_JSON_VALUE, ignoreCase = true)
    }

    /**
     * 获取 JSON 参数
     */
    private fun getJsonParam(request: HttpServletRequest): String {
        val rawPayload = when (request) {
            is CachedBodyHttpServletRequest -> request.cachedBodyAsString()
            else -> ""
        }
        return JsonUtils.compressJson(rawPayload)
    }
}
