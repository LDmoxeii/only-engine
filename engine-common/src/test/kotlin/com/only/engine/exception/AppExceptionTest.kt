package com.only.engine.exception

import com.only.engine.entity.Result
import com.only.engine.error.AuthErrors
import com.only.engine.error.CommonErrors
import com.only.engine.error.ErrorCategory
import org.junit.jupiter.api.Assertions.assertEquals
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
    fun `system exception should reject non system error code`() {
        assertThrows(IllegalArgumentException::class.java) {
            SystemException(CommonErrors.BUSINESS_ERROR)
        }
    }

    @Test
    fun `result error should copy error code metadata`() {
        val result = Result.error(AuthErrors.CAPTCHA_INVALID, "验证码错误")

        assertEquals(40240, result.code)
        assertEquals("验证码错误", result.message)
    }
}
