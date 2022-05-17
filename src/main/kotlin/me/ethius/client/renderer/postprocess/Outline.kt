package me.ethius.client.renderer.postprocess

import me.ethius.client.Client
import me.ethius.client.renderer.Framebuffer
import me.ethius.client.renderer.Shader
import me.ethius.client.renderer.Shaders
import me.ethius.client.renderer.bindTexture
import me.ethius.shared.int

object Outline {

    lateinit var swap:Framebuffer

    lateinit var outline:Shader

    fun init() {
        swap = Framebuffer(true)
        outline = Shaders.outline
    }

    fun render(final:Framebuffer, width:int) {
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