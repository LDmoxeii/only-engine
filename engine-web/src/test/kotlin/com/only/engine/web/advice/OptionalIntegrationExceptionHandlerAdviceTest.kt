package com.only.engine.web.advice

import cn.dev33.satoken.exception.NotLoginException
import cn.dev33.satoken.exception.NotPermissionException
import com.baomidou.lock.exception.LockFailureException
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

class OptionalIntegrationExceptionHandlerAdviceTest {

    @Test
    fun `sa token handler should return hybrid protocol responses`() {
        val mockMvc = MockMvcBuilders
            .standaloneSetup(OptionalIntegrationController())
            .setControllerAdvice(SaTokenExceptionHandlerAdvice())
            .build()

        mockMvc.perform(get("/auth/not-login").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.code").value(40100))
            .andExpect(jsonPath("$.message").value("未登录"))
            .andExpect(jsonPath("$.path").value("/auth/not-login"))

        mockMvc.perform(get("/auth/forbidden").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.code").value(40300))
            .andExpect(jsonPath("$.message").value("无权限访问"))
            .andExpect(jsonPath("$.path").value("/auth/forbidden"))
    }

    @Test
    fun `redis handler should return rate limit protocol response`() {
        val mockMvc = MockMvcBuilders
            .standaloneSetup(OptionalIntegrationController())
            .setControllerAdvice(RedisExceptionHandler())
            .build()

        mockMvc.perform(get("/lock-failure").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isTooManyRequests)
            .andExpect(jsonPath("$.code").value(40501))
            .andExpect(jsonPath("$.message").value("请求过于频繁"))
            .andExpect(jsonPath("$.path").value("/lock-failure"))
    }

    @RestController
    private class OptionalIntegrationController {

        @GetMapping("/auth/not-login")
        fun notLogin(): String = throw NotLoginException("token", "value", "未登录")

        @GetMapping("/auth/forbidden")
        fun forbidden(): String = throw NotPermissionException("video:manage")

        @GetMapping("/lock-failure")
        fun lockFailure(): String = throw LockFailureException("duplicate request")
    }
}
