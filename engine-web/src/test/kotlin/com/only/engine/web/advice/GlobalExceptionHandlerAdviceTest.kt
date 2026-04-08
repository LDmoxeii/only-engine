package com.only.engine.web.advice

import com.only.engine.error.AuthErrors
import com.only.engine.error.CommonErrors
import com.only.engine.exception.AuthenticationException
import com.only.engine.exception.AuthorizationException
import com.only.engine.exception.BusinessException
import com.only.engine.exception.DependencyException
import com.only.engine.exception.RateLimitException
import com.only.engine.exception.RequestException
import com.only.engine.exception.SystemException
import com.only.engine.misc.ThreadLocalUtils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

class GlobalExceptionHandlerAdviceTest {

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(TestController())
            .setControllerAdvice(GlobalExceptionHandlerAdvice())
            .build()
    }

    @AfterEach
    fun tearDown() {
        ThreadLocalUtils.clear()
    }

    @Test
    fun `business exception should return http 200 and response metadata`() {
        mockMvc.perform(get("/business").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value(40240))
            .andExpect(jsonPath("$.message").value("验证码错误"))
            .andExpect(jsonPath("$.requestId").value("req-business"))
            .andExpect(jsonPath("$.path").value("/business"))
    }

    @Test
    fun `request exception should return http 400`() {
        mockMvc.perform(get("/request"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value(40400))
    }

    @Test
    fun `authentication exception should return http 401`() {
        mockMvc.perform(get("/authentication"))
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.code").value(40100))
    }

    @Test
    fun `authorization exception should return http 403`() {
        mockMvc.perform(get("/authorization"))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.code").value(40300))
    }

    @Test
    fun `rate limit exception should return http 429`() {
        mockMvc.perform(get("/rate-limit"))
            .andExpect(status().isTooManyRequests)
            .andExpect(jsonPath("$.code").value(40501))
    }

    @Test
    fun `system exception should return http 500`() {
        mockMvc.perform(get("/system"))
            .andExpect(status().isInternalServerError)
            .andExpect(jsonPath("$.code").value(50000))
    }

    @Test
    fun `dependency exception should return http 503`() {
        mockMvc.perform(get("/dependency"))
            .andExpect(status().isServiceUnavailable)
            .andExpect(jsonPath("$.code").value(60000))
    }

    @RestController
    private class TestController {

        @GetMapping("/business")
        fun business(): String {
            ThreadLocalUtils.setBizTrackCode("req-business")
            throw BusinessException(AuthErrors.CAPTCHA_INVALID, "验证码错误")
        }

        @GetMapping("/request")
        fun request(): String = throw RequestException(CommonErrors.PARAM_INVALID)

        @GetMapping("/authentication")
        fun authentication(): String = throw AuthenticationException(AuthErrors.LOGIN_REQUIRED)

        @GetMapping("/authorization")
        fun authorization(): String = throw AuthorizationException(AuthErrors.ACCESS_DENIED)

        @GetMapping("/rate-limit")
        fun rateLimit(): String = throw RateLimitException(CommonErrors.REQUEST_RATE_LIMITED)

        @GetMapping("/system")
        fun system(): String = throw SystemException(CommonErrors.SYSTEM_ERROR, cause = IllegalStateException("boom"))

        @GetMapping("/dependency")
        fun dependency(): String = throw DependencyException(CommonErrors.DEPENDENCY_ERROR, "OSS 服务不可用")
    }
}
