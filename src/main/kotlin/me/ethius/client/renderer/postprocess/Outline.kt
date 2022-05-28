package me.ethius.client.renderer.postprocess

import me.ethius.client.Client
import me.ethius.client.renderer.ScreenFramebuffer
import me.ethius.client.renderer.Shader
import me.ethius.client.renderer.Shaders
import me.ethius.client.renderer.bindTexture
import me.ethius.shared.int

object Outline {

    lateinit var swap:ScreenFramebuffer

    lateinit var outline:Shader

    fun init() {
        swap = ScreenFramebuffer(true)
        outline = Shaders.outline
    }

    fun render(final:ScreenFramebuffer, width:int) {
        swap.clearColorAndDepth()

        outline.bind()
        outline["InSize", final.width] = final.height
        outline["OutSize", swap.width] = swap.height
        outline["ProjMat"] = final.projMat
        outline["width"] = width
        swap.bind()
        bindTexture(final.colorAttatchment, 0)
        PostProcessRenderer.renderFs(0.0,
                                     0.0,
                                     Client.window.scaledWidth.toDouble(),
                                     Client.window.scaledHeight.toDouble())

        final.copyColorFrom(swap)
    }

}