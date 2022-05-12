package me.ethius.client.renderer

import me.ethius.client.main_tex
import me.ethius.client.renderer.Mesh.Companion.drawTriangles
import me.ethius.client.renderer.Mesh.Companion.lines
import me.ethius.client.renderer.Mesh.Companion.triangleFans
import me.ethius.client.renderer.Mesh.Companion.triangles
import me.ethius.shared.*
import me.ethius.shared.opti.TexData
import org.joml.Matrix4d
import org.joml.Matrix4dStack

class Renderer {

    fun setRendering3d(threeD:bool) {
        triangles.depthTest = threeD
        lines.depthTest = threeD
    }

    fun setAlpha(lol:float = 1.0f) {
        lines.alpha = lol
        triangles.alpha = lol
    }

    fun drawTriangle(
        v1:fvec2,
        v2:fvec2,
        v3:fvec2,
        color:long,
    ) {
        triangles.triangle(
            triangles.addVertex(v1.x, v1.y, 0f).tex(373.0, 0.0, main_tex).color(color).float(0).next(),
            triangles.addVertex(v2.x, v2.y, 0f).tex(373.0, 1.0, main_tex).color(color).float(0).next(),
            triangles.addVertex(v3.x, v3.y, 0f).tex(374.0, 0.0, main_tex).color(color).float(0).next()
        )
    }

    fun drawCirclePart(
        matrices:Matrix4dStack,
        x:double,
        y:double,
        fromAngle:float,
        toAngle:float,
        radius:float,
        slices:int,
        color:long,
    ) {
        val increment = (toAngle - fromAngle) / slices
        triangleFans.begin()
        triangleFans.single(triangleFans.addVertex(matrices, x, y, 0.0).tex(373.0, 0.0, main_tex).color(color).float(0).next())
        for (i in 0..slices) {
            val angle = fromAngle + i * increment
            val dX:Double = sin(angle.toDouble())
            val dY:Double = cos(angle.toDouble())
            triangleFans.single(triangleFans.addVertex(matrices, x + dX * radius, y + dY * radius, 0.0)
                                    .tex(374.0, 1.0, main_tex).color(color).float(0).next())
        }
        triangleFans.end().render()
    }

    fun drawShadowOutlineRectWithoutEnding(
        matrix:Matrix4dStack,
        x:double,
        y:double,
        width:double,
        height:double,
        color1:long,
        lineWidth:double,
        top:bool,
        left:bool,
        right:bool,
        bottom:bool,
    ) {

        val x1 = x + width
        val y1 = y + height

        // lt corner
        if (left && top) {
            drawSeparateGradientRectWithoutEnding(matrix,
                                                  x - lineWidth,
                                                  y - lineWidth,
                                                  x,
                                                  y,
                                                  0x00000000,
                                                  0x00000000,
                                                  color1,
                                                  0x00000000)
        }
        // rt corner
        if (right && top) {
            drawSeparateGradientRectWithoutEnding(matrix,
                                                  x1,
                                                  y - lineWidth,
                                                  x1 + lineWidth,
                                                  y,
                                                  0x00000000,
                                                  0x00000000,
                                                  0x00000000,
                                                  color1)
        }
        // rb corner
        if (right && bottom) {
            drawSeparateGradientRectWithoutEnding(matrix,
                                                  x1,
                                                  y1,
                                                  x1 + lineWidth,
                                                  y1 + lineWidth,
                                                  color1,
                                                  0x00000000,
                                                  0x00000000,
                                                  0x00000000)
        }
        // lb corner
        if (left && bottom) {
            drawSeparateGradientRectWithoutEnding(matrix,
                                                  x - lineWidth,
                                                  y1,
                                                  x,
                                                  y1 + lineWidth,
                                                  0x00000000,
                                                  color1,
                                                  0x00000000,
                                                  0x00000000)
        }
        // left
        if (left) {
            drawSeparateGradientRectWithoutEnding(matrix,
                                                  x - lineWidth,
                                                  y,
                                                  x,
                                                  y1,
                                                  0x00000000,
                                                  color1,
                                                  color1,
                                                  0x00000000)
        }
        // right
        if (right) {
            drawSeparateGradientRectWithoutEnding(matrix,
                                                  x1,
                                                  y,
                                                  x1 + lineWidth,
                                                  y1,
                                                  color1,
                                                  0x00000000,
                                                  0x00000000,
                                                  color1)
        }
        // top
        if (top) {
            drawSeparateGradientRectWithoutEnding(matrix,
                                                  x,
                                                  y - lineWidth,
                                                  x1,
                                                  y,
                                                  0x00000000,
                                                  0x00000000,
                                                  color1,
                                                  color1)
        }
        // bottom
        if (bottom) {
            drawSeparateGradientRectWithoutEnding(matrix,
                                                  x,
                                                  y1,
                                                  x1,
                                                  y1 + lineWidth,
                                                  color1,
                                                  color1,
                                                  0x00000000,
                                                  0x00000000)
        }
    }

