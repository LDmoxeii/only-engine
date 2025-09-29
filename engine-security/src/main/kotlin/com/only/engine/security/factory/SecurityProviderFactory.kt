package com.only.engine.security.factory

import com.only.engine.security.*

interface SecurityProviderFactory {

    fun createAuthenticationService(): AuthenticationService

    fun createAuthorizationService(): AuthorizationService

    fun createTokenManager(): TokenManager

    fun createSecurityContextHolder(): SecurityContextHolder

    fun createSecurityInterceptors(): List<SecurityInterceptor>

    fun getProviderName(): String
}
