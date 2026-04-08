# Exception System Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace `KnownException / WarnException / ErrorException` with the new `ErrorCode + AppException` model, switch web APIs to the Hybrid protocol, and migrate `only-engine`, `only-danmuku`, and `only-danmuku-web-ui` in one pass.

**Architecture:** Build the new exception contract in `only-engine` first, then rewrite `engine-web` so HTTP mapping depends only on `AppException` category. After that, publish the new `only-engine` snapshot to local Maven and migrate `only-danmuku` plus the Vue interceptor against the new contract.

**Tech Stack:** Kotlin 2.x, Spring MVC, Sa-Token, Lock4j, Gradle multi-module builds, Vue 3, Axios, Element Plus

---

## File Map

- Create `engine-common/src/main/kotlin/com/only/engine/error/ErrorCategory.kt`: canonical error-category enum for protocol, logging, and alert policy.
- Create `engine-common/src/main/kotlin/com/only/engine/error/ErrorCode.kt`: shared contract implemented by every stable error-code definition.
- Create `engine-common/src/main/kotlin/com/only/engine/error/CommonErrors.kt`: generic cross-cutting request, business, rate-limit, system, and dependency codes.
- Create `engine-common/src/main/kotlin/com/only/engine/error/AuthErrors.kt`: authentication and authorization related codes, including captcha/login failures.
- Create `engine-common/src/main/kotlin/com/only/engine/exception/AppException.kt`: unified runtime base class carrying `ErrorCode`, message override, context, and cause.
- Create `engine-common/src/main/kotlin/com/only/engine/exception/BusinessException.kt`: category-checked business failure exception.
- Create `engine-common/src/main/kotlin/com/only/engine/exception/RequestException.kt`: category-checked request/validation exception.
- Create `engine-common/src/main/kotlin/com/only/engine/exception/AuthenticationException.kt`: category-checked unauthenticated exception.
- Create `engine-common/src/main/kotlin/com/only/engine/exception/AuthorizationException.kt`: category-checked forbidden exception.
- Create `engine-common/src/main/kotlin/com/only/engine/exception/RateLimitException.kt`: category-checked throttling/repeat-submit exception.
- Create `engine-common/src/main/kotlin/com/only/engine/exception/SystemException.kt`: category-checked internal failure exception.
- Create `engine-common/src/main/kotlin/com/only/engine/exception/DependencyException.kt`: category-checked downstream failure exception.
- Modify `engine-common/src/main/kotlin/com/only/engine/entity/Result.kt`: add `requestId` and `path`, add `error(ErrorCode, ...)`, and keep `20000` success handling.
- Modify `engine-common/src/main/kotlin/com/only/engine/misc/ThreadLocalUtils.kt`: add nullable lookup helpers so response metadata can read the request trace safely.
- Delete `engine-common/src/main/kotlin/com/only/engine/exception/KnownException.kt`: remove legacy exception base.
- Delete `engine-common/src/main/kotlin/com/only/engine/exception/WarnException.kt`: remove legacy warning exception.
- Delete `engine-common/src/main/kotlin/com/only/engine/exception/ErrorException.kt`: remove legacy error exception.
- Modify `engine-web/build.gradle.kts`: add test dependencies needed for standalone MVC exception tests.
- Modify `engine-web/src/main/kotlin/com/only/engine/web/advice/GlobalExceptionHandlerAdvice.kt`: rewrite all handlers around `AppException`, framework-exception translation, logging policy, and HTTP mapping.
- Create `engine-web/src/test/kotlin/com/only/engine/web/advice/GlobalExceptionHandlerAdviceTest.kt`: contract tests for every Hybrid HTTP mapping plus `requestId/path`.
- Modify `engine-redis/src/main/kotlin/com/only/engine/redis/aspectj/RepeatSubmitAspect.kt`: throw `RateLimitException` instead of a plain `KnownException`.
- Create `engine-redis/src/test/kotlin/com/only/engine/redis/aspectj/RepeatSubmitAspectTest.kt`: guard duplicate-submit behavior against regressions.
- Modify `engine-satoken/src/main/kotlin/com/only/engine/satoken/core/service/SaPermission.kt`: replace legacy exceptions with new system exceptions.
- Modify `engine-common/src/main/kotlin/com/only/engine/misc/FFprobeUtils.kt`: map illegal input to `RequestException`, execution failures to `SystemException`.
- Modify `engine-common/src/main/kotlin/com/only/engine/misc/FFmpegUtils.kt`: map illegal input to `RequestException`, execution failures to `SystemException`.
- Modify `engine-oss/src/main/kotlin/com/only/engine/oss/factory/OssFactory.kt`: map missing configuration to `DependencyException` or `SystemException` instead of `KnownException`.
- Modify `engine-oss/src/main/kotlin/com/only/engine/oss/core/OssClient.kt`: replace ad-hoc upload/download `KnownException` with `DependencyException` or `RequestException`.
- Modify `engine-oss/src/main/kotlin/com/only/engine/oss/enums/AccessPolicyType.kt`: map enum parse failures to `RequestException`.
- Modify `engine-common/src/main/kotlin/com/only/engine/enums/UserType.kt`: map enum parse failures to `RequestException`.
- Create `only-danmuku-domain/src/main/kotlin/edu/only4/danmuku/domain/shared/error/DanmukuBusinessErrors.kt`: danmuku-domain stable business-code catalog for repeated entity and permission failures.
- Create `only-danmuku-domain/src/main/kotlin/edu/only4/danmuku/domain/shared/error/DanmukuAuthErrors.kt`: danmuku-specific auth/business codes such as captcha failure.
- Modify `only-danmuku-adapter/src/main/kotlin/edu/only4/danmuku/adapter/portal/api/web/AccountController.kt`: remove `CaptchaInvalidException` usage and throw new business exception directly.
- Modify `only-danmuku-adapter/src/main/kotlin/edu/only4/danmuku/adapter/portal/api/admin/AdminAccountController.kt`: same login captcha migration as web account login.
- Delete `only-danmuku-adapter/src/main/kotlin/edu/only4/danmuku/adapter/portal/api/_share/exception/CaptchaInvalidException.kt`: remove temporary compatibility exception.
- Modify `only-danmuku-application/src/main/kotlin/**`: bulk-replace `KnownException` calls with `BusinessException`, `RequestException`, or `SystemException`.
- Modify `only-danmuku-domain/src/main/kotlin/**`: bulk-replace `KnownException` calls, especially aggregate guards.
- Modify `only-danmuku-adapter/src/main/kotlin/edu/only4/danmuku/adapter/application/distributed/clients/**`: manual-review and migrate filesystem/OSS/ffmpeg paths to `RequestException`, `SystemException`, or `DependencyException`.
- Modify `only-danmuku-adapter/src/main/kotlin/edu/only4/danmuku/adapter/application/queries/**`: replace request-validation and business failures with the new exception model.
- Create `only-danmuku-start/src/test/kotlin/edu/only4/danmuku/adapter/portal/api/HybridExceptionProtocolIntegrationTest.kt`: consumer-side integration test proving the published `only-engine` artifact exposes the new contract correctly.
- Modify `only-danmuku-start/src/test/kotlin/edu/only4/danmuku/adapter/portal/api/LoginCaptchaErrorResponseTest.kt`: keep the login captcha regression aligned with the final `BusinessException + code 40240` contract.
- Modify `only-danmuku-web-ui/src/utils/Request.ts`: split business failures from protocol failures and handle `401/403/429/5xx` explicitly.

