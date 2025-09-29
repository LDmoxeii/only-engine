package com.only.engine.security

import com.only.engine.security.config.SecurityProperties
import com.only.engine.security.factory.SecurityProviderFactory
import org.slf4j.LoggerFactory

class SecurityManager(
    private val securityProperties: SecurityProperties,
    private val securityProviderFactory: SecurityProviderFactory,
) {

    companion object {
        private val log = LoggerFactory.getLogger(SecurityManager::class.java)

        @JvmStatic
        @Volatile
        private var instance: SecurityManager? = null

        @JvmStatic
        fun getInstance(): SecurityManager {
            return instance ?: throw IllegalStateException("SecurityManager not initialized")
        }

        @JvmStatic
        internal fun setInstance(manager: SecurityManager) {
            instance = manager
        }
    }

    val authenticationService: AuthenticationService by lazy {
        securityProviderFactory.createAuthenticationService()
    }

    val authorizationService: AuthorizationService by lazy {
        securityProviderFactory.createAuthorizationService()
    }

    val tokenManager: TokenManager by lazy {
        securityProviderFactory.createTokenManager()
    }

    val securityContextHolder: SecurityContextHolder by lazy {
        securityProviderFactory.createSecurityContextHolder()
    }

    val securityInterceptors: List<SecurityInterceptor> by lazy {
        securityProviderFactory.createSecurityInterceptors()
    }

    fun getProviderName(): String = securityProviderFactory.getProviderName()

    init {
        log.info("Security manager initialized with provider: {}", securityProperties.provider)
        setInstance(this)
    }
}
