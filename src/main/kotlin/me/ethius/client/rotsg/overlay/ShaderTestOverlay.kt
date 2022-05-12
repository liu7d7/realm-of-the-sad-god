package me.ethius.client.rotsg.overlay


import me.ethius.client.renderer.postprocess.Bokeh
import me.ethius.shared.bool
import org.joml.Matrix4dStack

class ShaderTestOverlay:Overlay() {

    override fun shouldClose():bool {
        return false
    }

    override fun render(matrixStack:Matrix4dStack) {
        Bokeh.render(0.9)
    }

}