package me.ethius.client.rotsg.gui

import me.ethius.client.Client
import me.ethius.client.renderer.Mesh
import me.ethius.client.renderer.disableScissor
import me.ethius.client.renderer.enableScissor
import me.ethius.client.rotsg.screen.Screen
import me.ethius.shared.double
import me.ethius.shared.maths.Facing
import me.ethius.shared.safeRange
import me.ethius.shared.string
import org.joml.Matrix4dStack
import org.lwjgl.glfw.GLFW.*

class TextBox {

    var posX0:double = 0.0
    var posY0:double = 0.0
    var posX1:double = 0.0
    var posY1:double = 0.0
    val str = StringBuilder()
    var focused:Boolean = false
    var selectedRange = 0..0
    var lastSelectionDirection = Facing.left

    fun onClick(mouseX:Float, mouseY:Float) {
        focused = containsMouse(mouseX, mouseY)
    }

    fun render(matrix:Matrix4dStack) {
        Client.render.drawRectAlphaWithoutEnding(matrix, posX0, posY0, posX1, posY1, 0x000000, 0.4f)
//        if (selectedRange.first != selectedRange.last) {
//            val start = Client.font.getWidth(str.substring(0, (selectedRange.first + 1).coerceAtMost(str.length)))
//            val end = Client.font.getWidth(str.substring(0, (selectedRange.last + 1).coerceAtMost(str.length))) + if (selectedRange.last == 0) 0.0 else getKern(str[selectedRange.last.coerceAtMost(str.length - 1)].code)
//            Client.render.drawRectAlphaWithoutEnding(matrix, posX0 + start, posY0, posX0 + end, posY1, 0xff000000, 0.6f)
//        }
        val str = str.toString()
        val bl = Client.font.getWidth(str) > posX1 - posX0 - 10
        val flashCursor = (0..500).contains(System.currentTimeMillis() % 1000) && focused
        if (bl) {
            Client.font.drawLeftWithoutEnding(matrix, str, posX1 - 5, posY0 + 2f, 0xffffffff, true)
            if (flashCursor) {
                Client.render.drawRectWithoutEnding(matrix, posX1 - 5f, posY0 + 3f, posX1 - 4f, posY0 + 19f, 0xffffffff)
            }
        } else {
            Client.font.drawWithoutEnding(matrix, str, posX0 + 5, posY0 + 2f, 0xffffffff, true)
            if (flashCursor) {
                val e = when (lastSelectionDirection) {
                    Facing.left -> selectedRange.first
                    Facing.right -> selectedRange.last
                    else -> 0
                }
                val w = Client.font.getWidth(str, e, true)
                Client.render.drawRectWithoutEnding(
                    matrix,
                    posX0 + 5f + w,
                    posY0 + 3f,
                    posX0 + 6f + w,
                    posY0 + 19f,
                    0xffffffff
                )
            }
        }
    }

    fun doRender() {
        enableScissor(posX0, posY0, posX1 - posX0, posY1 - posY0)
        Mesh.drawTriangles()
        disableScissor()
    }

    fun clear() {
        str.clear()
        selectedRange = 0..0
    }

    fun onChar(chars:string) {
        if (focused) {
            str.replace(selectedRange.first, selectedRange.last, chars)
            selectedRange = selectedRange.first + chars.length..selectedRange.first + chars.length
            endIdxChange()
        }
    }