### Task 1: Introduce the New Error Contract in `engine-common`

**Files:**
- Create: `engine-common/src/main/kotlin/com/only/engine/error/ErrorCategory.kt`
- Create: `engine-common/src/main/kotlin/com/only/engine/error/ErrorCode.kt`
- Create: `engine-common/src/main/kotlin/com/only/engine/error/CommonErrors.kt`
- Create: `engine-common/src/main/kotlin/com/only/engine/error/AuthErrors.kt`
- Create: `engine-common/src/main/kotlin/com/only/engine/exception/AppException.kt`
- Create: `engine-common/src/main/kotlin/com/only/engine/exception/BusinessException.kt`
- Create: `engine-common/src/main/kotlin/com/only/engine/exception/RequestException.kt`
- Create: `engine-common/src/main/kotlin/com/only/engine/exception/AuthenticationException.kt`
- Create: `engine-common/src/main/kotlin/com/only/engine/exception/AuthorizationException.kt`
- Create: `engine-common/src/main/kotlin/com/only/engine/exception/RateLimitException.kt`
- Create: `engine-common/src/main/kotlin/com/only/engine/exception/SystemException.kt`
- Create: `engine-common/src/main/kotlin/com/only/engine/exception/DependencyException.kt`
- Modify: `engine-common/src/main/kotlin/com/only/engine/entity/Result.kt`
- Modify: `engine-common/src/main/kotlin/com/only/engine/misc/ThreadLocalUtils.kt`
- Test: `engine-common/src/test/kotlin/com/only/engine/exception/AppExceptionTest.kt`

- [ ] **Step 1: Write the failing `engine-common` contract test**

```kotlin
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
```

- [ ] **Step 2: Run the test and confirm it fails on missing classes/APIs**

Run: `.\gradlew.bat :engine-common:test --tests "com.only.engine.exception.AppExceptionTest" --no-daemon`  
Expected: FAIL with unresolved references such as `ErrorCategory`, `ErrorCode`, `BusinessException`, or `Result.error(ErrorCode, ...)`.

- [ ] **Step 3: Add the new error model and response metadata**

