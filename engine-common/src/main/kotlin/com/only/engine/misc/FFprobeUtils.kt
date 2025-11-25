package com.only.engine.misc

import cn.hutool.core.util.RuntimeUtil
import com.only.engine.exception.KnownException
import org.slf4j.LoggerFactory
import java.io.File

/**
 * FFprobe 工具方法，用于探测视频基础信息（分辨率/码率等）
 */
object FFprobeUtils {

    private val logger = LoggerFactory.getLogger("FFprobeUtils")

    data class VideoProbeResult(
        val width: Int,
        val height: Int,
        val bitrateKbps: Int?,
    )

    /**
     * 探测视频分辨率与码率（码率可能为空）
     */
    fun probeVideoResolution(inputPath: String, showLog: Boolean = false): VideoProbeResult {
        if (inputPath.isBlank()) {
            throw KnownException.illegalArgument("inputPath")
        }
        val inputFile = File(inputPath).absoluteFile
        if (!inputFile.exists()) {
            throw KnownException.systemError("视频文件不存在: ${inputFile.absolutePath}")
        }

        val resolutionCommand = listOf(
            "ffprobe",
            "-v",
            "error",
            "-select_streams",
            "v:0",
            "-show_entries",
            "stream=width,height",
            "-of",
            "csv=s=x:p=0",
            inputFile.absolutePath
        )
        val resolutionLine = execForStdout(resolutionCommand, showLog)
            .lineSequence()
            .map { it.trim() }
            .firstOrNull { it.isNotEmpty() }
            ?: throw KnownException.systemError("无法探测分辨率: ${inputFile.absolutePath}")

        val (width, height) = resolutionLine.split("x")
            .mapNotNull { it.toIntOrNull() }
            .let {
                if (it.size == 2) it[0] to it[1]
                else throw KnownException.systemError("分辨率格式异常: $resolutionLine")
            }

        val bitrateCommand = listOf(
            "ffprobe",
            "-v",
            "error",
            "-show_entries",
            "format=bit_rate",
            "-of",
            "default=noprint_wrappers=1:nokey=1",
            inputFile.absolutePath
        )
        val bitrateValue = execForStdout(bitrateCommand, showLog)
            .lineSequence()
            .map { it.trim() }
            .firstOrNull { it.isNotEmpty() }
            ?.toLongOrNull()

        val bitrateKbps = bitrateValue?.let { (it / 1000L).toInt() }

        return VideoProbeResult(width = width, height = height, bitrateKbps = bitrateKbps)
    }

    private fun execForStdout(commands: List<String>, showLog: Boolean): String {
        if (commands.isEmpty()) {
            throw KnownException.illegalArgument("commands")
        }

        var process: Process? = null
        return try {
            process = RuntimeUtil.exec(*commands.toTypedArray())
            val stdout = RuntimeUtil.getResult(process)
            val stderr = RuntimeUtil.getErrorResult(process)
            val exitCode = process.waitFor()

            val cmd = commands.joinToString(" ")
            if (showLog) {
                logger.info(
                    "FFprobe command executed: {}\nstdout: {}\nstderr: {}\nexitCode: {}",
                    cmd,
                    stdout.trim(),
                    stderr.trim(),
                    exitCode
                )
            } else if (stderr.isNotBlank()) {
                logger.debug("FFprobe stderr: {}", stderr.trim())
            }

            if (exitCode != 0) {
                val message = buildString {
                    append("FFprobe 命令执行失败 (exitCode=").append(exitCode).append(")")
                    if (stderr.isNotBlank()) append(": ").append(stderr.trim())
                }
                throw KnownException.systemError(message)
            }

            stdout
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw KnownException.systemError(e)
        } catch (e: KnownException) {
            throw e
        } catch (e: Exception) {
            throw KnownException.systemError(e)
        } finally {
            process?.destroy()
        }
    }
}
