package com.only.engine.translation.translation

import com.only.engine.translation.annotation.TranslationType
import com.only.engine.translation.core.BatchTranslationInterface
import com.only.engine.translation.core.TranslationInterface
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 将秒级时间戳转换为指定格式的日期字符串
 * 使用 other 传入日期格式，例如：yyyy-MM-dd（默认使用该格式）
 */
@TranslationType(type = EpochSecondToDateStringTranslation.TYPE)
class EpochSecondToDateStringTranslation :
    TranslationInterface<String>, BatchTranslationInterface<String> {

    companion object {
        const val TYPE = "epoch_second_to_date_str"
        private const val DEFAULT_PATTERN = "yyyy-MM-dd"
    }

    override fun translation(key: Any, other: String): String? {
        val seconds = when (key) {
            is Number -> key.toLong()
            is String -> key.toLongOrNull()
            else -> null
        } ?: return null

        val pattern = other.takeIf { it.isNotBlank() } ?: DEFAULT_PATTERN

        return try {
            val formatter = DateTimeFormatter.ofPattern(pattern)
            Instant.ofEpochSecond(seconds)
                .atZone(ZoneId.systemDefault())
                .format(formatter)
        } catch (_: Exception) {
            null
        }
    }

    override fun translationBatch(keys: Collection<Any>, other: String): Map<Any, String?> {
        if (keys.isEmpty()) return emptyMap()

        val pattern = other.takeIf { it.isNotBlank() } ?: DEFAULT_PATTERN
        val formatter = try {
            DateTimeFormatter.ofPattern(pattern)
        } catch (_: Exception) {
            return keys.associateWith { null }
        }

        val zone = ZoneId.systemDefault()

        return keys.associateWith { k ->
            val seconds = when (k) {
                is Number -> k.toLong()
                is String -> k.toLongOrNull()
                else -> null
            }
            if (seconds == null) null else try {
                Instant.ofEpochSecond(seconds).atZone(zone).format(formatter)
            } catch (_: Exception) {
                null
            }
        }
    }
}