```kotlin
// engine-common/src/main/kotlin/com/only/engine/error/ErrorCategory.kt
package com.only.engine.error

enum class ErrorCategory {
    BUSINESS,
    REQUEST,
    AUTHENTICATION,
    AUTHORIZATION,
    RATE_LIMIT,
    SYSTEM,
    DEPENDENCY,
}

// engine-common/src/main/kotlin/com/only/engine/error/ErrorCode.kt
package com.only.engine.error

interface ErrorCode {
    val code: Int
    val name: String
    val message: String
    val category: ErrorCategory
}

// engine-common/src/main/kotlin/com/only/engine/error/CommonErrors.kt
package com.only.engine.error

enum class CommonErrors(
    override val code: Int,
    override val message: String,
    override val category: ErrorCategory,
) : ErrorCode {
    BUSINESS_ERROR(400500, "业务异常", ErrorCategory.BUSINESS),
    PARAM_INVALID(40400, "请求参数异常", ErrorCategory.REQUEST),
    PARAM_REQUIRED(40401, "参数不能为空", ErrorCategory.REQUEST),
    REQUEST_RATE_LIMITED(40501, "请求过于频繁", ErrorCategory.RATE_LIMIT),
    SYSTEM_ERROR(50000, "系统异常", ErrorCategory.SYSTEM),
    DEPENDENCY_ERROR(60000, "第三方服务异常", ErrorCategory.DEPENDENCY),
}

// engine-common/src/main/kotlin/com/only/engine/error/AuthErrors.kt
package com.only.engine.error

enum class AuthErrors(
    override val code: Int,
    override val message: String,
    override val category: ErrorCategory,
) : ErrorCode {
    LOGIN_REQUIRED(40100, "未登录", ErrorCategory.AUTHENTICATION),
    ACCESS_DENIED(40300, "无权限访问", ErrorCategory.AUTHORIZATION),
    CAPTCHA_INVALID(40240, "验证码错误", ErrorCategory.BUSINESS),
}

// engine-common/src/main/kotlin/com/only/engine/exception/AppException.kt
package com.only.engine.exception

import com.only.engine.error.ErrorCategory
import com.only.engine.error.ErrorCode

abstract class AppException(
    val errorCode: ErrorCode,
    override val message: String = errorCode.message,
    val context: Map<String, Any?> = emptyMap(),
    cause: Throwable? = null,
) : RuntimeException(message, cause) {

    protected abstract val expectedCategory: ErrorCategory

    init {
        require(errorCode.category == expectedCategory) {
            "ErrorCode category ${errorCode.category} cannot be used by ${this::class.simpleName}"
        }
    }
}

// engine-common/src/main/kotlin/com/only/engine/exception/BusinessException.kt
package com.only.engine.exception

import com.only.engine.error.ErrorCategory
import com.only.engine.error.ErrorCode

class BusinessException(
    errorCode: ErrorCode,
    message: String = errorCode.message,
    context: Map<String, Any?> = emptyMap(),
    cause: Throwable? = null,
) : AppException(errorCode, message, context, cause) {
    override val expectedCategory: ErrorCategory = ErrorCategory.BUSINESS
}

// engine-common/src/main/kotlin/com/only/engine/exception/RequestException.kt
package com.only.engine.exception

import com.only.engine.error.ErrorCategory
import com.only.engine.error.ErrorCode

class RequestException(
    errorCode: ErrorCode,
    message: String = errorCode.message,
    context: Map<String, Any?> = emptyMap(),
    cause: Throwable? = null,
) : AppException(errorCode, message, context, cause) {
    override val expectedCategory: ErrorCategory = ErrorCategory.REQUEST
}

// engine-common/src/main/kotlin/com/only/engine/exception/AuthenticationException.kt
package com.only.engine.exception

import com.only.engine.error.ErrorCategory
import com.only.engine.error.ErrorCode

class AuthenticationException(
    errorCode: ErrorCode,
    message: String = errorCode.message,
    context: Map<String, Any?> = emptyMap(),
    cause: Throwable? = null,
) : AppException(errorCode, message, context, cause) {
    override val expectedCategory: ErrorCategory = ErrorCategory.AUTHENTICATION
}

// engine-common/src/main/kotlin/com/only/engine/exception/AuthorizationException.kt
package com.only.engine.exception

import com.only.engine.error.ErrorCategory
import com.only.engine.error.ErrorCode

class AuthorizationException(
    errorCode: ErrorCode,
    message: String = errorCode.message,
    context: Map<String, Any?> = emptyMap(),
    cause: Throwable? = null,
) : AppException(errorCode, message, context, cause) {
    override val expectedCategory: ErrorCategory = ErrorCategory.AUTHORIZATION
}

// engine-common/src/main/kotlin/com/only/engine/exception/RateLimitException.kt
package com.only.engine.exception

import com.only.engine.error.ErrorCategory
import com.only.engine.error.ErrorCode

class RateLimitException(
    errorCode: ErrorCode,
    message: String = errorCode.message,
    context: Map<String, Any?> = emptyMap(),
    cause: Throwable? = null,
) : AppException(errorCode, message, context, cause) {
    override val expectedCategory: ErrorCategory = ErrorCategory.RATE_LIMIT
}

// engine-common/src/main/kotlin/com/only/engine/exception/SystemException.kt
package com.only.engine.exception

import com.only.engine.error.ErrorCategory
import com.only.engine.error.ErrorCode

class SystemException(
    errorCode: ErrorCode,
    message: String = errorCode.message,
    context: Map<String, Any?> = emptyMap(),
    cause: Throwable? = null,
) : AppException(errorCode, message, context, cause) {
    override val expectedCategory: ErrorCategory = ErrorCategory.SYSTEM
}

// engine-common/src/main/kotlin/com/only/engine/exception/DependencyException.kt
package com.only.engine.exception

import com.only.engine.error.ErrorCategory
import com.only.engine.error.ErrorCode

class DependencyException(
    errorCode: ErrorCode,
    message: String = errorCode.message,
    context: Map<String, Any?> = emptyMap(),
    cause: Throwable? = null,
) : AppException(errorCode, message, context, cause) {
    override val expectedCategory: ErrorCategory = ErrorCategory.DEPENDENCY
}

// engine-common/src/main/kotlin/com/only/engine/misc/ThreadLocalUtils.kt
fun getOrNull(key: String): String? =
    CURRENT_CONTEXT.get()?.get(key)

fun getBizTrackCodeOrNull(): String? =
    getOrNull(HeaderConstants.X_ONLY_BIZ_TRACK_CODE)

// engine-common/src/main/kotlin/com/only/engine/entity/Result.kt
data class Result<T>(
    override val code: Int = ResultCode.SUCCESS.code,
    override var message: String = ResultCode.SUCCESS.message,
    val data: T? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val requestId: String? = ThreadLocalUtils.getBizTrackCodeOrNull(),
    val path: String? = ServletUtils.getRequest()?.requestURI,
) : BaseCode {
    companion object {
        @JvmStatic
        fun <T> ok(data: T? = null): Result<T> =
            Result(ResultCode.SUCCESS.code, ResultCode.SUCCESS.message, data)

        @JvmStatic
        fun error(errorCode: ErrorCode, message: String = errorCode.message): Result<Unit> =
            Result(code = errorCode.code, message = message, data = null)
    }
}
```

- [ ] **Step 4: Run the `engine-common` test until it passes**

Run: `.\gradlew.bat :engine-common:test --tests "com.only.engine.exception.AppExceptionTest" --no-daemon`  
Expected: PASS.

- [ ] **Step 5: Commit the foundational exception contract**

```bash
git add engine-common/src/main/kotlin/com/only/engine/error engine-common/src/main/kotlin/com/only/engine/exception engine-common/src/main/kotlin/com/only/engine/entity/Result.kt engine-common/src/main/kotlin/com/only/engine/misc/ThreadLocalUtils.kt engine-common/src/test/kotlin/com/only/engine/exception/AppExceptionTest.kt
git commit -m "feat(exception): add error-code exception model"
```

### Task 2: Rewrite `engine-web` Around `AppException`

**Files:**
- Modify: `engine-web/build.gradle.kts`
- Modify: `engine-web/src/main/kotlin/com/only/engine/web/advice/GlobalExceptionHandlerAdvice.kt`
- Test: `engine-web/src/test/kotlin/com/only/engine/web/advice/GlobalExceptionHandlerAdviceTest.kt`

- [ ] **Step 1: Write the failing MVC contract test for the Hybrid protocol**

```kotlin
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
```

- [ ] **Step 2: Run the MVC contract test and confirm the current advice fails**

Run: `.\gradlew.bat :engine-web:test --tests "com.only.engine.web.advice.GlobalExceptionHandlerAdviceTest" --no-daemon`  
Expected: FAIL because the current `GlobalExceptionHandlerAdvice` still handles `KnownException` and does not know `AppException`.

- [ ] **Step 3: Rewrite `GlobalExceptionHandlerAdvice` and add the missing test dependencies**

