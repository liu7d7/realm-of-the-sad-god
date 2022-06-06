package me.ethius.client.renderer.postprocess

import me.ethius.client.Client
import me.ethius.client.renderer.ScreenFramebuffer
import me.ethius.client.renderer.Shader
import me.ethius.client.renderer.Shaders
import me.ethius.client.renderer.bindTexture
import me.ethius.shared.double
import me.ethius.shared.int

object EntityOutline {

    lateinit var swap:ScreenFramebuffer

    lateinit var entity_outline:Shader

    fun init() {
        swap = ScreenFramebuffer(true)
        entity_outline = Shaders.entity_outline
    }

    fun render(final:ScreenFramebuffer, width:int, x0:double = 0.0, y0:double = 0.0, x1:double = Client.window.scaledWidth, y1:double = Client.window.scaledHeight) {
        swap.clearColorAndDepth()

        entity_outline.bind()
        entity_outline["InSize", final.width] = final.height
        entity_outline["OutSize", swap.width] = swap.height
        entity_outline["ProjMat"] = final.projMat
        entity_outline["width"] = width
        entity_outline["depthThreshold"] = 0.0021f
        swap.bind()
        bindTexture(final.colorAttatchment, 0)
        if (final.useRenBuf) {
            bindTexture(final.depthAttatchment, 1)
            entity_outline["DepthSampler"] = 1
        }
        PostProcessRenderer.renderFs(x0, y0, x1, y1)

        final.copyColorFrom(swap)
    }

}