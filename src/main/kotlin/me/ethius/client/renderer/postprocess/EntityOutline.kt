package me.ethius.client.renderer.postprocess

import me.ethius.client.Client
import me.ethius.client.renderer.Framebuffer
import me.ethius.client.renderer.Shader
import me.ethius.client.renderer.Shaders
import me.ethius.client.renderer.bindTexture
import me.ethius.shared.int

object EntityOutline {

    lateinit var swap:Framebuffer

    lateinit var entity_outline:Shader

    fun init() {
        swap = Framebuffer(true)
        entity_outline = Shaders.entity_outline
    }

    fun render(final:Framebuffer, width:int) {
        swap.clearColorAndDepth()

        entity_outline.bind()
        entity_outline["InSize", final.width] = final.height
        entity_outline["OutSize", swap.width] = swap.height
        entity_outline["ProjMat"] = final.projMat
        entity_outline["width"] = width
        swap.bind()
        bindTexture(final.colorAttatchment, 0)
        if (final.useRenBuf) {
            bindTexture(final.depthAttatchment, 1)
            entity_outline["DepthSampler"] = 1
        }
        PostProcessRenderer.renderFs(0.0,
                                     0.0,
                                     Client.window.scaledWidth.toDouble(),
                                     Client.window.scaledHeight.toDouble())

        final.copyColorFrom(swap)
    }

}