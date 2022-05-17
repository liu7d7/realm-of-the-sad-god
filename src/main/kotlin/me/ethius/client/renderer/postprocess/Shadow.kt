package me.ethius.client.renderer.postprocess

import me.ethius.client.Client
import me.ethius.client.renderer.Framebuffer
import me.ethius.client.renderer.Shader
import me.ethius.client.renderer.Shaders
import me.ethius.client.renderer.bindTexture
import me.ethius.shared.bool
import me.ethius.shared.float
import kotlin.math.floor

object Shadow {

    private lateinit var shadow_blur:Shader

    fun init() {
        shadow_blur = Shaders.shadow_blur
    }

    fun render(fbo:Framebuffer, radius:float, scale:bool = false) {
        if (radius == 0f) {
            return
        }

        // Prepare stuff for rendering
        shadow_blur.bind()
        if (scale) {
            shadow_blur["u_Size", Client.window.scaledWidth] = Client.window.scaledHeight
        } else {
            shadow_blur["u_Size", Client.window.width] = Client.window.height
        }
        shadow_blur["u_Texture"] = 0
        shadow_blur["u_Rad_Div"] = 1.55f

        // Render the blur
        shadow_blur["u_Radius"] = floor(radius)
        bindTexture(fbo.colorAttatchment)
        PostProcessRenderer.render()

    }

}