```kotlin
// engine-web/build.gradle.kts
dependencies {
    testImplementation(libs.spring.boot.starter.test) {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation(libs.sa.token.core)
    testImplementation(libs.lock4j.core)
}

// engine-web/src/main/kotlin/com/only/engine/web/advice/GlobalExceptionHandlerAdvice.kt
package com.only.engine.web.advice

import cn.dev33.satoken.exception.NotLoginException
import cn.dev33.satoken.exception.NotPermissionException
import cn.dev33.satoken.exception.NotRoleException
import com.baomidou.lock.exception.LockFailureException
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonProcessingException
import com.only.engine.entity.Result
import com.only.engine.error.AuthErrors
import com.only.engine.error.CommonErrors
import com.only.engine.error.ErrorCategory
import com.only.engine.exception.AppException
import com.only.engine.exception.AuthenticationException
import com.only.engine.exception.AuthorizationException
import com.only.engine.exception.DependencyException
import com.only.engine.exception.RateLimitException
import com.only.engine.exception.RequestException
import com.only.engine.exception.SystemException
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.ConstraintViolationException
import org.apache.catalina.connector.ClientAbortException
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingPathVariableException
import org.springframework.web.bind.MissingRequestHeaderException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.NoHandlerFoundException
import java.io.IOException

@Order(3)
@AutoConfiguration
@RestControllerAdvice
@ConditionalOnProperty(prefix = "only.engine.web.exception-handler", name = ["enable"], havingValue = "true")
class GlobalExceptionHandlerAdvice {

    companion object {
        private val log = LoggerFactory.getLogger(GlobalExceptionHandlerAdvice::class.java)
    }

    @ExceptionHandler(AppException::class)
    fun handleAppException(
        ex: AppException,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Result<Unit> {
        val status = resolveStatus(ex)
        response.status = status.value()
        logByCategory(ex.errorCode.category, request, ex.message, ex)
        return Result.error(ex.errorCode, ex.message)
    }

    @ExceptionHandler(
        MissingServletRequestParameterException::class,
        MissingRequestHeaderException::class,
        MissingPathVariableException::class,
        MethodArgumentTypeMismatchException::class,
        MethodArgumentNotValidException::class,
        BindException::class,
        ConstraintViolationException::class,
        HttpMessageNotReadableException::class,
        JsonProcessingException::class,
        JsonParseException::class,
        IllegalArgumentException::class,
        HttpRequestMethodNotSupportedException::class,
        NoHandlerFoundException::class,
    )
    fun handleRequestFailures(
        ex: Exception,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Result<Unit> = handleAppException(
        RequestException(CommonErrors.PARAM_INVALID, resolveRequestMessage(ex)),
        request,
        response,
    )

    @ExceptionHandler(NotLoginException::class)
    fun handleNotLogin(
        ex: NotLoginException,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Result<Unit> = handleAppException(AuthenticationException(AuthErrors.LOGIN_REQUIRED), request, response)

    @ExceptionHandler(NotPermissionException::class, NotRoleException::class)
    fun handleForbidden(
        ex: RuntimeException,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Result<Unit> = handleAppException(AuthorizationException(AuthErrors.ACCESS_DENIED), request, response)

    @ExceptionHandler(LockFailureException::class)
    fun handleRateLimit(
        ex: LockFailureException,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Result<Unit> = handleAppException(
        RateLimitException(CommonErrors.REQUEST_RATE_LIMITED, ex.message ?: CommonErrors.REQUEST_RATE_LIMITED.message),
        request,
        response,
    )

    @ExceptionHandler(HttpClientErrorException::class, HttpServerErrorException::class)
    fun handleDependencyHttpError(
        ex: Exception,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Result<Unit> = handleAppException(
        DependencyException(CommonErrors.DEPENDENCY_ERROR, CommonErrors.DEPENDENCY_ERROR.message, cause = ex),
        request,
        response,
    )

    @ExceptionHandler(ClientAbortException::class)
    fun handleClientAbort(ex: ClientAbortException, request: HttpServletRequest): Result<Unit>? = null

    @ExceptionHandler(ServletException::class, IOException::class, Throwable::class)
    fun handleThrowable(
        ex: Throwable,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Result<Unit> = handleAppException(
        SystemException(CommonErrors.SYSTEM_ERROR, resolveSafeSystemMessage(ex), cause = ex),
        request,
        response,
    )

    private fun resolveStatus(ex: AppException): HttpStatus =
        when (ex.errorCode.category) {
            ErrorCategory.BUSINESS -> HttpStatus.OK
            ErrorCategory.REQUEST -> HttpStatus.BAD_REQUEST
            ErrorCategory.AUTHENTICATION -> HttpStatus.UNAUTHORIZED
            ErrorCategory.AUTHORIZATION -> HttpStatus.FORBIDDEN
            ErrorCategory.RATE_LIMIT -> HttpStatus.TOO_MANY_REQUESTS
            ErrorCategory.SYSTEM -> HttpStatus.INTERNAL_SERVER_ERROR
            ErrorCategory.DEPENDENCY -> HttpStatus.SERVICE_UNAVAILABLE
        }

    private fun logByCategory(category: ErrorCategory, request: HttpServletRequest, message: String, ex: Throwable) {
        when (category) {
            ErrorCategory.BUSINESS, ErrorCategory.AUTHENTICATION ->
                log.info("Path: [{}], Exception message: [{}], Exception: [{}]", request.requestURI, message, ex::class.simpleName)
            ErrorCategory.REQUEST, ErrorCategory.AUTHORIZATION, ErrorCategory.RATE_LIMIT ->
                log.warn("Path: [{}], Exception message: [{}], Exception: [{}]", request.requestURI, message, ex::class.simpleName)
            ErrorCategory.SYSTEM, ErrorCategory.DEPENDENCY ->
                log.error("Path: [{}], Exception message: [{}], Exception: [{}]", request.requestURI, message, ex::class.simpleName, ex)
        }
    }

    private fun resolveRequestMessage(ex: Exception): String =
        ex.message?.takeIf { it.isNotBlank() } ?: CommonErrors.PARAM_INVALID.message

    private fun resolveSafeSystemMessage(ex: Throwable): String =
        ex.message?.takeIf { it.isNotBlank() } ?: CommonErrors.SYSTEM_ERROR.message
}
```

- [ ] **Step 4: Run the `engine-web` contract test until it passes**

Run: `.\gradlew.bat :engine-web:test --tests "com.only.engine.web.advice.GlobalExceptionHandlerAdviceTest" --no-daemon`  
Expected: PASS.

- [ ] **Step 5: Commit the `engine-web` protocol rewrite**