    fun drawRect(
        matrices:Matrix4dStack,
        x:double,
        y:double,
        x1:double,
        y1:double,
        color:long,
    ) {
        if (!triangles.building)
            triangles.begin()
        drawRectWithoutEnding(matrices, x, y, x1, y1, color)
        drawTriangles()
    }

    fun drawRectWithoutEnding(
        matrices:Matrix4dStack,
        x:double,
        y:double,
        x1:double,
        y1:double,
        color:long,
    ) {
        triangles.quad(
            triangles.addVertex(matrices, x, y).tex(TexData.rect.u, TexData.rect.v, main_tex).color(color).float(0)
                .next(),
            triangles.addVertex(matrices, x1, y).tex(TexData.rect.u + TexData.rect.width, TexData.rect.v, main_tex)
                .color(color).float(0).next(),
            triangles.addVertex(matrices, x1, y1)
                .tex(TexData.rect.u + TexData.rect.width, TexData.rect.v + TexData.rect.height, main_tex).color(color)
                .float(0).next(),
            triangles.addVertex(matrices, x, y1).tex(TexData.rect.u, TexData.rect.v + TexData.rect.height, main_tex)
                .color(color).float(0).next()
        )
    }

    fun drawGradientRectWithoutEnding(
        matrices:Matrix4dStack,
        x:double,
        y:double,
        x1:double,
        y1:double,
        color:long,
        color1:long,
        direction:Axis,
    ) {
        triangles.quad(
            triangles.addVertex(matrices, x, y).tex(TexData.rect.u, TexData.rect.v, main_tex).color(color).float(0)
                .next(),
            triangles.addVertex(matrices, x1, y).tex(TexData.rect.u + TexData.rect.width, TexData.rect.v, main_tex)
                .color(if (direction == Axis.horizontal) color1 else color).float(0).next(),
            triangles.addVertex(matrices, x1, y1)
                .tex(TexData.rect.u + TexData.rect.width, TexData.rect.v + TexData.rect.height, main_tex).color(color1)
                .float(0).next(),
            triangles.addVertex(matrices, x, y1).tex(TexData.rect.u, TexData.rect.v + TexData.rect.height, main_tex)
                .color(if (direction == Axis.horizontal) color else color1).float(0).next()
        )
    }

    fun drawGradientRect(
        matrices:Matrix4dStack,
        x:double,
        y:double,
        x1:double,
        y1:double,
        color:long,
        color1:long,
        direction:Axis,
    ) {
        if (!triangles.building)
            triangles.begin()
        drawGradientRectWithoutEnding(matrices, x, y, x1, y1, color, color1, direction)
        drawTriangles()
    }

    fun drawSeparateGradientRectWithoutEnding(
        matrices:Matrix4dStack,
        x:double,
        y:double,
        x1:double,
        y1:double,
        cxy:long,
        cx1y:long,
        cx1y1:long,
        cxy1:long,
    ) {
        triangles.quad(
            triangles.addVertex(matrices, x, y).tex(TexData.rect.u, TexData.rect.v, main_tex).color(cxy).float(0).next(),
            triangles.addVertex(matrices, x1, y).tex(TexData.rect.u + TexData.rect.width, TexData.rect.v, main_tex)
                .color(cx1y).float(0).next(),
            triangles.addVertex(matrices, x1, y1)
                .tex(TexData.rect.u + TexData.rect.width, TexData.rect.v + TexData.rect.height, main_tex).color(cx1y1)
                .float(0).next(),
            triangles.addVertex(matrices, x, y1).tex(TexData.rect.u, TexData.rect.v + TexData.rect.height, main_tex)
                .color(cxy1).float(0).next()
        )
    }

