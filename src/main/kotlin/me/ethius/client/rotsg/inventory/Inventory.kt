package me.ethius.client.rotsg.inventory

import me.ethius.client.Client
import me.ethius.client.ext.push
import me.ethius.client.ext.translate
import me.ethius.client.renderer.Framebuffer
import me.ethius.client.renderer.Mesh
import me.ethius.client.renderer.postprocess.Shadow
import me.ethius.client.rotsg.entity.Bag
import me.ethius.shared.bool
import me.ethius.shared.double
import me.ethius.shared.events.Listen
import me.ethius.shared.events.def.MouseClickedEvent
import me.ethius.shared.events.def.WindowResizedEvent
import me.ethius.shared.int
import me.ethius.shared.maths.Animations
import me.ethius.shared.measuringTimeMS
import org.joml.Matrix4dStack
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL11.*
import java.util.concurrent.CopyOnWriteArrayList

const val item_size = 40.0

class Inventory(private val dummy:bool = false) {

    val slots = ArrayList<Slot>()
    private val slotGroups = ArrayList<SlotGroup>()
    lateinit var weapon:WeaponSlot
    lateinit var ability:AbilitySlot
    lateinit var armor:ArmorSlot
    lateinit var ring:RingSlot
    var draggingSlot:Slot? = null
    var bags = CopyOnWriteArrayList<Bag>()
    lateinit var itemFramebuffer:Framebuffer
    lateinit var shadows:Framebuffer

    fun render(matrix:Matrix4dStack) {
        bags.removeIf { !it.shouldRenderInGui && measuringTimeMS() - it.animationTime >= 150f }
        if (dummy) return
        if (!this::itemFramebuffer.isInitialized) {
            itemFramebuffer = Framebuffer(false)
            shadows = Framebuffer(false)
        }
        // render all to Net //
        Mesh.triangles.begin()
        Client.font.begin(0.965)
        for (it in slotGroups) {
            it.renderSlots(matrix)
        }
        for ((i, k) in bags.withIndex()) {
            k.renderSlots(matrix, i)
        }
        Mesh.drawTriangles()

        itemFramebuffer.bind()
        glClearColor(0f, 0f, 0f, 0f)
        glClear(GL_COLOR_BUFFER_BIT)
        Mesh.triangles.begin()
        for (it in slotGroups) {
            it.postRender(matrix)
        }
        for (it in bags) {
            it.renderItems(matrix)
        }
        Mesh.drawTriangles()
        shadows.bind()
        glClearColor(0f, 0f, 0f, 0f)
        glClear(GL_COLOR_BUFFER_BIT)
        itemFramebuffer.draw(true,
                             0.0,
                             Client.window.height.toDouble(),
                             Client.window.width.toDouble(),
                             0.0)
        Shadow.render(4f, shadows)
        shadows.unbind()
//        EntityOutline.render(itemFramebuffer)
        itemFramebuffer.unbind()
        Client.frameBufferObj.bind()
        shadows.draw(false,
                     0.0,
                     0.0,
                     Client.window.scaledWidth.toDouble(),
                     Client.window.scaledHeight.toDouble())
        itemFramebuffer.draw(false,
                             0.0,
                             0.0,
                             Client.window.scaledWidth.toDouble(),
                             Client.window.scaledHeight.toDouble())

        Mesh.triangles.begin()
        for (it in slotGroups) {
            it.postRenderText(matrix)
        }
        for (it in bags) {
            it.renderItemText(matrix)
        }

        for (it in slotGroups) {
            it.renderTooltip(matrix)
        }
        for (it in bags) {
            it.renderTooltips(matrix)
        }
        Mesh.drawTriangles()
    }

    private fun Bag.setupMatrix(matrix:Matrix4dStack) {
        val tslg = this.slotGroups[1]
        matrix.translate(tslg.tlX + item_size * 2, tslg.tlY + item_size, 0.0) {
            if (measuringTimeMS() - animationTime <= 150f) {
                if (this.shouldRenderInGui) {
                    matrix.scale(Animations.getDecelerateAnimation(150f, measuringTimeMS() - this.animationTime).toDouble())
                } else {
                    matrix.scale(1 - Animations.getAccelerateAnimation(150f, measuringTimeMS() - this.animationTime).toDouble())
                }
            }
        }
    }

