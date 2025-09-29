package com.only.engine.satoken.adapter

import com.only.engine.security.SecurityContextHolder
import com.only.engine.security.model.SecurityContext

class SaTokenSecurityContextHolder(
    private val userDetailsProvider: SaTokenUserDetailsProvider,
) : SecurityContextHolder {

    companion object {
        private val contextHolder = ThreadLocal<SecurityContext>()
    }

    override fun getContext(): SecurityContext? {
        return contextHolder.get()
    }

    override fun setContext(context: SecurityContext) {
        contextHolder.set(context)
    }

    override fun clearContext() {
        contextHolder.remove()
    }

    override fun createEmptyContext(): SecurityContext {
        return SecurityContext(
            userInfo = null,
            token = null
        )
    }
}