    fun drawRectAlpha(
        matrices:Matrix4dStack,
        x:double,
        y:double,
        x1:double,
        y1:double,
        color:long,
        alpha:float,
    ) {
        val red = ((color shr 16 and 0xFF) / 255f)
        val green = ((color shr 8 and 0xFF) / 255f)
        val blue = ((color and 0xFF) / 255f)
        if (!triangles.building)
            triangles.begin()
        triangles.quad(
            triangles.addVertex(matrices, x, y).tex(TexData.rect.u, TexData.rect.v, main_tex)
                .color(alpha, red, green, blue)
                .float(0).next(),
            triangles.addVertex(matrices, x1, y).tex(TexData.rect.u + TexData.rect.width, TexData.rect.v, main_tex)
                .color(alpha, red, green, blue).float(0).next(),
            triangles.addVertex(matrices, x1, y1)
                .tex(TexData.rect.u + TexData.rect.width, TexData.rect.v + TexData.rect.height, main_tex)
                .color(alpha, red, green, blue).float(0).next(),
            triangles.addVertex(matrices, x, y1).tex(TexData.rect.u, TexData.rect.v + TexData.rect.height, main_tex)
                .color(alpha, red, green, blue).float(0).next()
        )
        drawTriangles()
    }

    fun drawRectAlphaWithoutEnding(
        matrices:Matrix4dStack,
        x:double,
        y:double,
        x1:double,
        y1:double,
        color:long,
        alpha:float,
    ) {
        val red = ((color shr 16 and 0xFF) / 255f)
        val green = ((color shr 8 and 0xFF) / 255f)
        val blue = ((color and 0xFF) / 255f)
        triangles.quad(
            triangles.addVertex(matrices, x, y).tex(TexData.rect.u, TexData.rect.v, main_tex)
                .color(alpha, red, green, blue)
                .float(0).next(),
            triangles.addVertex(matrices, x1, y).tex(TexData.rect.u + TexData.rect.width, TexData.rect.v, main_tex)
                .color(alpha, red, green, blue).float(0).next(),
            triangles.addVertex(matrices, x1, y1)
                .tex(TexData.rect.u + TexData.rect.width, TexData.rect.v + TexData.rect.height, main_tex)
                .color(alpha, red, green, blue).float(0).next(),
            triangles.addVertex(matrices, x, y1).tex(TexData.rect.u, TexData.rect.v + TexData.rect.height, main_tex)
                .color(alpha, red, green, blue).float(0).next()
        )
    }

    fun drawOutlineRect(
        matrices:Matrix4dStack,
        x:double,
        y:double,
        width:double,
        height:double,
        color:long,
        lineWidth:double,
        top:bool = true,
        left:bool = true,
        right:bool = true,
        bottom:bool = true,
    ) {
        val mx = x + width
        val my = y + height
        if (top)
            drawRect(matrices, x, y, mx, y + lineWidth, color)
        if (left)
            drawRect(matrices, x, y + lineWidth, x + lineWidth, my - lineWidth, color)
        if (right)
            drawRect(matrices, mx - lineWidth, y + lineWidth, mx, my - lineWidth, color)
        if (bottom)
            drawRect(matrices, x, my - lineWidth, mx, my, color)
    }

    fun drawStylizedRect(
        matrices:Matrix4dStack,
        x:double,
        y:double,
        width:double,
        height:double,
        lineColor:long,
        lineWidth:double,
        fill:bool = false, fillColor:long = 0xffffffff,
    ) {
        val mx = x + width
        val my = y + height
        /*
         *  ********
         * *       *
         * *       *
         * *       *
         * *       *
         * ********
         */
        if (fill) {
            drawRectWithoutEnding(matrices, x, y, mx, my, fillColor)
        }
        drawRectWithoutEnding(matrices, x, y - lineWidth, mx + lineWidth, y, fillColor)
        drawRectWithoutEnding(matrices, x - lineWidth, y, x, my + lineWidth, fillColor)
        drawRectWithoutEnding(matrices, mx, y, mx + lineWidth, my, fillColor)
        drawRectWithoutEnding(matrices, x, my, mx, my + lineWidth, fillColor)

        drawRectWithoutEnding(matrices, x, y + lineWidth * 2, mx, y + lineWidth * 7, lineColor)
    }

    fun drawTexCenteredVerticalWithoutEnding(
        data:TexData,
        matrices:Matrix4dStack,
        x:double,
        y:double,
        renderWidth:double = data.width,
        renderHeight:double = data.height,
        color:long = 0xffffffff,
    ) {
        triangles.quad(
            triangles.addVertex(matrices, x - renderWidth * 0.5 + data.pivotX, y, 0.0)
                .tex(data.u, data.v + renderHeight, main_tex).color(color).float(0).next(),
            triangles.addVertex(matrices, x + renderWidth - renderWidth * 0.5 + data.pivotX, y, 0.0)
                .tex(data.u + renderWidth, data.v + renderHeight, main_tex).color(color).float(0).next(),
            triangles.addVertex(matrices, x + renderWidth - renderWidth * 0.5 + data.pivotX, y, renderHeight)
                .tex(data.u + renderWidth, data.v, main_tex).color(color).float(0).next(),
            triangles.addVertex(matrices, x - renderWidth * 0.5 + data.pivotX, y, renderHeight)
                .tex(data.u, data.v, main_tex).color(color).float(0).next()
        )
    }

