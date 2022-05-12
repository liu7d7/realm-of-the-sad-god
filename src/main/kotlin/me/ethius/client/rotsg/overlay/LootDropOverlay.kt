package me.ethius.client.rotsg.overlay

import me.ethius.client.Client
import me.ethius.client.ext.push
import me.ethius.client.ext.translate
import me.ethius.client.renderer.postprocess.Blur
import me.ethius.client.renderer.postprocess.Bokeh
import me.ethius.shared.*
import me.ethius.shared.maths.Animations
import me.ethius.shared.opti.TexData
import org.joml.Matrix4dStack

class LootDropOverlay(val type:Type):Overlay() {

    override fun render(matrixStack:Matrix4dStack) {
        val width = type.texData.width * 2f
        val height = type.texData.height * 2f
        val x = Client.window.midX - width / 2f
        val time = measuringTimeMS()
        val animation =
            if (time - timeOpened <= 600f) {
                Animations.getDecelerateAnimation(600f, time - timeOpened)
            } else if (time - timeOpened < 900f) {
                1f
            } else {
                Animations.getAccelerateAnimation(600f, time - timeOpened - 900f)
            }
        val targetY =
            if (time - timeOpened < 900.0) {
                Client.window.midY - height * 0.5
            } else {
                Client.window.scaledHeight + 60.0
            }
        val startY =
            if (time - timeOpened < 900f) {
                -30.0
            } else {
                Client.window.midY - height * 0.5
            }
        val renderY:double = lerp(startY, targetY, animation)
        type.drawAction(
            if (time - timeOpened <= 300) {
                Animations.getDecelerateAnimation(300f, time - timeOpened).toDouble()
            } else if (time - timeOpened < 1200) {
                1.0
            } else {
                Animations.getDecelerateAnimation(300f, 300 - (time - timeOpened - 1200f)).toDouble()
            }
        )
        matrixStack.push {
            matrixStack.translate(x, renderY, 1.0) {
                matrixStack.scale(2.0, 2.0, 1.0)
            }
            Client.render.drawTex(type.texData, matrixStack, x, renderY)
        }
    }

    override fun shouldClose():bool {
        return measuringTimeMS() - timeOpened >= 1500
    }

    enum class Type(val texData:TexData, val drawAction:(double) -> void) {
        legendary(TexData.legendary, { Bokeh.render(it) }), primal(TexData.primal, { Blur.render(it) })
    }

}