package com.only.engine.satoken.provider

import com.only.engine.satoken.utils.LoginHelper
import com.only.engine.spi.audit.AuditOperatorProvider

class SaTokenAuditOperatorProvider : AuditOperatorProvider {

    override fun currentOperatorId(): Any? = LoginHelper.getUserInfo()?.id

    override fun currentOperatorName(): String? = LoginHelper.getUserInfo()?.username
}