    fun drawTexCenteredVerticalWindyWithoutEnding(
        data:TexData,
        matrices:Matrix4dStack,
        x:double,
        y:double,
        renderWidth:double,
        renderHeight:double,
        color:long = 0xffffffff,
        windX:double,
    ) {
        triangles.quad(
            triangles.addVertex(matrices, x - renderWidth * 0.5 + data.pivotX, y, 0.0)
                .tex(data.u, data.v + renderHeight, main_tex).color(color).float(0).next(),
            triangles.addVertex(matrices, x + renderWidth - renderWidth * 0.5 + data.pivotX, y, 0.0)
                .tex(data.u + renderWidth, data.v + renderHeight, main_tex).color(color).float(0).next(),
            triangles.addVertex(matrices, x + renderWidth - renderWidth * 0.5 + windX + data.pivotX, y, renderHeight)
                .tex(data.u + renderWidth, data.v, main_tex).color(color).float(0).next(),
            triangles.addVertex(matrices, x - renderWidth * 0.5 + windX + data.pivotX, y, renderHeight)
                .tex(data.u, data.v, main_tex).color(color).float(0).next()
        )
    }

    fun drawOutlineRectWithoutEnding(
        matrices:Matrix4dStack,
        x:double,
        y:double,
        width:double,
        height:double,
        color:long,
        lineWidth:double,
        top:bool = true,
        left:bool = true,
        right:bool = true,
        bottom:bool = true,
    ) {
        val mx = x + width
        val my = y + height
        if (top)
            drawRectWithoutEnding(matrices, x, y, mx, y + lineWidth, color)
        if (left)
            drawRectWithoutEnding(matrices, x, y, x + lineWidth, my, color)
        if (right)
            drawRectWithoutEnding(matrices, mx - lineWidth, y, mx, my, color)
        if (bottom)
            drawRectWithoutEnding(matrices, x, my - lineWidth, mx, my, color)
    }

    fun drawLine(
        matrices:Matrix4dStack,
        x1:double,
        y1:double,
        x2:double,
        y2:double,
        color:long,
    ) {
        val bl = lines.building
        if (!bl) {
            lines.begin()
        }
        lines.line(
            lines.addVertex(matrices, x1, y1).color(color).next(),
            lines.addVertex(matrices, x2, y2).color(color).next()
        )
        if (!bl) {
            lines.end()
            lines.render()
        }
    }

    fun drawTex(
        data:TexData,
        matrices:Matrix4dStack,
        x:double,
        y:double,
        windX:double = 0.0,
    ) {

        dessinerTexAvecWidthEtHeight(matrices,
                                     x,
                                     y,
                                     data.width,
                                     data.height,
                                     data.u,
                                     data.v,
                                     data.width,
                                     data.height,
                                     main_tex.width.toDouble(),
                                     main_tex.height.toDouble(),
                                     windX)
    }

    fun drawTex(
        data:TexData,
        matrices:Matrix4dStack,
        x:double,
        y:double,
        width:double,
        height:double,
    ) {

        dessinerTexAvecWidthEtHeight(matrices,
                                     x,
                                     y,
                                     width,
                                     height,
                                     data.u,
                                     data.v,
                                     data.width,
                                     data.height,
                                     main_tex.width.toDouble(),
                                     main_tex.height.toDouble(),
                                     0.0)
    }

    fun drawTexWithoutEnding(
        data:TexData,
        matrices:Matrix4dStack,
        x:double,
        y:double,
        windX:double = 0.0,
        color:long = 0xffffffff,
        renderWidth:double = data.width,
        renderHeight:double = data.height,
    ) {
        val x1 = x + renderWidth
        val y1 = y + renderHeight
        triangles.quad(
            triangles.addVertex(matrices, x, y1)
                .tex(data.u / main_tex.width.toDouble(), (data.v + data.height) / main_tex.height.toDouble())
                .color(color).float(0).next(),
            triangles.addVertex(matrices, x1, y1)
                .tex((data.u + data.width) / main_tex.width.toDouble(),
                     (data.v + data.height) / main_tex.height.toDouble())
                .color(color).float(0).next(),
            triangles.addVertex(matrices, x1 + windX, y)
                .tex((data.u + data.width) / main_tex.width.toDouble(), data.v / main_tex.height.toDouble())
                .color(color).float(0).next(),
            triangles.addVertex(matrices, x + windX, y)
                .tex(data.u / main_tex.width.toDouble(), data.v / main_tex.height.toDouble())
                .color(color).float(0).next()
        )
    }

