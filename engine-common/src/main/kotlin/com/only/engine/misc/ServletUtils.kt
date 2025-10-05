package com.only.engine.misc

import cn.hutool.extra.servlet.JakartaServletUtil
import cn.hutool.http.HttpStatus
import jakarta.servlet.ServletRequest
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.springframework.http.MediaType
import org.springframework.util.LinkedCaseInsensitiveMap
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.io.IOException
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * 客户端工具类，提供获取请求参数、响应处理、头部信息等常用操作
 *
 * @author LD_moxeii
 */
object ServletUtils {

    /**
     * 获取指定名称的 String 类型的请求参数
     *
     * @param name 参数名
     * @return 参数值
     */
    fun getParameter(name: String): String? {
        return getRequest()?.getParameter(name)
    }

    /**
     * 获取指定名称的 String 类型的请求参数，若参数不存在，则返回默认值
     *
     * @param name         参数名
     * @param defaultValue 默认值
     * @return 参数值或默认值
     */
    fun getParameter(name: String, defaultValue: String): String {
        return getRequest()?.getParameter(name) ?: defaultValue
    }

    /**
     * 获取指定名称的 Integer 类型的请求参数
     *
     * @param name 参数名
     * @return 参数值
     */
    fun getParameterToInt(name: String): Int? {
        return getRequest()?.getParameter(name)?.toIntOrNull()
    }

    /**
     * 获取指定名称的 Integer 类型的请求参数，若参数不存在，则返回默认值
     *
     * @param name         参数名
     * @param defaultValue 默认值
     * @return 参数值或默认值
     */
    fun getParameterToInt(name: String, defaultValue: Int): Int {
        return getRequest()?.getParameter(name)?.toIntOrNull() ?: defaultValue
    }

    /**
     * 获取指定名称的 Boolean 类型的请求参数
     *
     * @param name 参数名
     * @return 参数值
     */
    fun getParameterToBool(name: String): Boolean? {
        return getRequest()?.getParameter(name)?.toBooleanStrictOrNull()
    }

    /**
     * 获取指定名称的 Boolean 类型的请求参数，若参数不存在，则返回默认值
     *
     * @param name         参数名
     * @param defaultValue 默认值
     * @return 参数值或默认值
     */
    fun getParameterToBool(name: String, defaultValue: Boolean): Boolean {
        return getRequest()?.getParameter(name)?.toBooleanStrictOrNull() ?: defaultValue
    }

    /**
     * 获取所有请求参数（以 Map 的形式返回）
     *
     * @param request 请求对象
     * @return 请求参数的 Map，键为参数名，值为参数值数组
     */
    fun getParams(request: ServletRequest): Map<String, Array<String>> = request.parameterMap

    /**
     * 获取所有请求参数（以 Map 的形式返回，值为字符串形式的拼接）
     *
     * @param request 请求对象
     * @return 请求参数的 Map，键为参数名，值为拼接后的字符串
     */
    fun getParamMap(request: ServletRequest): Map<String, String> {
        return getParams(request).mapValues { (_, values) ->
            values.joinToString(",")
        }
    }

    /**
     * 获取当前 HTTP 请求对象
     *
     * @return 当前 HTTP 请求对象
     */
    fun getRequest(): HttpServletRequest? {
        return try {
            getRequestAttributes()?.request
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 获取当前 HTTP 响应对象
     *
     * @return 当前 HTTP 响应对象
     */
    fun getResponse(): HttpServletResponse? {
        return try {
            getRequestAttributes()?.response
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 获取当前请求的 HttpSession 对象
     *
     * 如果当前请求已经关联了一个会话（即已经存在有效的 session ID），
     * 则返回该会话对象；如果没有关联会话，则会创建一个新的会话对象并返回。
     *
     * HttpSession 用于存储会话级别的数据，如用户登录信息、购物车内容等，
     * 可以在多个请求之间共享会话数据
     *
     * @return 当前请求的 HttpSession 对象
     */
    fun getSession(): HttpSession? {
        return getRequest()?.session
    }

    /**
     * 获取当前请求的请求属性
     *
     * @return 请求属性对象
     */
    fun getRequestAttributes(): ServletRequestAttributes? {
        return try {
            val attributes: RequestAttributes? = RequestContextHolder.getRequestAttributes()
            attributes as? ServletRequestAttributes
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 获取指定请求头的值，如果头部为空则返回空字符串
     *
     * @param request 请求对象
     * @param name    头部名称
     * @return 头部值
     */
    fun getHeader(request: HttpServletRequest, name: String): String {
        val value = request.getHeader(name)
        return if (value.isNullOrEmpty()) {
            ""
        } else {
            urlDecode(value)
        }
    }

    /**
     * 获取所有请求头的 Map，键为头部名称，值为头部值
     *
     * @param request 请求对象
     * @return 请求头的 Map
     */
    fun getHeaders(request: HttpServletRequest): Map<String, String> {
        val map = LinkedCaseInsensitiveMap<String>()
        val enumeration = request.headerNames
        if (enumeration != null) {
            while (enumeration.hasMoreElements()) {
                val key = enumeration.nextElement()
                val value = request.getHeader(key)
                map[key] = value
            }
        }
        return map
    }

    /**
     * 将字符串渲染到客户端（以 JSON 格式返回）
     *
     * @param response 渲染对象
     * @param string   待渲染的字符串
     */
    fun renderString(response: HttpServletResponse, string: String) {
        try {
            response.status = HttpStatus.HTTP_OK
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.characterEncoding = StandardCharsets.UTF_8.toString()
            response.writer.print(string)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 判断当前请求是否为 Ajax 异步请求
     *
     * @param request 请求对象
     * @return 是否为 Ajax 请求
     */
    fun isAjaxRequest(request: HttpServletRequest): Boolean {
        // 判断 Accept 头部是否包含 application/json
        val accept = request.getHeader("accept")
        if (accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE)) {
            return true
        }

        // 判断 X-Requested-With 头部是否包含 XMLHttpRequest
        val xRequestedWith = request.getHeader("X-Requested-With")
        if (xRequestedWith != null && xRequestedWith.contains("XMLHttpRequest")) {
            return true
        }

        // 判断 URI 后缀是否为 .json 或 .xml
        val uri = request.requestURI
        if (uri.endsWith(".json", ignoreCase = true) || uri.endsWith(".xml", ignoreCase = true)) {
            return true
        }

        // 判断请求参数 __ajax 是否为 json 或 xml
        val ajax = request.getParameter("__ajax")
        return ajax.equals("json", ignoreCase = true) || ajax.equals("xml", ignoreCase = true)
    }

    /**
     * 获取客户端 IP 地址
     *
     * @return 客户端 IP 地址
     */
    fun getClientIP(): String? {
        return getRequest()?.let { JakartaServletUtil.getClientIP(it) }
    }

    /**
     * 对内容进行 URL 编码
     *
     * @param str 内容
     * @return 编码后的内容
     */
    fun urlEncode(str: String): String {
        return URLEncoder.encode(str, StandardCharsets.UTF_8)
    }

    /**
     * 对内容进行 URL 解码
     *
     * @param str 内容
     * @return 解码后的内容
     */
    fun urlDecode(str: String): String {
        return URLDecoder.decode(str, StandardCharsets.UTF_8)
    }
}
