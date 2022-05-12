package me.ethius.client.rotsg.gui

import me.ethius.client.Client
import me.ethius.shared.bool
import me.ethius.shared.double
import me.ethius.shared.events.def.KeyPressedEvent
import me.ethius.shared.events.def.KeyTypedEvent
import me.ethius.shared.events.def.MouseClickedEvent
import me.ethius.shared.string
import me.ethius.shared.void
import org.joml.Matrix4dStack
import org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT
import org.lwjgl.glfw.GLFW.GLFW_PRESS

class Button(
    var centerX:double,
    var centerY:double,
    var width:double,
    var height:double,
) {

    private var text = { "" }
    private var left = { _:Button -> }
    private var right = { _:Button -> }
    private var textAction = { _:string, _:Button -> }
    private var keyAction = { _:KeyPressedEvent, _:Button -> }
    private var textAlignment = TextAlignment.center
    private val data = mutableMapOf<string, Any>()

    val minX
        get() = centerX - width / 2.0
    val maxX
        get() = centerX + width / 2.0
    val minY
        get() = centerY - height / 2.0
    val maxY
        get() = centerY + height / 2.0

    private var additionalRendering = { _:Matrix4dStack, _:Button -> }

    constructor():this(-5000.0, -5000.0, 0.0, 0.0)

    // push data to the data map //
    fun pushData(id:string, data:Any):Button {
        // set the data //
        this.data[id] = data
        // return this //
        return this
    }

    // get data from the data map //
    fun <T> getData(id:string):T {
        // return the data, if it is null throw an exception //
        return data[id] as T ?: throw IllegalStateException("$id is not a valid identifier!")
    }

    fun setCenterX(centerX:double):Button {
        this.centerX = centerX
        return this
    }

    fun setCenterY(centerY:double):Button {
        this.centerY = centerY
        return this
    }

    fun setX(x:double):Button {
        centerX = x + width / 2
        return this
    }

    fun setY(y:double):Button {
        centerY = y + height / 2
        return this
    }

    fun setWidth(width:double):Button {
        this.width = width
        return this
    }

    fun setHeight(height:double):Button {
        this.height = height
        return this
    }

    fun setText(text:() -> string):Button {
        this.text = text
        return this
    }

    fun setOnLeft(action:(Button) -> void):Button {
        this.left = action
        return this
    }

    fun setOnRight(action:(Button) -> void):Button {
        this.right = action
        return this
    }

    fun setTextAction(action:(string, Button) -> void):Button {
        this.textAction = action
        return this
    }

    fun setKeyAction(action:(KeyPressedEvent, Button) -> void):Button {
        this.keyAction = action
        return this
    }

    fun setTextAlignment(textAlignment:TextAlignment):Button {
        this.textAlignment = textAlignment
        return this
    }

    fun setAdditionalRendering(additionalRendering:(Matrix4dStack, Button) -> void):Button {
        this.additionalRendering = additionalRendering
        return this
    }

    fun render(matrix:Matrix4dStack) {
        Client.render.drawRectAlpha(matrix,
                                    centerX - width / 2f,
                                    centerY - height / 2f,
                                    centerX + width / 2f,
                                    centerY + height / 2f,
                                    dul, 0.5f)
        when (this.textAlignment) {
            TextAlignment.center -> Client.font.drawCenteredString(matrix, text(), centerX, centerY, 0xffffffff, true)
            TextAlignment.left -> Client.font.draw(matrix,
                                                   text(),
                                                   centerX - width / 2f + 2f,
                                                   centerY - Client.font.getHeight(true) / 2f,
                                                   0xffffffff,
                                                   true)
            TextAlignment.right -> Client.font.drawLeft(matrix,
                                                        text(),
                                                        centerX + width / 2f - 2f,
                                                        centerY - Client.font.getHeight(true) / 2f,
                                                        0xffffffff,
                                                        true)
        }
        this.additionalRendering(matrix, this)
    }

    fun renderWithoutEnding(matrix:Matrix4dStack) {
        Client.render.drawRectAlphaWithoutEnding(matrix,
                                                 centerX - width / 2f,
                                                 centerY - height / 2f,
                                                 centerX + width / 2f,
                                                 centerY + height / 2f,
                                                 dul, 0.5f)
        when (this.textAlignment) {
            TextAlignment.center -> Client.font.drawCenteredStringWithoutEnding(matrix,
                                                                                text(),
                                                                                centerX,
                                                                                centerY,
                                                                                0xffffffff,
                                                                                true)
            TextAlignment.left -> Client.font.drawWithoutEnding(matrix,
                                                                text(),
                                                                centerX - width / 2f + 2f,
                                                                centerY - Client.font.getHeight(
                                                                    true) / 2f,
                                                                0xffffffff,
                                                                true)
            TextAlignment.right -> Client.font.drawLeftWithoutEnding(matrix,
                                                                     text(),
                                                                     centerX + width / 2f - 2f,
                                                                     centerY - Client.font.getHeight(
                                                                         true) / 2f,
                                                                     0xffffffff,
                                                                     true)
        }
        this.additionalRendering(matrix, this)
    }

    fun onChars(event:KeyTypedEvent) {
        this.textAction(event.str, this)
    }

    fun onKey(event:KeyPressedEvent) {
        this.keyAction(event, this)
    }

    fun isIn(x:double, y:double):bool {
        return x >= centerX - width / 2 && x <= centerX + width / 2 && y >= centerY - height / 2 && y <= centerY + height / 2
    }

    fun onMouse(event:MouseClickedEvent):bool {
        if (isIn(event.x.toDouble(), event.y.toDouble())) {
            if (event.action == GLFW_PRESS) {
                return if (event.button == GLFW_MOUSE_BUTTON_LEFT) {
                    left(this)
                    true
                } else {
                    right(this)
                    true
                }
            }
        }
        return false
    }

    enum class TextAlignment {
        left,
        center,
        right
    }

    companion object {
        inline fun make(lambda:Button.() -> void):Button = Button().apply(lambda)
    }

}