package com.only.engine.satoken.factory

import com.only.engine.satoken.adapter.*
import com.only.engine.security.*
import com.only.engine.security.factory.SecurityProviderFactory
import com.only.engine.security.url.UrlCollector

class SaTokenSecurityProviderFactory(
    private val userDetailsProvider: SaTokenUserDetailsProvider,
    private val urlCollector: UrlCollector,
) : SecurityProviderFactory {

    override fun createAuthenticationService(): AuthenticationService {
        return SaTokenAuthenticationService(userDetailsProvider)
    }

    override fun createAuthorizationService(): AuthorizationService {
        return SaTokenAuthorizationService(userDetailsProvider)
    }

    override fun createTokenManager(): TokenManager {
        return SaTokenTokenManager()
    }

    override fun createSecurityContextHolder(): SecurityContextHolder {
        return SaTokenSecurityContextHolder(userDetailsProvider)
    }

    override fun createSecurityInterceptors(): List<SecurityInterceptor> {
        val contextHolder = createSecurityContextHolder() as SaTokenSecurityContextHolder
        return listOf(
            SaTokenSecurityInterceptor(userDetailsProvider, contextHolder, urlCollector)
        )
    }

    override fun getProviderName(): String = "sa-token"
}
