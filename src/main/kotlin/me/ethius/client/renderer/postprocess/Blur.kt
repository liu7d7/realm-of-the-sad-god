package me.ethius.client.renderer.postprocess

import me.ethius.client.Client
import me.ethius.client.renderer.ScreenFramebuffer
import me.ethius.client.renderer.Shader
import me.ethius.client.renderer.Shaders
import me.ethius.client.renderer.bindTexture
import me.ethius.shared.double
import me.ethius.shared.int
import kotlin.math.floor

object Blur {

    private lateinit var fbo1:ScreenFramebuffer
    private lateinit var fbo2:ScreenFramebuffer

    private lateinit var blur:Shader

    fun init() {
        blur = Shaders.blur
        fbo1 = ScreenFramebuffer(true)
        fbo2 = ScreenFramebuffer(true)
    }

    fun render(progress:double) {

        if (progress == 0.0) {
            return
        }

        // Prepare stuff for rendering
        val sourceTexture:int = Client.frameBufferObj.colorAttatchment
        blur.bind()
        blur["u_Size", Client.window.width] = Client.window.height
        blur["u_Texture"] = 0

        // Render the blur
        blur["u_Radius"] = floor(25f * progress)
        fbo1.bind()
        bindTexture(sourceTexture)
        blur["u_Direction", 1.0f] = 0.0f
        PostProcessRenderer.render()
        fbo2.bind()
        bindTexture(fbo1.colorAttatchment)
        blur["u_Direction", 0.0f] = 1.0f
        PostProcessRenderer.render()
        fbo1.bind()
        bindTexture(fbo2.colorAttatchment)
        blur["u_Direction", 1.0f] = 0.0f
        PostProcessRenderer.render()
        fbo2.unbind()
        Client.frameBufferObj.bind()
        bindTexture(fbo1.colorAttatchment)
        blur["u_Direction", 0.0f] = 1.0f
        PostProcessRenderer.render()
    }

}

