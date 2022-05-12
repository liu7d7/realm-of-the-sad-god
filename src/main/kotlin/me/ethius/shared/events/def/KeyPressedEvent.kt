package me.ethius.shared.events.def

import me.ethius.client.Client
import me.ethius.client.rotsg.screen.InGameMenuScreen
import me.ethius.shared.Side
import me.ethius.shared.events.Event
import me.ethius.shared.events.Listen
import me.ethius.shared.int
import org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE
import org.lwjgl.glfw.GLFW.GLFW_PRESS

data class KeyPressedEvent(
    val key:int,
    val action:int,
    val mods:int,
):Event() {

    companion object {
        @Listen(priority = -1)
        fun key(event:KeyPressedEvent) {
            if (event.key == GLFW_KEY_ESCAPE && event.action == GLFW_PRESS && Client.worldInit && Client.screen == null) {
                Client.screen = InGameMenuScreen()
            }
        }

        init {
            if (Side._client) {
                Client.events.register(this)
            }
        }
    }

}