```bash
git add engine-web/build.gradle.kts engine-web/src/main/kotlin/com/only/engine/web/advice/GlobalExceptionHandlerAdvice.kt engine-web/src/test/kotlin/com/only/engine/web/advice/GlobalExceptionHandlerAdviceTest.kt
git commit -m "feat(exception): rewrite web exception mapping"
```

### Task 3: Migrate `only-engine` Internal Callers and Delete Legacy Exceptions

**Files:**
- Modify: `engine-redis/src/main/kotlin/com/only/engine/redis/aspectj/RepeatSubmitAspect.kt`
- Modify: `engine-satoken/src/main/kotlin/com/only/engine/satoken/core/service/SaPermission.kt`
- Modify: `engine-common/src/main/kotlin/com/only/engine/misc/FFprobeUtils.kt`
- Modify: `engine-common/src/main/kotlin/com/only/engine/misc/FFmpegUtils.kt`
- Modify: `engine-oss/src/main/kotlin/com/only/engine/oss/factory/OssFactory.kt`
- Modify: `engine-oss/src/main/kotlin/com/only/engine/oss/core/OssClient.kt`
- Modify: `engine-oss/src/main/kotlin/com/only/engine/oss/enums/AccessPolicyType.kt`
- Modify: `engine-common/src/main/kotlin/com/only/engine/enums/UserType.kt`
- Delete: `engine-common/src/main/kotlin/com/only/engine/exception/KnownException.kt`
- Delete: `engine-common/src/main/kotlin/com/only/engine/exception/WarnException.kt`
- Delete: `engine-common/src/main/kotlin/com/only/engine/exception/ErrorException.kt`
- Test: `engine-redis/src/test/kotlin/com/only/engine/redis/aspectj/RepeatSubmitAspectTest.kt`

- [ ] **Step 1: Write the failing duplicate-submit regression test**

```kotlin
package com.only.engine.redis.aspectj

import com.fasterxml.jackson.databind.ObjectMapper
import com.only.engine.error.CommonErrors
import com.only.engine.exception.RateLimitException
import com.only.engine.redis.annotation.RepeatSubmit
import com.only.engine.redis.misc.RedisUtils
import com.only.engine.misc.ServletUtils
import com.only.engine.spi.idempotent.TokenProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.aspectj.lang.JoinPoint
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import java.lang.annotation.Annotation
import java.util.concurrent.TimeUnit

class RepeatSubmitAspectTest {
    @BeforeEach
    fun setUp() {
        mockkObject(RedisUtils)
        mockkObject(ServletUtils)
        every { ServletUtils.getRequest() } returns MockHttpServletRequest("POST", "/repeat")
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `duplicate submit should throw rate limit exception`() {
        every { RedisUtils.setObjectIfAbsent(any(), any<String>(), any()) } returns false
        val joinPoint = mockk<JoinPoint>()
        every { joinPoint.args } returns arrayOf(mapOf("name" to "demo"))

        val aspect = RepeatSubmitAspect(
            tokenProvider = object : TokenProvider {
                override fun getToken(): String = "token"
            },
            objectMapper = ObjectMapper(),
        )

        val ex = assertThrows(RateLimitException::class.java) {
            aspect.doBefore(joinPoint, repeatSubmit("请勿重复提交"))
        }

        assertEquals(CommonErrors.REQUEST_RATE_LIMITED.code, ex.errorCode.code)
        assertEquals("请勿重复提交", ex.message)
    }

    private fun repeatSubmit(message: String): RepeatSubmit =
        object : RepeatSubmit {
            override fun message(): String = message
            override fun interval(): Int = 1
            override fun timeUnit(): TimeUnit = TimeUnit.SECONDS
            override fun annotationType(): Class<out Annotation> = RepeatSubmit::class.java
        }
}
```

- [ ] **Step 2: Run the internal regression tests and confirm they fail on legacy exceptions**

Run: `.\gradlew.bat :engine-redis:test --tests "com.only.engine.redis.aspectj.RepeatSubmitAspectTest" :engine-common:test :engine-web:test --no-daemon`  
Expected: FAIL because `RepeatSubmitAspect` still throws `KnownException`, and legacy imports remain in internal modules.

- [ ] **Step 3: Replace legacy exception calls module by module and then delete the old classes**

```kotlin
// engine-redis/src/main/kotlin/com/only/engine/redis/aspectj/RepeatSubmitAspect.kt
throw RateLimitException(CommonErrors.REQUEST_RATE_LIMITED, message)

// engine-satoken/src/main/kotlin/com/only/engine/satoken/core/service/SaPermission.kt
val service = permissionService.ifAvailable
    ?: throw SystemException(CommonErrors.SYSTEM_ERROR, "PermissionService 实例不存在")
return service

// engine-common/src/main/kotlin/com/only/engine/misc/FFprobeUtils.kt
throw RequestException(CommonErrors.PARAM_REQUIRED, "参数 'inputPath' 不能为空")
throw SystemException(CommonErrors.SYSTEM_ERROR, "视频文件不存在: ${inputFile.absolutePath}")
throw SystemException(CommonErrors.SYSTEM_ERROR, cause = e)

// engine-common/src/main/kotlin/com/only/engine/misc/FFmpegUtils.kt
throw RequestException(CommonErrors.PARAM_REQUIRED, "参数 'videoFilePath' 不能为空")
throw SystemException(CommonErrors.SYSTEM_ERROR, "FFmpeg 命令执行失败 (exitCode=$exitCode): ${stderr.trim()}")

// engine-oss/src/main/kotlin/com/only/engine/oss/factory/OssFactory.kt
private fun fallbackInstance(configKey: String): OssClient {
    val properties = defaultProperties
        ?: throw DependencyException(CommonErrors.DEPENDENCY_ERROR, "文件存储服务类型无法找到")
    return instanceFromProperties(configKey, properties)
}

// engine-oss/src/main/kotlin/com/only/engine/oss/core/OssClient.kt
throw DependencyException(CommonErrors.DEPENDENCY_ERROR, "上传文件失败，错误信息: ${e.message ?: "未知错误"}", cause = e)
throw RequestException(CommonErrors.PARAM_REQUIRED, "参数 'prefix' 不能为空")

// engine-oss/src/main/kotlin/com/only/engine/oss/enums/AccessPolicyType.kt
return valueOfOrNull(value)
    ?: throw RequestException(CommonErrors.PARAM_INVALID, "枚举类型 AccessPolicyType 不存在的值: $value")

// engine-common/src/main/kotlin/com/only/engine/enums/UserType.kt
return enumMap[value]
    ?: throw RequestException(CommonErrors.PARAM_INVALID, "枚举类型 UserType 不存在的值: $value")

// delete the legacy classes after all imports are removed
// engine-common/src/main/kotlin/com/only/engine/exception/KnownException.kt
// engine-common/src/main/kotlin/com/only/engine/exception/WarnException.kt
// engine-common/src/main/kotlin/com/only/engine/exception/ErrorException.kt
```