    fun onKey(keyCode:Int) {
        if (glfwGetKey(Client.window.handle, keyCode) != 1 || !focused) {
            return
        }
        when (keyCode) {
            GLFW_KEY_BACKSPACE -> {
                if (str.isNotEmpty()) {
                    val start = if (selectedRange.first == selectedRange.last) {
                        selectedRange.first - 1
                    } else {
                        selectedRange.first
                    }
                    str.delete(start.coerceIn(0..str.length), selectedRange.last)
                    selectedRange = start..start
                }
            }
            GLFW_KEY_DELETE -> {
                if (str.isNotEmpty()) {
                    val end = if (selectedRange.first == selectedRange.last) {
                        selectedRange.last + 1
                    } else {
                        selectedRange.last
                    }
                    str.delete(selectedRange.first, end.coerceIn(0..str.length))
                    selectedRange = selectedRange.first..selectedRange.first
                }
            }
            GLFW_KEY_LEFT -> {
                selectedRange =
                    if (glfwGetKey(Client.window.handle,
                                   GLFW_KEY_LEFT_SHIFT) == 1 || glfwGetKey(Client.window.handle,
                                                                           GLFW_KEY_RIGHT_SHIFT) == 1
                    ) {
                        val first:Int
                        val last:Int
                        when (lastSelectionDirection) {
                            Facing.left -> {
                                first = selectedRange.first - 1
                                last = selectedRange.last
                            }
                            Facing.right -> {
                                first = selectedRange.first
                                last = selectedRange.last - 1
                            }
                            else -> {
                                throw IllegalStateException("lastSelectionDirection is not set, val is $lastSelectionDirection")
                            }
                        }
                        safeRange(first.coerceIn(0..str.length), last.coerceIn(0..str.length)).also {
                            if (it.last >= it.first) {
                                lastSelectionDirection = Facing.left
                            }
                        }
                    } else {
                        val idx = when (lastSelectionDirection) {
                            Facing.left -> selectedRange.first - 1
                            Facing.right -> selectedRange.last - 1
                            else -> {
                                throw IllegalStateException("lastSelectionDirection is not set, val is $lastSelectionDirection")
                            }
                        }
                        safeRange(idx.coerceIn(0..str.length), idx.coerceIn(0..str.length))
                    }
            }
            GLFW_KEY_RIGHT -> {
                selectedRange =
                    if (glfwGetKey(Client.window.handle,
                                   GLFW_KEY_LEFT_SHIFT) == 1 || glfwGetKey(Client.window.handle,
                                                                           GLFW_KEY_RIGHT_SHIFT) == 1
                    ) {
                        val first:Int
                        val last:Int
                        when (lastSelectionDirection) {
                            Facing.left -> {
                                first = selectedRange.first + 1
                                last = selectedRange.last
                            }
                            Facing.right -> {
                                first = selectedRange.first
                                last = selectedRange.last + 1
                            }
                            else -> {
                                throw IllegalStateException("lastSelectionDirection is not set, val is $lastSelectionDirection")
                            }
                        }
                        (first.coerceIn(0..str.length)..last.coerceIn(0..str.length)).let {
                            if (it.first >= it.last) {
                                lastSelectionDirection = Facing.right
                            }
                            safeRange(it.first, it.last)
                        }
                    } else {
                        val idx = when (lastSelectionDirection) {
                            Facing.left -> selectedRange.first + 1
                            Facing.right -> selectedRange.last + 1
                            else -> {
                                throw IllegalStateException("lastSelectionDirection is not set, val is $lastSelectionDirection")
                            }
                        }
                        safeRange(idx.coerceIn(0..str.length), idx.coerceIn(0..str.length))
                    }
            }
            GLFW_KEY_V -> {
                if (Screen.hasControlDown()) {
                    val str = Screen.getClipboardString()
                    this.str.replace(selectedRange.first, selectedRange.last, str)
                    this.selectedRange = selectedRange.first + str.length..selectedRange.first + str.length
                }
            }
            GLFW_KEY_C -> {
                if (Screen.hasControlDown()) {
                    val e = str.substring(selectedRange.first, selectedRange.last)
                    Screen.setClipboardString(e)
                }
            }
            GLFW_KEY_A -> {
                if (Screen.hasControlDown()) {
                    selectedRange = 0..str.length
                }
            }
        }
        endIdxChange()
    }

    private fun endIdxChange() {
        selectedRange = selectedRange.first.coerceIn(0..str.length)..selectedRange.last.coerceIn(0..str.length)
    }

    open fun containsMouse(mouseX:Float, mouseY:Float):Boolean {
        return mouseX in posX0..posX1 && mouseY in posY0..posY1
    }

}