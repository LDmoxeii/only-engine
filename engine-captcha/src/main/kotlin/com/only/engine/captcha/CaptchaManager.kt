package com.only.engine.captcha

import com.only.engine.captcha.config.properties.CaptchaProperties
import com.only.engine.captcha.core.entity.*
import com.only.engine.captcha.core.enums.CaptchaChannel
import com.only.engine.spi.captcha.CaptchaGenerator
import com.only.engine.spi.captcha.CaptchaSender
import com.only.engine.spi.captcha.CaptchaStore
import java.security.MessageDigest
import java.time.Instant
import java.util.*

class CaptchaManager(
    private val generators: List<CaptchaGenerator>,
    private val senders: List<CaptchaSender>,
    private val store: CaptchaStore,
    private val config: CaptchaProperties,
) {

    fun generate(cmd: GenerateCommand): GenerateResult {
        val generator = generators.firstOrNull { it.supports(cmd.type) }
            ?: error("No generator for ${cmd.type}")
        val content = generator.generate(cmd)
        val id = UUID.randomUUID().toString()
        val hash = hashValue(content)
        val record = CaptchaRecord(
            id = id,
            bizType = cmd.bizType,
            type = cmd.type,
            contentHash = hash,
            expireAt = Instant.now().plusSeconds(cmd.ttlSeconds),
            metadata = mapOf("channel" to cmd.channel.name) + cmd.metadata
        )
        store.save(record)

        val sender = senders.firstOrNull { it.supports(cmd.channel) }
            ?: error("No sender for channel ${cmd.channel}")
        sender.send(
            SendContext(
                channel = cmd.channel,
                record = record,
                rawContent = content,
                targets = cmd.targets,
                templateCode = cmd.templateCode
            )
        )
        val inline = if (cmd.channel == CaptchaChannel.INLINE) content else null
        return GenerateResult(id, cmd.channel, inline)
    }

    fun verify(id: String, input: String): Boolean {
        val record = store.find(id) ?: return false
        val expectedOk = compare(record.contentHash, input)
        val updated = when {
            expectedOk && config.verifyPolicy.onceOnly ->
                record.copy(used = true)

            else -> record
        }
        if (expectedOk) {
            if (config.verifyPolicy.onceOnly) store.remove(id) else store.update(updated)
            return true
        }

        // 失败
        if (config.verifyPolicy.deleteOnFail) {
            store.remove(id)
        } else {
            store.update(record.copy(failCount = record.failCount + 1))
        }
        return false
    }

    private fun compare(storedHash: String, rawInput: String): Boolean {
        val candidate = if (config.verifyPolicy.caseInsensitive) rawInput.lowercase() else rawInput
        return storedHash == hash(candidate)
    }

    private fun hashValue(content: CaptchaContent): String =
        when (content) {
            is CaptchaContent.Text -> hash(normalize(content.value))
            is CaptchaContent.Image -> hash(normalize(content.text))
        }

    private fun normalize(v: String) =
        if (config.verifyPolicy.caseInsensitive) v.lowercase() else v

    private fun hash(v: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(v.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