- [ ] **Step 4: Run the internal module tests again and ensure no legacy references remain**

Run: `.\gradlew.bat :engine-common:test :engine-redis:test :engine-web:test --no-daemon`  
Expected: PASS.  

Run: `rg -n "KnownException|WarnException|ErrorException" engine-common/src/main/kotlin engine-redis/src/main/kotlin engine-satoken/src/main/kotlin engine-oss/src/main/kotlin engine-web/src/main/kotlin`  
Expected: no matches.

- [ ] **Step 5: Commit the internal `only-engine` migration**

```bash
git add engine-common/src/main/kotlin engine-redis/src/main/kotlin engine-redis/src/test/kotlin engine-satoken/src/main/kotlin engine-oss/src/main/kotlin engine-web/src/main/kotlin
git commit -m "refactor(exception): migrate engine internals"
```

### Task 4: Publish the New `only-engine` Snapshot and Add Consumer-Side Contract Tests

**Files:**
- Create: `only-danmuku-start/src/test/kotlin/edu/only4/danmuku/adapter/portal/api/HybridExceptionProtocolIntegrationTest.kt`
- Modify: `only-danmuku-start/src/test/kotlin/edu/only4/danmuku/adapter/portal/api/LoginCaptchaErrorResponseTest.kt`

- [ ] **Step 1: Write the failing consumer-side integration test**

```kotlin
package edu.only4.danmuku.adapter.portal.api

import com.only.engine.error.AuthErrors
import com.only.engine.error.CommonErrors
import com.only.engine.exception.AuthenticationException
import com.only.engine.exception.AuthorizationException
import com.only.engine.exception.BusinessException
import com.only.engine.exception.DependencyException
import com.only.engine.exception.RateLimitException
import com.only.engine.exception.SystemException
import com.only.engine.web.advice.GlobalExceptionHandlerAdvice
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

class HybridExceptionProtocolIntegrationTest {

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(TestController())
            .setControllerAdvice(GlobalExceptionHandlerAdvice())
            .build()
    }

    @Test
    fun `consumer repo should see business failure as http 200`() {
        mockMvc.perform(get("/hybrid/business"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value(40240))
    }

    @Test
    fun `consumer repo should see authentication failure as http 401`() {
        mockMvc.perform(get("/hybrid/authentication"))
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.code").value(40100))
    }

    @Test
    fun `consumer repo should see authorization failure as http 403`() {
        mockMvc.perform(get("/hybrid/authorization"))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.code").value(40300))
    }

    @Test
    fun `consumer repo should see rate limit and server failures as non 200`() {
        mockMvc.perform(get("/hybrid/rate-limit"))
            .andExpect(status().isTooManyRequests)
            .andExpect(jsonPath("$.code").value(40501))

        mockMvc.perform(get("/hybrid/system"))
            .andExpect(status().isInternalServerError)
            .andExpect(jsonPath("$.code").value(50000))

        mockMvc.perform(get("/hybrid/dependency"))
            .andExpect(status().isServiceUnavailable)
            .andExpect(jsonPath("$.code").value(60000))
    }

    @RestController
    private class TestController {
        @GetMapping("/hybrid/business")
        fun business(): String = throw BusinessException(AuthErrors.CAPTCHA_INVALID)

        @GetMapping("/hybrid/authentication")
        fun authentication(): String = throw AuthenticationException(AuthErrors.LOGIN_REQUIRED)

        @GetMapping("/hybrid/authorization")
        fun authorization(): String = throw AuthorizationException(AuthErrors.ACCESS_DENIED)

        @GetMapping("/hybrid/rate-limit")
        fun rateLimit(): String = throw RateLimitException(CommonErrors.REQUEST_RATE_LIMITED)

        @GetMapping("/hybrid/system")
        fun system(): String = throw SystemException(CommonErrors.SYSTEM_ERROR)

        @GetMapping("/hybrid/dependency")
        fun dependency(): String = throw DependencyException(CommonErrors.DEPENDENCY_ERROR)
    }
}
```

- [ ] **Step 2: Run the `only-danmuku-start` integration test before publishing and confirm it fails**

Run: `.\gradlew.bat :only-danmuku-start:test --tests "edu.only4.danmuku.adapter.portal.api.HybridExceptionProtocolIntegrationTest" --no-daemon`  
Expected: FAIL because the current `only-engine` dependency in Maven does not yet expose the new exception classes.

- [ ] **Step 3: Publish the rebuilt `only-engine` modules to local Maven and refresh `only-danmuku`**

```bash
cd c:\Users\LD_moxeii\Documents\code\only-workspace\only-engine
.\gradlew.bat publishToMavenLocal --no-daemon

cd c:\Users\LD_moxeii\Documents\code\only-workspace\only-danmuku
.\gradlew.bat :only-danmuku-start:test --tests "edu.only4.danmuku.adapter.portal.api.HybridExceptionProtocolIntegrationTest" --refresh-dependencies --no-daemon
```

- [ ] **Step 4: Keep the captcha login regression green in the consumer repo**

Run: `.\gradlew.bat :only-danmuku-start:test --tests "edu.only4.danmuku.adapter.portal.api.LoginCaptchaErrorResponseTest" --refresh-dependencies --no-daemon`  
Expected: PASS with `HTTP 200`, `code=40240`, `message=验证码错误`.

- [ ] **Step 5: Commit the consumer-side contract tests**

```bash
git add only-danmuku-start/src/test/kotlin/edu/only4/danmuku/adapter/portal/api/HybridExceptionProtocolIntegrationTest.kt only-danmuku-start/src/test/kotlin/edu/only4/danmuku/adapter/portal/api/LoginCaptchaErrorResponseTest.kt
git commit -m "test(exception): add hybrid protocol integration coverage"
```

