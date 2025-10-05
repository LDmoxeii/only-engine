package com.only.engine.redis.misc

import cn.hutool.core.date.DatePattern
import cn.hutool.core.date.DateUtil
import cn.hutool.core.util.StrUtil
import cn.hutool.extra.spring.SpringUtil
import org.redisson.api.RIdGenerator
import org.redisson.api.RedissonClient
import java.time.Duration

/**
 * 发号器工具类
 *
 * @author 秋辞未寒
 * @since 2024-12-10
 */
object SequenceUtils {

    /**
     * 默认初始值
     */
    const val DEFAULT_INIT_VALUE = 1L

    /**
     * 默认步长
     */
    const val DEFAULT_STEP_VALUE = 1L

    /**
     * 默认过期时间-天
     */
    val DEFAULT_EXPIRE_TIME_DAY: Duration = Duration.ofDays(1)

    /**
     * 默认过期时间-分钟
     */
    val DEFAULT_EXPIRE_TIME_MINUTE: Duration = Duration.ofMinutes(1)

    /**
     * 获取 Redisson 客户端实例
     */
    private val REDISSON_CLIENT: RedissonClient by lazy {
        SpringUtil.getBean(RedissonClient::class.java)
    }

    /**
     * 获取 ID 生成器
     *
     * @param key        业务 key
     * @param expireTime 过期时间
     * @param initValue  ID 初始值
     * @param stepValue  ID 步长
     * @return ID 生成器
     */
    private fun getIdGenerator(key: String, expireTime: Duration, initValue: Long?, stepValue: Long?): RIdGenerator {
        val actualInitValue = if (initValue == null || initValue <= 0) DEFAULT_INIT_VALUE else initValue
        val actualStepValue = if (stepValue == null || stepValue <= 0) DEFAULT_STEP_VALUE else stepValue

        val idGenerator = REDISSON_CLIENT.getIdGenerator(key)
        // 设置初始值和步长
        idGenerator.tryInit(actualInitValue, actualStepValue)
        // 设置过期时间
        idGenerator.expire(expireTime)
        return idGenerator
    }

    /**
     * 获取指定业务 key 的唯一 id
     *
     * @param key        业务 key
     * @param expireTime 过期时间
     * @param initValue  ID 初始值
     * @param stepValue  ID 步长
     * @return 唯一 id
     */
    @JvmStatic
    fun nextId(key: String, expireTime: Duration, initValue: Long?, stepValue: Long?): Long {
        return getIdGenerator(key, expireTime, initValue, stepValue).nextId()
    }

    /**
     * 获取指定业务 key 的唯一 id 字符串
     *
     * @param key        业务 key
     * @param expireTime 过期时间
     * @param initValue  ID 初始值
     * @param stepValue  ID 步长
     * @return 唯一 id
     */
    @JvmStatic
    fun nextIdStr(key: String, expireTime: Duration, initValue: Long?, stepValue: Long?): String {
        return nextId(key, expireTime, initValue, stepValue).toString()
    }

    /**
     * 获取指定业务 key 的唯一 id (ID 初始值=1, ID 步长=1)
     *
     * @param key        业务 key
     * @param expireTime 过期时间
     * @return 唯一 id
     */
    @JvmStatic
    fun nextId(key: String, expireTime: Duration): Long {
        return getIdGenerator(key, expireTime, DEFAULT_INIT_VALUE, DEFAULT_STEP_VALUE).nextId()
    }

    /**
     * 获取指定业务 key 的唯一 id 字符串 (ID 初始值=1, ID 步长=1)
     *
     * @param key        业务 key
     * @param expireTime 过期时间
     * @return 唯一 id
     */
    @JvmStatic
    fun nextIdStr(key: String, expireTime: Duration): String {
        return nextId(key, expireTime).toString()
    }

    /**
     * 获取 yyyyMMdd 开头的唯一 id
     *
     * @return 唯一 id
     */
    @JvmStatic
    fun nextIdDate(): String {
        return nextIdDate("")
    }

    /**
     * 获取 prefix + yyyyMMdd 开头的唯一 id
     *
     * @param prefix 业务前缀
     * @return 唯一 id
     */
    @JvmStatic
    fun nextIdDate(prefix: String): String {
        // 前缀+日期 构建 prefixKey
        val prefixKey = StrUtil.format(
            "{}{}",
            StrUtil.blankToDefault(prefix, ""),
            DateUtil.format(DateUtil.date(), DatePattern.PURE_DATE_FORMATTER)
        )
        // 获取下一个 id
        val nextId = getIdGenerator(prefixKey, DEFAULT_EXPIRE_TIME_DAY, DEFAULT_INIT_VALUE, DEFAULT_STEP_VALUE).nextId()
        // 返回完整 id
        return StrUtil.format("{}{}", prefixKey, nextId)
    }

    /**
     * 获取 yyyyMMddHHmmss 开头的唯一 id
     *
     * @return 唯一 id
     */
    @JvmStatic
    fun nextIdDateTime(): String {
        return nextIdDateTime("")
    }

    /**
     * 获取 prefix + yyyyMMddHHmmss 开头的唯一 id
     *
     * @param prefix 业务前缀
     * @return 唯一 id
     */
    @JvmStatic
    fun nextIdDateTime(prefix: String): String {
        // 前缀+日期时间 构建 prefixKey
        val prefixKey = StrUtil.format(
            "{}{}",
            StrUtil.blankToDefault(prefix, ""),
            DateUtil.format(DateUtil.date(), DatePattern.PURE_DATETIME_FORMATTER)
        )
        // 获取下一个 id
        val nextId =
            getIdGenerator(prefixKey, DEFAULT_EXPIRE_TIME_MINUTE, DEFAULT_INIT_VALUE, DEFAULT_STEP_VALUE).nextId()
        // 返回完整 id
        return StrUtil.format("{}{}", prefixKey, nextId)
    }
}
