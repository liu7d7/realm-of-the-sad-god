package me.ethius.client.rotsg.screen

import me.ethius.client.Client
import me.ethius.shared.bool
import me.ethius.shared.events.Listen
import me.ethius.shared.events.def.KeyPressedEvent
import me.ethius.shared.events.def.KeyTypedEvent
import me.ethius.shared.events.def.MouseClickedEvent
import me.ethius.shared.events.def.MouseScrolledEvent
import me.ethius.shared.string
import org.joml.Matrix4dStack
import org.lwjgl.glfw.GLFW.*

open class Screen {

    enum class ScreenRenderLayer {
        before, hud, after
    }

    var renderLayer:ScreenRenderLayer = ScreenRenderLayer.after

    open fun render(matrix:Matrix4dStack) {
    }

    protected open fun onMouse(event:MouseClickedEvent) {
    }

    protected open fun onScroll(event:MouseScrolledEvent) {
    }

    protected open fun onKey(event:KeyPressedEvent) {
        if (Client.keyboard.areKeysDown(GLFW_KEY_ESCAPE) && this.shouldCloseOnEsc) {
            this.close()
        }
    }

    protected open fun onChar(event:KeyTypedEvent) {
    }

    fun close() {
        Client.screen = null
        this.onClose()
    }

    open fun onClose() {

    }

    open fun onEnter() {

    }

    open val doesGuiPauseGame:bool
        get() = false
    open val shouldCloseOnEsc:bool
        get() = false

    open fun renderBackground(matrix:Matrix4dStack) {
        // the background
        Client.render.drawGradientRect(matrix,
                                       0.0,
                                       0.0,
                                       Client.window.scaledWidth.toDouble(),
                                       Client.window.scaledHeight.toDouble(),
                                       0xff0a89ff,
                                       0xff1b64a7,
                                       me.ethius.client.renderer.Axis.vertical)
    }

    companion object {

        @Listen
        fun _click(event:MouseClickedEvent) {
            Client.screen?.onMouse(event)
        }

        @Listen
        fun _scroll(event:MouseScrolledEvent) {
            Client.screen?.onScroll(event)
        }

        @Listen(priority = 5)
        fun _key(event:KeyPressedEvent) {
            Client.screen?.onKey(event)
        }

        @Listen(priority = 5)
        fun _char(event:KeyTypedEvent) {
            Client.screen?.onChar(event)
        }

        fun getClipboardString():string {
            return glfwGetClipboardString(Client.window.handle) ?: ""
        }

        fun setClipboardString(copyText:string) {
            if (copyText.isNotBlank()) {
                glfwSetClipboardString(Client.window.handle, copyText)
            }
        }

        fun hasControlDown():bool {
            return Client.keyboard.anyKeysDown(GLFW_KEY_LEFT_CONTROL, GLFW_KEY_RIGHT_CONTROL)
        }

        fun hasShiftDown():bool {
            return Client.keyboard.anyKeysDown(340, 344)
        }

        fun hasAltDown():bool {
            return Client.keyboard.anyKeysDown(342, 346)
        }

        fun isCut(code:Int):bool {
            return code == 88 && hasControlDown() && !hasShiftDown() && !hasAltDown()
        }

        fun isPaste(code:Int):bool {
            return code == 86 && hasControlDown() && !hasShiftDown() && !hasAltDown()
        }

        fun isCopy(code:Int):bool {
            return code == 67 && hasControlDown() && !hasShiftDown() && !hasAltDown()
        }

        fun isSelectAll(code:Int):bool {
            return code == 65 && hasControlDown() && !hasShiftDown() && !hasAltDown()
        }

        init {
            Client.events.register(this)
        }
    }
}