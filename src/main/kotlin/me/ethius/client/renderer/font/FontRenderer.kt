package me.ethius.client.renderer.font

import me.ethius.client.renderer.Mesh
import me.ethius.shared.*
import org.joml.Matrix4dStack
import org.lwjgl.BufferUtils
import java.io.InputStream
import java.nio.Buffer
import kotlin.math.floor
import kotlin.math.roundToInt

class FontRenderer(inputStream:InputStream) {

    private val fonts:Array<Font?>
    var font:Font? = null
    private var isBuilding = false

    fun begin(scale:double) {
        if (isBuilding) throw RuntimeException("FontRenderer.begin() called twice")
        font = run {
            val scaleA = floor(scale * 10) / 10
            val scaleI:int =
                if (scaleA >= 3) 5 else if (scaleA >= 2.5) 4 else if (scaleA >= 2) 3 else if (scaleA >= 1.5) 2 else 1
            fonts[scaleI - 1]
        }
        isBuilding = true
    }

    fun getWidth(
        text:string,
        length:int = text.length,
        shadow:bool = true,
        scale:double = 0.965,
        kern:(int) -> double = { getKern(it) },
    ):double {
        if (font == null) return 0.0
        return (font!!.getWidth(text, length, scale, kern) + if (shadow) 1 else 0)
    }

    fun getWidth(text:string, scale:double):double {
        return getWidth(text, text.length, true, scale)
    }

    fun getHeight(
        shadow:bool,
        scale:double = 0.965,
    ):double {
        if (font == null) return 0.0
        return (font!!.getHeight() - 2.5 + if (shadow) 1 else 0) * scale
    }

    fun draw(
        matrices:Matrix4dStack,
        text:string,
        x:double,
        y:double,
        color:long,
        shadow:bool,
        scale:double = 0.965,
        kern:(int) -> double = { getKern(it) },
    ):double {
        val dynScale:double = scale
        begin(dynScale)
        Mesh.triangles.begin()
        val f = if (shadow) {
            font!!.draw(Mesh.triangles, matrices, text, x, y, color, dynScale, true, kern)
        } else font!!.draw(Mesh.triangles, matrices, text, x, y, color, dynScale, false, kern)
        Mesh.drawTriangles()
        return f
    }

    fun drawWithoutEnding(
        matrices:Matrix4dStack,
        text:string,
        x:double,
        y:double,
        color:long,
        shadow:bool,
        scale:double = 0.965,
        kern:(int) -> double = { getKern(it) },
    ) {
        val dynScale:double = scale
        if (shadow) {
            font!!.draw(Mesh.triangles, matrices, text, x, y, color, dynScale, true, kern)
        } else font!!.draw(Mesh.triangles, matrices, text, x, y, color, dynScale, false, kern)
    }

    fun drawLeft(
        matrices:Matrix4dStack,
        text:string,
        x:double,
        y:double,
        color:long,
        shadow:bool,
        scale:double = 0.965,
        kern:(int) -> double = { getKern(it) },
    ) {
        draw(matrices, text, x - getWidth(text, text.length, shadow, scale, kern), y, color, shadow, scale, kern)
    }

    fun drawLeftWithoutEnding(
        matrices:Matrix4dStack,
        text:string,
        x:double,
        y:double,
        color:long,
        shadow:bool,
        scale:double = 0.965,
        kern:(int) -> double = { getKern(it) },
    ) {
        drawWithoutEnding(matrices,
                          text,
                          x - getWidth(text, text.length, shadow, scale, kern),
                          y,
                          color,
                          shadow,
                          scale,
                          kern)
    }

    fun drawCenteredString(
        matrices:Matrix4dStack,
        text:string,
        x:double,
        y:double,
        color:long,
        shadow:bool,
        scale:double = 0.965,
        kern:(int) -> double = { getKern(it) },
    ):double {
        val dynScale:double = scale
        begin(dynScale)
        Mesh.triangles.begin()
        val f = if (shadow) {
            font!!.draw(Mesh.triangles,
                        matrices,
                        text,
                        x - getWidth(text, text.length, true, scale, kern) / 2f,
                        y - getHeight(shadow, scale) / 2f,
                        color,
                        dynScale,
                        true,
                        kern)
        } else font!!.draw(Mesh.triangles,
                           matrices,
                           text,
                           x - getWidth(text, text.length, false, scale, kern) / 2f,
                           y - getHeight(shadow, scale) / 2f,
                           color,
                           dynScale,
                           false,
                           kern)
        Mesh.drawTriangles()
        return f / 2f
    }

