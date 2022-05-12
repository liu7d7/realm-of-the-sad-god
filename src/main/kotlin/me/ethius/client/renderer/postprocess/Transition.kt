package me.ethius.client.renderer.postprocess

import me.ethius.client.Client
import me.ethius.client.renderer.Shader
import me.ethius.client.renderer.Shaders
import me.ethius.client.renderer.bindTexture
import me.ethius.shared.bool
import me.ethius.shared.float

object Transition {

    lateinit var transition:Shader

    fun init() {
        transition = Shaders.transition
    }

    fun render(time:float, duration:float, reverse:bool) {
        transition.bind()
        transition["progress"] = time / duration
        transition["reverse"] = reverse
        transition["diamondPixelSize"] = 20f
        transition["Size", Client.window.scaledWidth] = Client.window.scaledHeight
        bindTexture(Client.frameBufferObj.colorAttatchment)
        PostProcessRenderer.renderFs(0.0, 0.0, Client.window.scaledWidth.toDouble(), Client.window.scaledHeight.toDouble())
        transition.unbind()
    }

}