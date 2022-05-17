package me.ethius.client.rotsg.inventory

import me.ethius.client.Client
import me.ethius.client.rotsg.gui.dul
import me.ethius.client.rotsg.gui.ldu
import me.ethius.client.rotsg.gui.lldu
import me.ethius.shared.bool
import me.ethius.shared.double
import org.joml.Matrix4dStack

class SlotGroup(private val slots:Array<Slot>, tlX:double, tlY:double) {

    var tlX:double = 0.0
        set(value) {
            field = value
            for ((idx, slot) in slots.withIndex()) {
                slot.x = field - Client.window.midX + idx * slot_width
            }
        }
    var tlY:double = 0.0
        set(value) {
            field = value
            for (slot in slots) {
                slot.y = field - Client.window.scaledHeight
            }
        }

    var top = true
    var bottom = true
    var left = true
    var right = true

    fun Slot.render(matrix:Matrix4dStack, outline:bool) {
        if (Client.player.canUse(this.item)) {
            Client.render.drawRectAlphaWithoutEnding(matrix,
                                                     x,
                                                     y,
                                                     x + slot_width,
                                                     y + slot_width,
                                                     if (isIn(Client.mouse.x.toDouble(),
                                                              Client.mouse.y.toDouble())) lldu else ldu,
                                                     0.3f)
        } else {
            Client.render.drawRectAlphaWithoutEnding(matrix,
                                                     x,
                                                     y,
                                                     x + slot_width,
                                                     y + slot_width,
                                                     if (isIn(Client.mouse.x.toDouble(),
                                                              Client.mouse.y.toDouble())) 0xffce4b50 else 0xffb33035,
                                                     0.3f)
        }
        if (outline) {
            Client.render.drawOutlineRectWithoutEnding(matrix,
                                                       x,
                                                       y,
                                                       slot_width,
                                                       slot_width,
                                                       dul,
                                                       1.0,
                                                       true,
                                                       true,
                                                       true,
                                                       true)
        }
        Client.font.drawCenteredStringWithoutEnding(matrix,
                                                    slotId.slotString(),
                                                    x + slot_width / 2f,
                                                    y + slot_width / 2f,
                                                    0xffffffff,
                                                    true,
                                                    1.0)
    }

    fun renderSlots(matrix:Matrix4dStack) {
        for (it in slots) {
            it.render(matrix, false)
        }
        Client.render.drawShadowOutlineRectWithoutEnding(matrix,
                                                         tlX + 0.1f,
                                                         tlY + 0.05f,
                                                         slot_width * slots.size,
                                                         slot_width + 0.1f,
                                                         0x60000000,
                                                         3.0,
                                                         top,
                                                         left,
                                                         right,
                                                         bottom)
    }

    fun postRender(matrix:Matrix4dStack) {
        for (it in slots) {
            it.renderItem(matrix)
        }
    }

    fun postRenderText(matrix:Matrix4dStack) {
        for (it in slots) {
            it.renderItemText(matrix)
        }
    }

    fun renderTooltip(matrix:Matrix4dStack) {
        for (it in slots) {
            it.renderTooltip(matrix)
        }
    }

    init {
        this.tlX = tlX
        this.tlY = tlY
    }

}