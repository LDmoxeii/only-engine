package com.only.engine.redis.store

import com.only.engine.entity.CaptchaRecord
import com.only.engine.redis.misc.RedisUtils
import com.only.engine.spi.captcha.CaptchaStore

class RedisCaptchaStore : CaptchaStore {

    override fun save(record: CaptchaRecord) = RedisUtils.setCacheObject(record.id, record)

    override fun find(id: String): CaptchaRecord? = RedisUtils.getCacheObject<CaptchaRecord>(id)

    override fun update(record: CaptchaRecord): Unit = RedisUtils.setCacheObject(record.id, record)

    override fun remove(id: String) = RedisUtils.deleteObject(id)
}
