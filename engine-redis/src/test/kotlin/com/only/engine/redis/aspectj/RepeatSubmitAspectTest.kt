package com.only.engine.redis.aspectj

import com.fasterxml.jackson.databind.ObjectMapper
import com.only.engine.error.CommonErrors
import com.only.engine.exception.RateLimitException
import com.only.engine.misc.ServletUtils
import com.only.engine.redis.annotation.RepeatSubmit
import com.only.engine.redis.misc.RedisUtils
import com.only.engine.spi.idempotent.TokenProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import org.aspectj.lang.JoinPoint
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import jakarta.servlet.http.HttpServletRequest
import java.util.concurrent.TimeUnit

class RepeatSubmitAspectTest {

    @BeforeEach
    fun setUp() {
        mockkObject(ServletUtils)
        mockkObject(RedisUtils)
        val request = mockk<HttpServletRequest>()
        every { request.requestURI } returns "/repeat-submit"
        every { ServletUtils.getRequest() } returns request
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(ServletUtils)
        unmockkObject(RedisUtils)
    }

    @Test
    fun `duplicate submit should throw rate limit exception`() {
        val tokenProvider = mockk<TokenProvider>()
        every { tokenProvider.getToken() } returns "token-001"

        val joinPoint = mockk<JoinPoint>()
        every { joinPoint.args } returns arrayOf("same-payload")

        val repeatSubmit = mockk<RepeatSubmit>()
        every { repeatSubmit.interval } returns 5000
        every { repeatSubmit.timeUnit } returns TimeUnit.MILLISECONDS
        every { repeatSubmit.message } returns "请勿重复提交"

        every { RedisUtils.setObjectIfAbsent(any(), any<String>(), any()) } returnsMany listOf(true, false)

        val aspect = RepeatSubmitAspect(tokenProvider, ObjectMapper())

        aspect.doBefore(joinPoint, repeatSubmit)

        val exception = assertThrows(RateLimitException::class.java) {
            aspect.doBefore(joinPoint, repeatSubmit)
        }
        assertEquals(CommonErrors.REQUEST_RATE_LIMITED.code, exception.errorCode.code)
        assertEquals("请勿重复提交", exception.message)
    }
}
