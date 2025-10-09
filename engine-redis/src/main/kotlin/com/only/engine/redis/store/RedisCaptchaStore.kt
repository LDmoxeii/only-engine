package com.only.engine.redis.store

import com.only.engine.entity.CaptchaRecord
import com.only.engine.redis.misc.RedisUtils
import com.only.engine.spi.captcha.CaptchaStore
import java.time.Duration
import java.time.Instant

/**
 * 基于 Redis 的验证码存储实现
 *
 * 使用 RedisUtils 工具类进行验证码记录的存储、查询、更新和删除操作。
 * 验证码记录会自动根据 expireAt 时间设置过期。
 */
class RedisCaptchaStore : CaptchaStore {

    /**
     * 保存验证码记录
     *
     * 将验证码记录存储到 Redis 中，并根据 expireAt 设置过期时间
     *
     * @param record 验证码记录
     */
    override fun save(record: CaptchaRecord) {
        val ttl = calculateTtl(record.expireAt)
        if (ttl.isNegative || ttl.isZero) {
            // 如果已过期，不存储
            return
        }
        RedisUtils.setCacheObject(record.id, record, ttl)
    }

    /**
     * 查找验证码记录
     *
     * 从 Redis 中根据 id 查找验证码记录
     *
     * @param id 验证码 id
     * @return 验证码记录，如果不存在或已过期则返回 null
     */
    override fun find(id: String): CaptchaRecord? {
        val record = RedisUtils.getCacheObject<CaptchaRecord>(id) ?: return null

        // 检查是否已过期
        val ttl = calculateTtl(record.expireAt)
        if (ttl.isNegative || ttl.isZero) {
            // 如果已过期，删除
            remove(record.id)
            return null
        }

        return record
    }

    /**
     * 更新验证码记录
     *
     * 更新 Redis 中的验证码记录，保留原有的 TTL
     *
     * @param record 验证码记录
     */
    override fun update(record: CaptchaRecord) {
        val ttl = calculateTtl(record.expireAt)
        if (ttl.isNegative || ttl.isZero) {
            // 如果已过期，删除
            remove(record.id)
            return
        }
        RedisUtils.setCacheObject(record.id, record, ttl)
    }

    /**
     * 删除验证码记录
     *
     * 从 Redis 中删除指定 id 的验证码记录
     *
     * @param id 验证码 id
     */
    override fun remove(id: String) {
        RedisUtils.deleteObject(id)
    }

    /**
     * 计算剩余 TTL
     *
     * @param expireAt 过期时间
     * @return 剩余时间
     */
    private fun calculateTtl(expireAt: Instant): Duration {
        return Duration.between(now(), expireAt)
    }
}
