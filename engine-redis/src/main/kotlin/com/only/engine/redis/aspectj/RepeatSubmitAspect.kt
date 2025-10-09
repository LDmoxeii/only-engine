package com.only.engine.redis.aspectj

import cn.hutool.core.util.ObjectUtil
import cn.hutool.crypto.SecureUtil
import com.fasterxml.jackson.databind.ObjectMapper
import com.only.engine.constants.Constants
import com.only.engine.entity.Result
import com.only.engine.exception.KnownException
import com.only.engine.misc.MessageUtils
import com.only.engine.misc.ServletUtils
import com.only.engine.redis.annotation.RepeatSubmit
import com.only.engine.redis.misc.RedisUtils
import com.only.engine.spi.Idempotent.TokenProvider
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.AfterReturning
import org.aspectj.lang.annotation.AfterThrowing
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.springframework.validation.BindingResult
import org.springframework.web.multipart.MultipartFile
import java.time.Duration
import java.util.*

/**
 * 防止重复提交切面
 *
 * 使用 TokenProvider 接口获取 Token，实现依赖倒置
 *
 * @author LD_moxeii
 */
@Aspect
class RepeatSubmitAspect(
    private val tokenProvider: TokenProvider,
    private val objectMapper: ObjectMapper,
) {

    companion object {
        private val KEY_CACHE = ThreadLocal<String>()
    }

    @Before("@annotation(repeatSubmit)")
    fun doBefore(point: JoinPoint, repeatSubmit: RepeatSubmit) {
        val interval = repeatSubmit.timeUnit.toMillis(repeatSubmit.interval.toLong())

        require(interval >= 1000) { "重复提交间隔时间不能小于 1 秒" }

        val request = ServletUtils.getRequest()!!
        val nowParams = argsArrayToString(point.args)
        val url = request.requestURI

        // 使用 TokenProvider 获取 Token（依赖倒置）
        val submitKey = SecureUtil.md5("${tokenProvider.getToken()}:$nowParams")
        val cacheRepeatKey = "${Constants.REPEAT_SUBMIT_KEY}$url:$submitKey"

        if (RedisUtils.setObjectIfAbsent(cacheRepeatKey, "", Duration.ofMillis(interval))) {
            KEY_CACHE.set(cacheRepeatKey)
        } else {
            var message = repeatSubmit.message
            if (message.startsWith("{") && message.endsWith("}")) {
                message = MessageUtils.message(message.substring(1, message.length - 1))
            }
            throw KnownException(message)
        }
    }

    @AfterReturning(pointcut = "@annotation(repeatSubmit)", returning = "jsonResult")
    fun doAfterReturning(joinPoint: JoinPoint, repeatSubmit: RepeatSubmit, jsonResult: Any?) {
        if (jsonResult is Result<*>) {
            try {
                // 成功则不删除 Redis 数据，保证在有效时间内无法重复提交
                if (jsonResult.code == 20000) {  // ResultCode.SUCCESS
                    return
                }
                KEY_CACHE.get()?.let { RedisUtils.deleteObject(it) }
            } finally {
                KEY_CACHE.remove()
            }
        }
    }

    @AfterThrowing(value = "@annotation(repeatSubmit)", throwing = "e")
    fun doAfterThrowing(joinPoint: JoinPoint, repeatSubmit: RepeatSubmit, e: Exception) {
        KEY_CACHE.get()?.let { RedisUtils.deleteObject(it) }
        KEY_CACHE.remove()
    }

    private fun argsArrayToString(paramsArray: Array<Any>?): String {
        if (paramsArray.isNullOrEmpty()) return ""

        val params = StringJoiner(" ")
        paramsArray.forEach { param ->
            if (ObjectUtil.isNotNull(param) && !isFilterObject(param)) {
                params.add(objectMapper.writeValueAsString(param))
            }
        }
        return params.toString()
    }

    private fun isFilterObject(obj: Any): Boolean {
        val clazz = obj.javaClass
        return when {
            clazz.isArray -> MultipartFile::class.java.isAssignableFrom(clazz.componentType)
            Collection::class.java.isAssignableFrom(clazz) ->
                (obj as Collection<*>).any { it is MultipartFile }

            Map::class.java.isAssignableFrom(clazz) ->
                (obj as Map<*, *>).values.any { it is MultipartFile }

            else -> obj is MultipartFile ||
                    obj is HttpServletRequest ||
                    obj is HttpServletResponse ||
                    obj is BindingResult
        }
    }
}
