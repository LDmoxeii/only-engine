package com.only.engine.captcha.store

import com.only.engine.entity.CaptchaRecord
import com.only.engine.spi.captcha.CaptchaStore
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

class InMemoryCaptchaStore : CaptchaStore {
    private val data = ConcurrentHashMap<String, CaptchaRecord>()

    override fun save(record: CaptchaRecord) {
        data[record.id] = record
    }

    override fun find(id: String): CaptchaRecord? {
        val r = data[id] ?: return null
        return if (r.expireAt.isBefore(Instant.now())) {
            data.remove(id)
            null
        } else r
    }

    override fun update(record: CaptchaRecord) {
        data[record.id] = record
    }

    override fun remove(id: String) {
        data.remove(id)
    }
}