### Task 5: Bulk-Migrate `only-danmuku` Backend Exceptions

**Files:**
- Create: `only-danmuku-domain/src/main/kotlin/edu/only4/danmuku/domain/shared/error/DanmukuBusinessErrors.kt`
- Create: `only-danmuku-domain/src/main/kotlin/edu/only4/danmuku/domain/shared/error/DanmukuAuthErrors.kt`
- Modify: `only-danmuku-adapter/src/main/kotlin/edu/only4/danmuku/adapter/portal/api/web/AccountController.kt`
- Modify: `only-danmuku-adapter/src/main/kotlin/edu/only4/danmuku/adapter/portal/api/admin/AdminAccountController.kt`
- Delete: `only-danmuku-adapter/src/main/kotlin/edu/only4/danmuku/adapter/portal/api/_share/exception/CaptchaInvalidException.kt`
- Modify: `only-danmuku-application/src/main/kotlin/**`
- Modify: `only-danmuku-domain/src/main/kotlin/**`
- Modify: `only-danmuku-adapter/src/main/kotlin/edu/only4/danmuku/adapter/application/distributed/clients/**`
- Modify: `only-danmuku-adapter/src/main/kotlin/edu/only4/danmuku/adapter/application/queries/**`
- Test: `only-danmuku-start/src/test/kotlin/edu/only4/danmuku/adapter/portal/api/LoginCaptchaErrorResponseTest.kt`
- Test: `only-danmuku-start/src/test/kotlin/edu/only4/danmuku/adapter/portal/api/HybridExceptionProtocolIntegrationTest.kt`

- [ ] **Step 1: Create the danmuku-local error-code catalogs that the migration will target**

```kotlin
// only-danmuku-domain/src/main/kotlin/edu/only4/danmuku/domain/shared/error/DanmukuBusinessErrors.kt
package edu.only4.danmuku.domain.shared.error

import com.only.engine.error.ErrorCategory
import com.only.engine.error.ErrorCode

enum class DanmukuBusinessErrors(
    override val code: Int,
    override val message: String,
    override val category: ErrorCategory,
) : ErrorCode {
    RESOURCE_NOT_FOUND(41000, "资源不存在", ErrorCategory.BUSINESS),
    OPERATION_FORBIDDEN(41001, "无权执行当前操作", ErrorCategory.BUSINESS),
    STATE_INVALID(41002, "状态不允许当前操作", ErrorCategory.BUSINESS),
}

// only-danmuku-domain/src/main/kotlin/edu/only4/danmuku/domain/shared/error/DanmukuAuthErrors.kt
package edu.only4.danmuku.domain.shared.error

import com.only.engine.error.ErrorCategory
import com.only.engine.error.ErrorCode

enum class DanmukuAuthErrors(
    override val code: Int,
    override val message: String,
    override val category: ErrorCategory,
) : ErrorCode {
    CAPTCHA_INVALID(40240, "验证码错误", ErrorCategory.BUSINESS),
}
```

- [ ] **Step 2: Run a full legacy-exception inventory and use it as the migration checklist**

Run:

```bash
rg -n "KnownException|WarnException|ErrorException" only-danmuku-application/src/main/kotlin only-danmuku-domain/src/main/kotlin only-danmuku-adapter/src/main/kotlin
```

Expected: the output includes business-string throws in `only-danmuku-application`, aggregate guards in `only-danmuku-domain`, and high-risk filesystem/ffmpeg/OSS callers under `only-danmuku-adapter/src/main/kotlin/edu/only4/danmuku/adapter/application/distributed/clients`.

- [ ] **Step 3: Apply the migration rules consistently and remove the temporary captcha exception**

```kotlin
// only-danmuku-adapter/src/main/kotlin/edu/only4/danmuku/adapter/portal/api/web/AccountController.kt
if (!valid.code) {
    throw BusinessException(DanmukuAuthErrors.CAPTCHA_INVALID)
}

// only-danmuku-adapter/src/main/kotlin/edu/only4/danmuku/adapter/portal/api/admin/AdminAccountController.kt
if (!valid.code) {
    throw BusinessException(DanmukuAuthErrors.CAPTCHA_INVALID)
}

// business-rule migration examples in only-danmuku-application and only-danmuku-domain
throw BusinessException(DanmukuBusinessErrors.RESOURCE_NOT_FOUND, "视频不存在")
throw BusinessException(DanmukuBusinessErrors.OPERATION_FORBIDDEN, "无权限修改该视频互动设置")
throw BusinessException(DanmukuBusinessErrors.STATE_INVALID, "系列数量已达到上限")

// request-validation migration examples
throw RequestException(CommonErrors.PARAM_REQUIRED, "参数 'quality' 不能为空")
throw RequestException(CommonErrors.PARAM_INVALID, "无效的视频ID: $trimmed")

// system/dependency migration examples in distributed clients
throw SystemException(CommonErrors.SYSTEM_ERROR, "源目录不存在: $sourceDir")
throw SystemException(CommonErrors.SYSTEM_ERROR, cause = e)
throw DependencyException(CommonErrors.DEPENDENCY_ERROR, "上传文件失败，错误信息: ${e.message ?: "未知错误"}", cause = e)

// delete the temporary compatibility class
// only-danmuku-adapter/src/main/kotlin/edu/only4/danmuku/adapter/portal/api/_share/exception/CaptchaInvalidException.kt
```

Manual-review this exact high-risk set after the bulk replacement:

```text
only-danmuku-adapter/src/main/kotlin/edu/only4/danmuku/adapter/application/distributed/clients/video_transcode/TranscodeVideoFileToAbrByPathCliHandler.kt
only-danmuku-adapter/src/main/kotlin/edu/only4/danmuku/adapter/application/distributed/clients/video_encrypt/EncryptHlsWithQualityKeysCliHandler.kt
only-danmuku-adapter/src/main/kotlin/edu/only4/danmuku/adapter/application/distributed/clients/video_encrypt/EncryptHlsVariantWithKeyCliHandler.kt
only-danmuku-adapter/src/main/kotlin/edu/only4/danmuku/adapter/application/distributed/clients/file_storage/UploadImageResourceCliHandler.kt
only-danmuku-adapter/src/main/kotlin/edu/only4/danmuku/adapter/application/distributed/clients/file_upload_session/UploadVideoChunkStorageCliHandler.kt
only-danmuku-domain/src/main/kotlin/edu/only4/danmuku/domain/aggregates/video_file_upload_session/VideoFileUploadSession.kt
only-danmuku-adapter/src/main/kotlin/edu/only4/danmuku/adapter/portal/api/web/VideoEncryptController.kt
only-danmuku-adapter/src/main/kotlin/edu/only4/danmuku/adapter/portal/api/web/VideoAbrController.kt
only-danmuku-adapter/src/main/kotlin/edu/only4/danmuku/adapter/portal/api/web/UCenterVideoPostController.kt
only-danmuku-adapter/src/main/kotlin/edu/only4/danmuku/adapter/portal/api/admin/AdminVideoAbrController.kt
```

