package me.ethius.client.renderer.font

import me.ethius.client.renderer.Mesh
import me.ethius.client.renderer.Texture
import me.ethius.shared.*
import me.ethius.shared.rotsg.data.Formatting
import org.joml.Matrix4dStack
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.ARBTextureRG.GL_R8
import org.lwjgl.opengl.GL11.*
import org.lwjgl.stb.STBTTFontinfo
import org.lwjgl.stb.STBTTPackContext
import org.lwjgl.stb.STBTTPackedchar
import org.lwjgl.stb.STBTruetype.*
import org.lwjgl.system.MemoryStack
import java.nio.ByteBuffer
import kotlin.math.max

private const val ipw = 1f / 2048f
private const val iph = 1f / 2048f

class Font(buffer:ByteBuffer?, val height:int) {

    var texture:Texture
    private val scale:double
    private var ascent = 0.0
    private val charData:Array<CharData?>

    fun getWidth(
        string:string,
        length:int,
        scale:double,
        kern:(int) -> double = { getKern(it) }
    ):double {
        if (string.isEmpty()) return 0.0
        if (string == "") return 0.0
        var width = 0.0
        for (i in 0 until length) {
            val c1 = string[i]
            val prev = string[(max(i - 1, 0))]
            if (c1 == '§' || prev == '§') continue
            var cp = c1.code
            if (cp < 32 || cp > 128) cp = 32
            val c = charData[cp - 32]
            width += (c!!.xAdvance + kern(cp)) * scale
        }
        return width
    }

    fun getHeight():double = height.toDouble()

    fun draw(
        mesh:Mesh,
        matrices:Matrix4dStack,
        string:string,
        x:double,
        y:double,
        color:long,
        scale:double,
        shadow:bool = false,
        kern:(int) -> double,
    ):double {
        var drawX = x
        var drawY = y
        drawY += ascent * this.scale * scale
        val length:int = string.length
        var alpha = (color shr 24 and 0xFF) / 255.0f
        var red = (color shr 16 and 0xFF) / 255.0f
        var green = (color shr 8 and 0xFF) / 255.0f
        var blue = (color and 0xFF) / 255.0f
        for (i in 0 until length) {
            var cp = string[i].code
            val previous = if (i > 0) string[i - 1] else '.'
            if (previous == '§') continue
            if (cp.toChar() == '§') {
                val fortnite = string.lowercase()[i + 1]
                val formatting = Formatting.byChar(fortnite)
                if (formatting != null) {
                    val txtColor = formatting.color(color)
                    alpha = (color shr 24 and 0xFF) / 255.0f
                    red = (txtColor shr 16 and 0xFF) / 255.0f
                    green = (txtColor shr 8 and 0xFF) / 255.0f
                    blue = (txtColor and 0xFF) / 255.0f
                }
                continue
            }
            if (cp < 32 || cp > 256) cp = 32
            val c = charData[cp - 32]!!
            if (shadow) {
                mesh.quad(
                    mesh.addVertex(matrices,
                                   (drawX + scale + c.x0 * scale),
                                   (drawY + scale + c.y0 * scale)).tex(c.u0, c.v0)
                        .color(alpha, red * 0.125f, green * 0.125f, blue * 0.125f).float(1).next(),
                    mesh.addVertex(matrices,
                                   (drawX + scale + c.x0 * scale),
                                   (drawY + scale + c.y1 * scale)).tex(c.u0, c.v1)
                        .color(alpha, red * 0.125f, green * 0.125f, blue * 0.125f).float(1).next(),
                    mesh.addVertex(matrices,
                                   (drawX + scale + c.x1 * scale),
                                   (drawY + scale + c.y1 * scale)).tex(c.u1, c.v1)
                        .color(alpha, red * 0.125f, green * 0.125f, blue * 0.125f).float(1).next(),
                    mesh.addVertex(matrices,
                                   (drawX + scale + c.x1 * scale),
                                   (drawY + scale + c.y0 * scale)).tex(c.u1, c.v0)
                        .color(alpha, red * 0.125f, green * 0.125f, blue * 0.125f).float(1).next()
                )
            }
            mesh.quad(
                mesh.addVertex(matrices, (drawX + c.x0 * scale), (drawY + c.y0 * scale))
                    .tex(c.u0, c.v0)
                    .color(alpha, red, green, blue).float(1).next(),
                mesh.addVertex(matrices, (drawX + c.x0 * scale), (drawY + c.y1 * scale))
                    .tex(c.u0, c.v1)
                    .color(alpha, red, green, blue).float(1).next(),
                mesh.addVertex(matrices, (drawX + c.x1 * scale), (drawY + c.y1 * scale))
                    .tex(c.u1, c.v1)
                    .color(alpha, red, green, blue).float(1).next(),
                mesh.addVertex(matrices, (drawX + c.x1 * scale), (drawY + c.y0 * scale))
                    .tex(c.u1, c.v0)
                    .color(alpha, red, green, blue).float(1).next()
            )
            drawX += (c.xAdvance + kern(cp)) * scale
        }
        return drawX
    }

    init {
        val fontInfo = STBTTFontinfo.create()
        stbtt_InitFont(fontInfo, buffer!!)
        charData = arrayOfNulls(256)
        val cdata = STBTTPackedchar.create(charData.size)
        val bitmap = BufferUtils.createByteBuffer(2048 * 2048)
        val packContext = STBTTPackContext.create()
        stbtt_PackBegin(packContext, bitmap, 2048, 2048, 0, 1)
        stbtt_PackSetOversampling(packContext, 8, 8)
        stbtt_PackFontRange(packContext, buffer, 0, height.toFloat(), 32, cdata)
        stbtt_PackEnd(packContext)
        texture = Texture.createTextureMipped(2048, 2048, bitmap, GL_NEAREST_MIPMAP_NEAREST, GL_LINEAR, GL_RED, GL_R8)
        scale = stbtt_ScaleForPixelHeight(fontInfo, height.toFloat()).toDouble()
        MemoryStack.stackPush().use { stack ->
            val ascent = stack.mallocInt(1)
            stbtt_GetFontVMetrics(fontInfo, ascent, null, null)
            this.ascent = ascent[0].toDouble()
        }
        for (i in charData.indices) {
            val packedChar = cdata[i]
            charData[i] = CharData(
                packedChar.xoff().toDouble(),
                packedChar.yoff().toDouble(),
                packedChar.xoff2().toDouble(),
                packedChar.yoff2().toDouble(),
                packedChar.x0() * ipw.toDouble(),
                packedChar.y0() * iph.toDouble(),
                packedChar.x1() * ipw.toDouble(),
                packedChar.y1() * iph.toDouble(),
                packedChar.xadvance().toDouble()
            )
        }
    }
}