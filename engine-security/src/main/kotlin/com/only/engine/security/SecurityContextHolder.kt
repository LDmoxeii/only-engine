package com.only.engine.security

import com.only.engine.security.model.SecurityContext

interface SecurityContextHolder {

    fun getContext(): SecurityContext?

    fun setContext(context: SecurityContext)

    fun clearContext()

    fun createEmptyContext(): SecurityContext
}