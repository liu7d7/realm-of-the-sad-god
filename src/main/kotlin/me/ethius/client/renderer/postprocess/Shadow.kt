package me.ethius.client.renderer.postprocess

import me.ethius.client.Client
import me.ethius.client.renderer.Framebuffer
import me.ethius.client.renderer.Shader
import me.ethius.client.renderer.Shaders
import me.ethius.client.renderer.bindTexture
import me.ethius.shared.float
import kotlin.math.floor

object Shadow {

    private lateinit var fbo1:Framebuffer

    private lateinit var shadow_blur:Shader

    fun init() {
        fbo1 = Framebuffer(true)
        shadow_blur = Shaders.shadow_blur
    }

    fun render(radius:float, fbo:Framebuffer) {
        if (radius == 0f) {
            return
        }

        // Prepare stuff for rendering
        shadow_blur.bind()
        shadow_blur["u_Size", Client.window.width] = Client.window.height
        shadow_blur["u_Texture"] = 0
        shadow_blur["u_Rad_Div"] = 1.55f

        // Render the blur
        shadow_blur["u_Radius"] = floor(radius)
        bindTexture(fbo.colorAttatchment)
        PostProcessRenderer.render()

    }

}