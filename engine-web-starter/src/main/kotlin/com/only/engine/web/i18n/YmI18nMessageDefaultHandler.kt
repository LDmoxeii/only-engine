package com.only.engine.web.i18n

import org.springframework.context.MessageSource
import org.springframework.context.MessageSourceResolvable
import org.springframework.context.NoSuchMessageException
import java.util.Locale

class YmI18nMessageDefaultHandler(
    private val messageSource: MessageSource
) : I18nMessageHandler {

    @Throws(NoSuchMessageException::class)
    override fun getMessage(
        locale: Locale,
        defaultMessage: String?,
        code: String,
        vararg args: Any
    ): String = messageSource.getMessage(code, args, defaultMessage, locale) ?: code

    @Throws(NoSuchMessageException::class)
    override fun getMessage(
        locale: Locale,
        resolvable: MessageSourceResolvable
    ): String = messageSource.getMessage(resolvable, locale)
}
