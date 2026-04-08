package com.only.engine.exception

import com.only.engine.constants.HeaderConstants
import com.only.engine.entity.Result
import com.only.engine.error.AuthErrors
import com.only.engine.error.CommonErrors
import com.only.engine.error.ErrorCategory
import com.only.engine.error.SystemErrorCode
import com.only.engine.misc.ThreadLocalUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class AppExceptionTest {

    @Test
    fun `business exception should expose code category and custom message`() {
        val ex = BusinessException(AuthErrors.CAPTCHA_INVALID, "验证码错误")

        assertEquals(40240, ex.errorCode.code)
        assertEquals(ErrorCategory.BUSINESS, ex.errorCode.category)
        assertEquals("验证码错误", ex.message)
    }

    @Test
    fun `system exception should reject invalid system error category`() {
        val invalidSystemErrorCode = object : SystemErrorCode {
            override val code: Int = 50001
            override val name: String = "INVALID_SYSTEM_ERROR"
            override val message: String = "invalid system error"
            override val category: ErrorCategory = ErrorCategory.BUSINESS
        }

        assertThrows(IllegalArgumentException::class.java) {
            SystemException(invalidSystemErrorCode)
        }
    }

    @Test
    fun `result error should copy error code metadata`() {
        val result = Result.error(AuthErrors.CAPTCHA_INVALID, "验证码错误")

        assertEquals(40240, result.code)
        assertEquals("验证码错误", result.message)
    }

    @Test
    fun `result error should use default message and keep request metadata null by default`() {
        try {
            ThreadLocalUtils.setBizTrackCode("biz-track-001")

            val result = Result.error(AuthErrors.CAPTCHA_INVALID)

            assertEquals(40240, result.code)
            assertEquals("验证码错误", result.message)
            assertNull(result.requestId)
            assertNull(result.path)
        } finally {
            ThreadLocalUtils.clear()
        }
    }

    @Test
    fun `result should allow explicit request metadata`() {
        val result = Result<Unit>(
            code = CommonErrors.PARAM_INVALID.code,
            message = CommonErrors.PARAM_INVALID.message,
            requestId = "req-001",
            path = "/api/test",
        )

        assertEquals("req-001", result.requestId)
        assertEquals("/api/test", result.path)
    }

    @Test
    fun `thread local nullable getters should return null when value is absent`() {
        try {
            ThreadLocalUtils.clear()

            assertNull(ThreadLocalUtils.getOrNull(HeaderConstants.X_ONLY_BIZ_TRACK_CODE))
            assertNull(ThreadLocalUtils.getBizTrackCodeOrNull())
        } finally {
            ThreadLocalUtils.clear()
        }
    }
}
