package me.ethius.client.rotsg.overlay


import me.ethius.shared.bool
import me.ethius.shared.measuringTimeMS
import org.joml.Matrix4dStack

abstract class Overlay {

    protected val timeOpened = measuringTimeMS()

    open var shouldOverlayPauseGame:bool = false

    abstract fun shouldClose():bool

    abstract fun render(matrixStack:Matrix4dStack)

    open fun onClose() {

    }

}