package com.only.engine.captcha.core

import com.only.engine.captcha.entity.CaptchaRecord
import java.time.Instant

interface CaptchaStore {
    fun save(record: CaptchaRecord)
    fun find(id: String): CaptchaRecord?
    fun update(record: CaptchaRecord)
    fun remove(id: String)
    fun now(): Instant = Instant.now()
}
