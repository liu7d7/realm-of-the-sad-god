package me.ethius.client.renderer

import me.ethius.shared.int
import org.lwjgl.opengl.GL32C

enum class DrawMode(val indices:int) {
    line(2),
    triangle(3),
    triangle_fan(3);

    val asGl:int
        get() {
            return when (this) {
                me.ethius.client.renderer.DrawMode.line -> GL32C.GL_LINES
                me.ethius.client.renderer.DrawMode.triangle -> GL32C.GL_TRIANGLES
                me.ethius.client.renderer.DrawMode.triangle_fan -> GL32C.GL_TRIANGLE_FAN
            }
        }
}