- [ ] **Step 4: Re-run the consumer regression tests and then run a backend compile sweep**

Run:

```bash
.\gradlew.bat :only-danmuku-start:test --tests "edu.only4.danmuku.adapter.portal.api.HybridExceptionProtocolIntegrationTest" --refresh-dependencies --no-daemon
.\gradlew.bat :only-danmuku-start:test --tests "edu.only4.danmuku.adapter.portal.api.LoginCaptchaErrorResponseTest" --refresh-dependencies --no-daemon
.\gradlew.bat build -x test --refresh-dependencies --no-daemon
```

Expected:
- both targeted tests PASS
- backend compile succeeds with no `KnownException` symbols left

Run:

```bash
rg -n "KnownException|WarnException|ErrorException" only-danmuku-application/src/main/kotlin only-danmuku-domain/src/main/kotlin only-danmuku-adapter/src/main/kotlin
```

Expected: no matches.

- [ ] **Step 5: Commit the full backend migration**

```bash
git add only-danmuku-domain/src/main/kotlin only-danmuku-application/src/main/kotlin only-danmuku-adapter/src/main/kotlin only-danmuku-start/src/test/kotlin
git commit -m "refactor(exception): migrate danmuku backend"
```

### Task 6: Align the Vue Request Interceptor to the Hybrid Protocol

**Files:**
- Modify: `only-danmuku-web-ui/src/utils/Request.ts`

- [ ] **Step 1: Update the response/error branching so HTTP and business failures are handled separately**

```ts
instance.interceptors.response.use(
  (response: any) => {
    const { showLoading, errorCallback, showError = true } = response.config || {}
    if (showLoading && loading) loading.close()

    const responseData = response.data
    const respType = response?.request?.responseType || response?.config?.responseType
    if (respType === 'arraybuffer' || respType === 'blob') return responseData

    if (responseData?.code === 20000) return responseData.data

    if (errorCallback) errorCallback(responseData)
    if (showError) Message.error(responseData?.message || '请求失败')
    return Promise.reject({ showError, msg: responseData?.message, code: responseData?.code })
  },
  (error: any) => {
    if (error?.config?.showLoading && loading) loading.close()

    const showError = error?.config?.showError !== false
    const status = error?.response?.status

    if (status === 401) {
      const loginStore: any = useLoginStore()
      loginStore.setLogin(true)
      return Promise.reject({ showError: false, msg: '登录状态已失效', status })
    }

    if (status === 403 && showError) Message.error(error?.response?.data?.message || '没有权限执行当前操作')
    else if (status === 429 && showError) Message.error(error?.response?.data?.message || '请求过于频繁')
    else if (showError) Message.error(error?.response?.data?.message || '系统异常')

    return Promise.reject({
      showError,
      msg: error?.response?.data?.message || '系统异常',
      status,
    })
  }
)
```

- [ ] **Step 2: Run the frontend static checks**

Run: `npm run check`  
Expected: PASS.

- [ ] **Step 3: Commit the frontend protocol alignment**

```bash
git add src/utils/Request.ts
git commit -m "refactor(exception): align web ui interceptor"
```

### Task 7: Final Cross-Repo Verification

**Files:**
- No additional source files. This task verifies the full migration result before any merge or release action.

- [ ] **Step 1: Run the full `only-engine` verification suite**

Run:

```bash
cd c:\Users\LD_moxeii\Documents\code\only-workspace\only-engine
.\gradlew.bat :engine-common:test :engine-redis:test :engine-web:test --no-daemon
.\gradlew.bat publishToMavenLocal --no-daemon
```

Expected: PASS and local Maven contains the rebuilt `0.1.12-SNAPSHOT` engine artifacts.

- [ ] **Step 2: Run the `only-danmuku` verification suite against the freshly published engine**

Run:

```bash
cd c:\Users\LD_moxeii\Documents\code\only-workspace\only-danmuku
.\gradlew.bat :only-danmuku-start:test --tests "edu.only4.danmuku.adapter.portal.api.HybridExceptionProtocolIntegrationTest" --refresh-dependencies --no-daemon
.\gradlew.bat :only-danmuku-start:test --tests "edu.only4.danmuku.adapter.portal.api.LoginCaptchaErrorResponseTest" --refresh-dependencies --no-daemon
.\gradlew.bat build -x test --refresh-dependencies --no-daemon
```

Expected: PASS.

- [ ] **Step 3: Run the frontend verification**

Run:

```bash
cd c:\Users\LD_moxeii\Documents\code\only-workspace\only-danmuku-web-ui
npm run check
```

Expected: PASS.

- [ ] **Step 4: Perform the final legacy-symbol scan**

Run:

```bash
cd c:\Users\LD_moxeii\Documents\code\only-workspace\only-engine
rg -n "KnownException|WarnException|ErrorException" engine-common/src/main/kotlin engine-redis/src/main/kotlin engine-satoken/src/main/kotlin engine-oss/src/main/kotlin engine-web/src/main/kotlin

cd c:\Users\LD_moxeii\Documents\code\only-workspace\only-danmuku
rg -n "KnownException|WarnException|ErrorException" only-danmuku-application/src/main/kotlin only-danmuku-domain/src/main/kotlin only-danmuku-adapter/src/main/kotlin
```

Expected: no matches in production sources.

- [ ] **Step 5: Confirm all three repos are clean after verification**

```bash
cd c:\Users\LD_moxeii\Documents\code\only-workspace\only-engine
git status --short

cd c:\Users\LD_moxeii\Documents\code\only-workspace\only-danmuku
git status --short

cd c:\Users\LD_moxeii\Documents\code\only-workspace\only-danmuku-web-ui
git status --short
```
