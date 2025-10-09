package com.only.engine.captcha.generate

import com.only.engine.captcha.core.entity.CaptchaContent
import com.only.engine.captcha.core.entity.GenerateCommand
import com.only.engine.captcha.core.enums.CaptchaType
import com.only.engine.spi.captcha.CaptchaGenerator
import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.random.Random

class ImageCaptchaGenerator : CaptchaGenerator {
    override fun supports(type: CaptchaType) = type == CaptchaType.IMAGE

    override fun generate(cmd: GenerateCommand): CaptchaContent {
        val width = cmd.width ?: 120
        val height = cmd.height ?: 40
        val text = TextCaptchaGenerator()
            .generate(cmd.copy(type = CaptchaType.TEXT))
            .let { (it as CaptchaContent.Text).value }

        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val g = image.createGraphics()
        try {
            g.color = Color(240, 240, 240)
            g.fillRect(0, 0, width, height)
            g.font = Font("Arial", Font.BOLD, (height * 0.6).toInt())
            text.forEachIndexed { i, c ->
                g.color = Color(Random.nextInt(50, 150), Random.nextInt(50, 150), Random.nextInt(50, 150))
                val x = (width * (0.1 + (i.toDouble() / text.length) * 0.8)).toInt()
                val y = (height * 0.7).toInt()
                g.drawString(c.toString(), x, y)
            }
        } finally {
            g.dispose()
        }
        val baos = ByteArrayOutputStream()
        ImageIO.write(image, "png", baos)
        return CaptchaContent.Image(baos.toByteArray(), "image/png", text)
    }
}
