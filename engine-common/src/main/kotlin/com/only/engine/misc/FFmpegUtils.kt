package com.only.engine.misc

import cn.hutool.core.util.RuntimeUtil
import com.only.engine.exception.KnownException
import org.slf4j.LoggerFactory
import java.io.File
import java.math.BigDecimal

private const val THUMBNAIL_WIDTH = 200
private const val IMAGE_THUMBNAIL_SUFFIX = "_thumbnail.jpg"
private const val TS_NAME = "index.ts"
private const val M3U8_NAME = "index.m3u8"
private const val DEFAULT_SEGMENT_SECONDS = 10

private val logger = LoggerFactory.getLogger("FFmpegUtils")

/**
 * 生成图片缩略图，默认宽度 200 像素，等比例缩放高度。
 *
 * @param filePath 原始图片路径
 * @param showLog 是否输出执行日志
 */
fun createImageThumbnail(filePath: String, showLog: Boolean = false) {
    if (filePath.isBlank()) {
        throw KnownException.illegalArgument("filePath")
    }

    val sourceFile = File(filePath).absoluteFile
    val targetFile = buildSiblingFile(sourceFile, IMAGE_THUMBNAIL_SUFFIX)
    targetFile.parentFile?.takeIf { !it.exists() }?.let {
        if (!it.mkdirs()) {
            throw KnownException.systemError("无法创建缩略图目录: ${it.absolutePath}")
        }
    }

    val command = listOf(
        "ffmpeg",
        "-y",
        "-i",
        sourceFile.absolutePath,
        "-vf",
        "scale=$THUMBNAIL_WIDTH:-1",
        targetFile.absolutePath
    )

    execForStdout(command, showLog)
}

/**
 * 获取视频编码信息。
 *
 * @param videoFilePath 视频文件路径
 * @param showLog 是否输出执行日志
 * @return 视频编码，如果未获取到则返回空字符串
 */
fun getVideoCodec(videoFilePath: String, showLog: Boolean = false): String {
    if (videoFilePath.isBlank()) {
        throw KnownException.illegalArgument("videoFilePath")
    }

    val inputFile = File(videoFilePath).absoluteFile
    val command = listOf(
        "ffprobe",
        "-v",
        "error",
        "-select_streams",
        "v:0",
        "-show_entries",
        "stream=codec_name",
        "-of",
        "default=noprint_wrappers=1:nokey=1",
        inputFile.absolutePath
    )

    val codec = execForStdout(command, showLog)
        .lineSequence()
        .map { it.trim() }
        .firstOrNull { it.isNotEmpty() }
        .orEmpty()

    return codec
}

/**
 * 将 HEVC 视频转码为 H.264 编码的 MP4 文件。
 *
 * @param outputFilePath 转码后文件路径
 * @param inputFilePath 原始视频文件路径
 * @param showLog 是否输出执行日志
 */
fun convertHevcToMp4(
    outputFilePath: String,
    inputFilePath: String,
    showLog: Boolean = false,
) {
    if (inputFilePath.isBlank()) {
        throw KnownException.illegalArgument("inputFilePath")
    }
    if (outputFilePath.isBlank()) {
        throw KnownException.illegalArgument("outputFilePath")
    }

    val inputFile = File(inputFilePath).absoluteFile
    val outputFile = File(outputFilePath).absoluteFile
    outputFile.parentFile?.takeIf { !it.exists() }?.let {
        if (!it.mkdirs()) {
            throw KnownException.systemError("无法创建输出目录: ${it.absolutePath}")
        }
    }

    val command = listOf(
        "ffmpeg",
        "-y",
        "-i",
        inputFile.absolutePath,
        "-c:v",
        "libx264",
        "-crf",
        "20",
        outputFile.absolutePath
    )

    execForStdout(command, showLog)
}

/**
 * 将视频转为分片 TS 文件并生成 M3U8 索引。
 *
 * @param tsFolder 输出目录
 * @param videoFilePath 原始视频文件路径
 * @param segmentSeconds 分片时长（秒）
 * @param showLog 是否输出执行日志
 */
