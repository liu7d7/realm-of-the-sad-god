package me.ethius.client.rotsg.overlay

import me.ethius.client.renderer.postprocess.Transition
import me.ethius.shared.bool
import me.ethius.shared.float
import me.ethius.shared.measuringTimeMS
import org.joml.Matrix4dStack

class TransitionOverlay(private val duration:float, private val rvs:bool, private val backAndForth:bool, private val pauseTime:float = 0.0f):Overlay() {
    override fun shouldClose():bool {
        return measuringTimeMS() - timeOpened > duration + pauseTime
    }

    override fun render(matrixStack:Matrix4dStack) {
        val time = measuringTimeMS() - timeOpened
        if (backAndForth) {
            if (time < duration / 2f) {
                Transition.render(time, duration / 2f, !rvs)
            } else if (time < duration / 2f + pauseTime) {
                Transition.render(1f, 1f, !rvs)
            } else {
                Transition.render(time - duration / 2f - pauseTime, duration / 2f, rvs)
            }
        } else {
            Transition.render(time, duration, rvs)
        }
    }
}