    fun drawCenteredStringWithoutEnding(
        matrices:Matrix4dStack,
        text:string,
        x:double,
        y:double,
        color:long,
        shadow:bool,
        scale:double = 0.965,
        kern:(int) -> double = { getKern(it) },
    ) {
        val dynScale:double = scale
        if (shadow) {
            font!!.draw(Mesh.triangles,
                        matrices,
                        text,
                        x - getWidth(text, text.length, true, scale, kern) / 2f,
                        y - getHeight(shadow) / 2f,
                        color,
                        dynScale,
                        true,
                        kern)
        } else font!!.draw(Mesh.triangles,
                           matrices,
                           text,
                           x - getWidth(text, text.length, false, scale, kern) / 2f,
                           y - getHeight(shadow) / 2f,
                           color,
                           dynScale,
                           false,
                           kern)
    }

    fun end() {
        if (!isBuilding) return
        isBuilding = false
    }

    fun wrapWords(
        text:string,
        width:double,
        scale:double = 0.965,
    ):Array<string> {
        val finalWords = ArrayList<string>()
        if (getWidth(text, text.length, true, scale) > width) {
            val words = text.split(" ".toRegex()).toTypedArray()
            var currentWord = StringBuilder()
            var lastColorCode = 65535.toChar()
            for (word in words) {
                for (innerIndex in word.toCharArray().indices) {
                    val c = word.toCharArray()[innerIndex]
                    if (c == '\u00a7' && innerIndex < word.toCharArray().size - 1) {
                        lastColorCode = word.toCharArray()[innerIndex + 1]
                    }
                }
                if (getWidth("$currentWord$word ", currentWord.length + word.length + 1, true, scale) < width) {
                    currentWord.append(word).append(" ")
                } else {
                    finalWords.add(currentWord.toString())
                    currentWord = StringBuilder("\u00a7$lastColorCode$word ")
                }
            }
            if (currentWord.isNotEmpty()) {
                if (getWidth(currentWord.toString(), currentWord.length, true, scale) < width) {
                    finalWords.add("\u00a7$lastColorCode$currentWord ")
                } else {
                    finalWords.addAll(formatString(currentWord.toString(), width, scale))
                }
            }
        } else {
            finalWords.add(text)
        }
        return finalWords.toTypedArray()
    }

    fun formatString(
        string:string,
        width:double,
        scale:double,
    ):List<string> {
        val finalWords = ArrayList<string>()
        var currentWord = StringBuilder()
        var lastColorCode = 65535.toChar()
        val chars = string.toCharArray()
        for (index in chars.indices) {
            val c = chars[index]
            if (c == '\u00a7' && index < chars.size - 1) {
                lastColorCode = chars[index + 1]
            }
            if (getWidth(currentWord.toString() + c, currentWord.length + 1, true, scale) < width) {
                currentWord.append(c)
            } else {
                finalWords.add(currentWord.toString())
                currentWord = StringBuilder("\u00a7" + lastColorCode + c)
            }
        }
        if (currentWord.isNotEmpty()) {
            finalWords.add(currentWord.toString())
        }
        return finalWords
    }

    init {
        val bytes = readBytes(inputStream)
        val buffer = BufferUtils.createByteBuffer(bytes.size).put(bytes)
        fonts = arrayOfNulls(5)
        for (i in fonts.indices) {
            (buffer as Buffer).flip()
            fonts[i] = Font(buffer, (18 * (i * 0.5 + 1)).roundToInt())
        }
        val scaleA = floor(0.965 * 10) / 10
        val scaleI:int =
            if (scaleA >= 3) 5 else if (scaleA >= 2.5) 4 else if (scaleA >= 2) 3 else if (scaleA >= 1.5) 2 else 1
        font = fonts[scaleI - 1]
    }

}