fun convertVideoToTs(
    tsFolder: File,
    videoFilePath: String,
    segmentSeconds: Int = DEFAULT_SEGMENT_SECONDS,
    showLog: Boolean = false,
) {
    if (videoFilePath.isBlank()) {
        throw KnownException.illegalArgument("videoFilePath")
    }
    if (segmentSeconds <= 0) {
        throw KnownException.illegalArgument("segmentSeconds")
    }

    val inputFile = File(videoFilePath).absoluteFile
    val outputDir = runCatching { tsFolder.canonicalFile }.getOrElse {
        throw KnownException.systemError(it)
    }
    if (!outputDir.exists() && !outputDir.mkdirs()) {
        throw KnownException.systemError("无法创建 TS 输出目录: ${outputDir.absolutePath}")
    }

    val tsFile = File(outputDir, TS_NAME)
    val m3u8File = File(outputDir, M3U8_NAME)
    val segmentPattern = File(outputDir, "%04d.ts").absolutePath

    // 第一步：转为 TS 格式的临时文件
    val transferCommand = listOf(
        "ffmpeg",
        "-y",
        "-i",
        inputFile.absolutePath,
        "-vcodec",
        "copy",
        "-acodec",
        "copy",
        "-bsf:v",
        "h264_mp4toannexb",
        tsFile.absolutePath
    )
    execForStdout(transferCommand, showLog)

    try {
        // 第二步：切片并生成 M3U8
        val cutCommand = listOf(
            "ffmpeg",
            "-y",
            "-i",
            tsFile.absolutePath,
            "-c",
            "copy",
            "-map",
            "0",
            "-f",
            "segment",
            "-segment_list",
            m3u8File.absolutePath,
            "-segment_time",
            segmentSeconds.toString(),
            segmentPattern
        )

        execForStdout(cutCommand, showLog)
    } finally {
        if (tsFile.exists() && !tsFile.delete()) {
            logger.warn("删除临时 TS 文件失败: {}", tsFile.absolutePath)
        }
    }
}

/**
 * 获取视频时长信息（秒）。
 *
 * @param videoFilePath 视频文件路径
 * @param showLog 是否输出执行日志
 * @return 视频时长（秒），获取失败时返回 0
 */
fun getVideoDuration(videoFilePath: String, showLog: Boolean = false): Int {
    if (videoFilePath.isBlank()) {
        throw KnownException.illegalArgument("videoFilePath")
    }

    val inputFile = File(videoFilePath).absoluteFile
    val command = listOf(
        "ffprobe",
        "-v",
        "error",
        "-show_entries",
        "format=duration",
        "-of",
        "default=noprint_wrappers=1:nokey=1",
        inputFile.absolutePath
    )

    val rawDuration = execForStdout(command, showLog)
        .lineSequence()
        .map { it.trim() }
        .firstOrNull { it.isNotEmpty() }
        ?: return 0

    return runCatching { BigDecimal(rawDuration).toInt() }.getOrDefault(0)
}

private fun buildSiblingFile(source: File, suffix: String): File {
    val parent = source.parentFile
    val baseName = source.nameWithoutExtension
    val siblingName = baseName + suffix
    return if (parent == null) {
        File(siblingName)
    } else {
        File(parent, siblingName)
    }
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

        val commandLine = commands.joinToString(" ")
        if (showLog) {
            logger.info(
                "FFmpeg command executed: {}\nstdout: {}\nstderr: {}\nexitCode: {}",
                commandLine,
                stdout.trim(),
                stderr.trim(),
                exitCode
            )
        } else {
            logger.debug("FFmpeg command executed: {} (exitCode={})", commandLine, exitCode)
            if (stderr.isNotBlank()) {
                logger.debug("FFmpeg stderr: {}", stderr.trim())
            }
        }

        if (exitCode != 0) {
            val message = buildString {
                append("FFmpeg 命令执行失败 (exitCode=").append(exitCode).append(")")
                if (stderr.isNotBlank()) {
                    append(": ").append(stderr.trim())
                }
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