    private fun Bag.renderItems(matrix:Matrix4dStack) {
        matrix.push {
            setupMatrix(matrix)
            for (it in slots) {
                it.renderItem(matrix)
            }
        }
    }

    private fun Bag.renderItemText(matrix:Matrix4dStack) {
        matrix.push {
            setupMatrix(matrix)
            for (it in slots) {
                it.renderItemText(matrix)
            }
        }
    }

    private fun Bag.renderSlots(matrix:Matrix4dStack, offset:int) {
        updateOffset(offset)
        matrix.push {
            setupMatrix(matrix)
            for (i in slotGroups) {
                i.renderSlots(matrix)
            }
        }
    }

    private fun Bag.renderTooltips(matrix:Matrix4dStack) {
        for (it in slots) {
            it.renderTooltip(matrix)
        }
    }

    private fun Bag.updateOffset(offset:int) {
        for (_i in slotGroups.indices) {
            val i = 1 - _i
            val group = slotGroups[_i]
            group.tlY = Client.window.scaledHeight - Client.window.scaledHeight * 0.0675 - item_size * (2 + i) - 9 - (item_size * 2 + 10) * offset
        }
    }

    fun isIn(mouseX:double, mouseY:double):bool {
        return slots.any { it.isIn(mouseX, mouseY) }
    }

    @Listen
    fun click(event:MouseClickedEvent) {
        if (dummy) return
        if (!Client.playerInit) return
        if (event.button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            for (it in bags) {
                for (it in it.slots) {
                    it.click(event)
                }
            }
        }
        for (it in slots) {
            it.click(event)
        }
    }

    fun release() {
        Client.events.unregister(this)
    }

    @Listen
    fun resize(event:WindowResizedEvent) {
        if (dummy) return
        initSlots()
    }

    private fun initSlots() {
        if (dummy) {
            for (i in 0..11) {
                slots.add(SlotId[i].newInst(0.0, 0.0).also {
                    when (it.slotId) {
                        SlotId.Weapon -> weapon = it as WeaponSlot
                        SlotId.Ability -> ability = it as AbilitySlot
                        SlotId.Armor -> armor = it as ArmorSlot
                        SlotId.Ring -> ring = it as RingSlot
                        else -> {}
                    }
                })
            }
            return
        }
        val oldSlots = slots.clone() as ArrayList<Slot>
        slots.clear()
        slotGroups.clear()
        // 4 equipped items //
        run {
            val arr1 = Array(4) { i -> SlotId[i].newInst(0.0, 0.0).also { slots.add(it) } }
            slotGroups.add(SlotGroup(arr1,
                                     Client.window.midX - item_size * 2,
                                     Client.window.scaledHeight - Client.window.scaledHeight * 0.0675 - item_size + 1))
        }

        // first 4 slots //
        run {
            val arr2 = Array(4) { i -> SlotId[i + 4].newInst(0.0, 0.0).also { slots.add(it) } }
            slotGroups.add(SlotGroup(arr2,
                                     Client.window.midX - item_size * 6 - 10,
                                     Client.window.scaledHeight - Client.window.scaledHeight * 0.0675 - item_size + 1))
        }

        // last 4 slots //
        run {
            val arr3 = Array(4) { i -> SlotId[i + 8].newInst(0.0, 0.0).also { slots.add(it) } }
            slotGroups.add(SlotGroup(arr3,
                                     Client.window.midX + item_size * 2 + 10,
                                     Client.window.scaledHeight - Client.window.scaledHeight * 0.0675 - item_size + 1))
        }

        // easily accessible  //
        weapon = slots[0] as WeaponSlot
        ability = slots[1] as AbilitySlot
        armor = slots[2] as ArmorSlot
        ring = slots[3] as RingSlot

        if (oldSlots.isNotEmpty()) {
            for (i in slots.indices) {
                slots[i].item = oldSlots[i].item
            }
        }
    }

    init {
        initSlots()
        Client.events.register(this)
    }

}