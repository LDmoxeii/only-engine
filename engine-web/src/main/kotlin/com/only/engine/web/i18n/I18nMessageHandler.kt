package com.only.engine.web.i18n

import com.only.engine.misc.ThreadLocalUtils
import org.springframework.context.MessageSourceResolvable
import org.springframework.context.NoSuchMessageException
import java.util.*

interface I18nMessageHandler{

    @Throws(NoSuchMessageException::class)
    fun getMessage(
        locale: Locale = ThreadLocalUtils.getLocale(),
        defaultMessage: String? = null,
        code: String,
        vararg args: Any,
    ): String

    @Throws(NoSuchMessageException::class)
    fun getMessage(
        locale: Locale,
        resolvable: MessageSourceResolvable
    ): String
}
