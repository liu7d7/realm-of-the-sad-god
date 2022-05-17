package me.ethius.client.rotsg.inventory

import me.ethius.client.Client
import me.ethius.client.rotsg.data.ItemInfo
import me.ethius.client.rotsg.item.AirItem
import me.ethius.client.rotsg.item.Item
import me.ethius.client.rotsg.item.WeaponItem
import me.ethius.client.rotsg.screen.Screen
import me.ethius.shared.bool
import me.ethius.shared.double
import me.ethius.shared.events.def.MouseClickedEvent
import me.ethius.shared.int
import me.ethius.shared.string
import org.joml.Matrix4dStack
import org.lwjgl.glfw.GLFW
import kotlin.math.roundToInt

class AbilitySlot(x:double, y:double):Slot(x, y, SlotId.Ability) {

    override var item:Item = ItemInfo.air()
        set(value) {
            field.onDequipExt()
            value.onEquipExt()
            field = value
        }

    override fun canPut(item:Item):bool {
        return super.canPut(item) && Client.player.pClass.abilityBaseClass.isAssignableFrom(item.javaClass)
    }

}

class ArmorSlot(x:double, y:double):Slot(x, y, SlotId.Armor) {

    override var item:Item = ItemInfo.air()
        set(value) {
            field.onDequipExt()
            value.onEquipExt()
            field = value
        }

    override fun canPut(item:Item):bool {
        return super.canPut(item) && Client.player.pClass.armorBaseClass.isAssignableFrom(item.javaClass)
    }

}

class RingSlot(x:double, y:double):Slot(x, y, SlotId.Ring) {

    override var item:Item = ItemInfo.air()
        set(value) {
            field.onDequipExt()
            value.onEquipExt()
            field = value
        }

}

class WeaponSlot(x:double, y:double):Slot(x, y, SlotId.Weapon) {

    override var item:Item = ItemInfo.air()
        set(value) {
            field.onDequipExt()
            value.onEquipExt()
            field = value
        }

    override fun canPut(item:Item):bool {
        return super.canPut(item) && Client.player.pClass.weaponBaseClass.isAssignableFrom(item.javaClass)
    }

    fun getWeapon():WeaponItem? {
        return if (item is WeaponItem)
            item as WeaponItem
        else
            null
    }

}

open class Slot(x:double, y:double, val slotId:SlotId) {

    var x:double = x
        get() = field.roundToInt().toDouble() + Client.window.midX.roundToInt().toDouble()
    var y:double = y
        get() = field.roundToInt().toDouble() + Client.window.scaledHeight

    open var item:Item = ItemInfo.air()

    open fun item():Item {
        return item
    }

    fun getItemForUse():Item {
        return if (item.dragging) ItemInfo.air() else item
    }

    open fun canPut(item:Item):bool {
        return slotId.canPut(item)
    }

    fun renderItem(matrix:Matrix4dStack) {
        item.render(matrix, (x + slot_width / 2f).roundToInt().toDouble(), (y + slot_width / 2f).roundToInt().toDouble())
    }

    fun renderItemText(matrix:Matrix4dStack) {
        item.renderTier(matrix, x, y)
    }

    fun renderTooltip(matrix:Matrix4dStack) {
        item.renderToolTip(matrix)
    }

    fun isIn(mouseX:double, mouseY:double):bool {
        return mouseX > x && mouseX < x + slot_width && mouseY > y && mouseY < y + slot_width
    }

    fun click(event:MouseClickedEvent) {
        if (isIn(event.x.toDouble(), event.y.toDouble())) {
            if (event.action == GLFW.GLFW_PRESS) {
                if (Screen.hasShiftDown()) {
                    if (this.item.consume()) {
                        this.item = ItemInfo.air()
                    }
                } else {
                    val draggingSlot = Client.player.inventory.draggingSlot
                    check(draggingSlot == null) {
                        "A slot (${draggingSlot!!.slotId}) is already being dragged, but slot ($slotId) is requesting to be dragged too!"
                    }
                    if (item !is AirItem) {
                        Client.player.inventory.draggingSlot = this
                        item.dragging = true
                    }
                }
            } else if (event.action == GLFW.GLFW_RELEASE) {
                val draggingSlot = Client.player.inventory.draggingSlot
                if (draggingSlot != null) {
                    if (draggingSlot.item !is AirItem) {
                        val otherItem = draggingSlot.item
                        if (this.canPut(otherItem)) {
                            draggingSlot.item = this.item
                            this.item = otherItem
                            otherItem.dragging = false
                        }
                    }
                    Client.player.inventory.draggingSlot = null
                }
            }
        } else if (event.action == GLFW.GLFW_RELEASE) {
            val draggingSlot = Client.player.inventory.draggingSlot
            if (this != draggingSlot)
                return
            if (!Client.player.inventory.slots.any { it.isIn(event.x.toDouble(), event.y.toDouble()) }) {
                draggingSlot.item.dragging = false
                Client.player.inventory.draggingSlot = null
            }
        }
    }
}

enum class SlotId(val canPut:(Item) -> bool) {
    Weapon({ it is WeaponItem || it is AirItem }),
    Ability({ it is me.ethius.client.rotsg.item.AbilityItem || it is AirItem }),
    Armor({ it is me.ethius.client.rotsg.item.ArmorItem || it is AirItem }),
    Ring({ it is me.ethius.client.rotsg.item.RingItem || it is AirItem }),
    Inv_1({ true }),
    Inv_2({ true }),
    Inv_3({ true }),
    Inv_4({ true }),
    Inv_5({ true }),
    Inv_6({ true }),
    Inv_7({ true }),
    Inv_8({ true }),
    Bag_1({ true }),
    Bag_2({ true }),
    Bag_3({ true }),
    Bag_4({ true }),
    Bag_5({ true }),
    Bag_6({ true }),
    Bag_7({ true }),
    Bag_8({ true });

    fun slotString():string {
        if (ordinal in 4..11)
            return (ordinal - 3).toString()
        return " "
    }

    fun newInst(x:double, y:double):Slot {
        return when (this) {
            Weapon -> WeaponSlot(x, y)
            Ring -> RingSlot(x, y)
            Armor -> ArmorSlot(x, y)
            Ability -> AbilitySlot(x, y)
            else -> Slot(x, y, this)
        }
    }

    companion object {
        operator fun get(i:int):SlotId {
            return values()[i]
        }
    }
}