    fun drawCenteredTexWithoutEnding(
        data:TexData,
        matrices:Matrix4dStack,
        x:double,
        y:double,
        windX:double = 0.0,
        color:long = 0xffffffff,
    ) {
        val x = x - data.width * 0.5 + data.pivotX
        val y = y - data.height * 0.5 + data.pivotY
        triangles.quad(
            triangles.addVertex(matrices, x, y + data.height)
                .tex(data.u / main_tex.width.toDouble(), (data.v + data.height) / main_tex.height.toDouble())
                .color(color).float(0).next(),
            triangles.addVertex(matrices, x + data.width, y + data.height)
                .tex((data.u + data.width) / main_tex.width.toDouble(),
                     (data.v + data.height) / main_tex.height.toDouble())
                .color(color).float(0).next(),
            triangles.addVertex(matrices, x + data.width + windX, y)
                .tex((data.u + data.width) / main_tex.width.toDouble(), data.v / main_tex.height.toDouble())
                .color(color).float(0).next(),
            triangles.addVertex(matrices, x + windX, y)
                .tex(data.u / main_tex.width.toDouble(), data.v / main_tex.height.toDouble())
                .color(color).float(0).next()
        )
    }

    fun drawTexCenteredWithoutEnding(
        data:TexData,
        matrices:Matrix4dStack,
        x:double,
        y:double,
        windX:double = 0.0,
        color:long = 0xffffffff,
        renderWidth:double = data.width,
        renderHeight:double = data.height,
        pivotX:double = data.pivotX,
        pivotY:double = data.pivotY,
    ) {
        triangles.quad(
            triangles.addVertex(matrices, x - renderWidth * 0.5 + pivotX, y + renderHeight * 0.5 + pivotY)
                .tex(data.u / main_tex.width.toDouble(), (data.v + renderHeight) / main_tex.height.toDouble())
                .color(color).float(0).next(),
            triangles.addVertex(matrices, x + renderWidth * 0.5 + pivotX, y + renderHeight * 0.5 + pivotY)
                .tex((data.u + renderWidth) / main_tex.width.toDouble(),
                     (data.v + renderHeight) / main_tex.height.toDouble())
                .color(color).float(0).next(),
            triangles.addVertex(matrices, x + renderWidth * 0.5 + pivotX + windX, y - renderHeight * 0.5 + pivotY)
                .tex((data.u + renderWidth) / main_tex.width.toDouble(), data.v / main_tex.height.toDouble())
                .color(color).float(0).next(),
            triangles.addVertex(matrices, x - renderWidth * 0.5 + pivotX + windX, y - renderHeight * 0.5 + pivotY)
                .tex(data.u / main_tex.width.toDouble(), data.v / main_tex.height.toDouble())
                .color(color).float(0).next()
        )
    }

    private fun dessinerTexAvecWidthEtHeight(
        matrices:Matrix4dStack,
        x:double,
        y:double,
        width:double,
        height:double,
        u:double,
        v:double,
        regionWidth:double,
        regionHeight:double,
        textureWidth:double,
        textureHeight:double,
        windX:double,
    ) {
        texQuad(
            matrices,
            x,
            x + width,
            y,
            y + height,
            u,
            v,
            textureWidth,
            textureHeight,
            regionWidth,
            regionHeight,
            windX,
        )
    }

    private fun texQuad(
        matrices:Matrix4d,
        x0:double,
        x1:double,
        y0:double,
        y1:double,
        u:double,
        v:double,
        textureWidth:double,
        textureHeight:double,
        regionWidth:double,
        regionHeight:double,
        windX:double,
    ) {
        triangles.begin()
        triangles.quad(
            triangles.addVertex(matrices, x0, y1).tex((u + 0.0f) / textureWidth, (v + regionHeight) / textureHeight)
                .color(0xffffffff).float(0).next(),
            triangles.addVertex(matrices, x1, y1)
                .tex((u + regionWidth) / textureWidth, (v + regionHeight) / textureHeight)
                .color(0xffffffff).float(0).next(),
            triangles.addVertex(matrices, x1 + windX, y0)
                .tex((u + regionWidth) / textureWidth, (v + 0.0f) / textureHeight)
                .color(0xffffffff).float(0).next(),
            triangles.addVertex(matrices, x0 + windX, y0).tex((u + 0.0f) / textureWidth, (v + 0.0f) / textureHeight)
                .color(0xffffffff).float(0).next()
        )
        drawTriangles()
    }

}