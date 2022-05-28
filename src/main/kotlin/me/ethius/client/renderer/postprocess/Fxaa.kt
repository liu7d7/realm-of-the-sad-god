package me.ethius.client.renderer.postprocess

import me.ethius.client.Client
import me.ethius.client.renderer.ScreenFramebuffer
import me.ethius.client.renderer.Shader
import me.ethius.client.renderer.Shaders
import me.ethius.client.renderer.bindTexture

object Fxaa {

    private lateinit var swap:ScreenFramebuffer

    private lateinit var fxaa:Shader
    private lateinit var blit:Shader

    fun init() {
        fxaa = Shaders.fxaa
        blit = Shaders.blit

        swap = ScreenFramebuffer(true)
    }

    fun render(frameBufferObj:ScreenFramebuffer = Client.frameBufferObj) {

        fxaa.bind()
        fxaa["ProjMat"] = frameBufferObj.projMat
        fxaa["OutSize", swap.width] = swap.height
        fxaa["SpanMax"] = 8.0f
        fxaa["ReduceMul"] = 0.125f
        fxaa["SubPixelShift"] = 0.25f
        swap.bind()
        bindTexture(frameBufferObj.colorAttatchment)
        PostProcessRenderer.renderFs()
        swap.unbind()
        blit.bind()
        blit["ProjMat"] = swap.projMat
        blit["OutSize", frameBufferObj.width] = frameBufferObj.height
        frameBufferObj.bind()
        bindTexture(swap.colorAttatchment)
        PostProcessRenderer.renderFs()

    }

}