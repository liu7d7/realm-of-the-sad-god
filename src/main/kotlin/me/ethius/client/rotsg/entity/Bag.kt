package me.ethius.client.rotsg.entity

import me.ethius.client.Client
import me.ethius.client.rotsg.inventory.Slot
import me.ethius.client.rotsg.inventory.SlotGroup
import me.ethius.client.rotsg.inventory.SlotId
import me.ethius.client.rotsg.inventory.slot_width
import me.ethius.client.rotsg.item.AirItem
import me.ethius.client.rotsg.item.Item
import me.ethius.client.rotsg.item.ItemTier
import me.ethius.client.rotsg.overlay.LootDropOverlay
import me.ethius.client.rotsg.world.ClientWorld
import me.ethius.shared.*
import me.ethius.shared.opti.TexData
import me.ethius.shared.rotsg.entity.AEntity
import me.ethius.shared.rotsg.entity.PassableEntity
import me.ethius.shared.rotsg.tile.tile_size
import me.ethius.shared.rotsg.world.IWorld
import kotlin.math.floor

class Bag(var bagTier:BagTier, items:List<Item>):PassableEntity() {

    override var height:double = 0.0
    override var pivotX = 0.0
    override var pivotY = 0.0
    override var texDataId:string = ""
    override var width:double = 0.0

    var animationTime = 0f
    var shouldRenderInGui = false

    // this is the bag's inventory //
    var slots:ArrayList<Slot>
    var slotGroups:Array<SlotGroup>
    var rawItems:List<string>

    override var x:double
        get() = super.x
        set(value) {
            super.x = value
            this.prevX = value
            this.updateBoundingCircle()
        }
    override var y:double
        get() = super.y
        set(value) {
            super.y = value
            this.prevY = value
            this.updateBoundingCircle()
        }

    override fun onAdd(world:IWorld) {
        if (world is ClientWorld) {
            val tile = world.tileAt(ivec2(floor(this.x / tile_size).toInt(), floor(this.y / tile_size).toInt()))
            if (tile != null) {
                depth = tile.depth
            }
        }
    }

    override fun release() {
        super.release()
        Client.player.inventory.bags.remove(this)
    }

    override fun collideWith(other:AEntity) {
        if (Side._client) {
            if (other is ClientPlayer && !other.inventory.bags.contains(this) && Client.ticker.contains(this) && !slots.stream().allMatch { it.item is AirItem }) {
                other.inventory.bags.add(this)
                animationTime = measuringTimeMS()
                shouldRenderInGui = true
            }
        }
    }

    override fun clientTick() {
        prevX = x
        prevY = y
        boundingCircle.cx = x
        boundingCircle.cy = y
        if (!Client.player.boundingCircle.collidesWith(this.boundingCircle) && Client.player.inventory.bags.contains(this) && shouldRenderInGui) {
            animationTime = measuringTimeMS()
            shouldRenderInGui = false
        }
        if (slots.stream().allMatch { it.item is AirItem }) {
            Client.world.remEntity(this)
        }
        if (measuringTimeMS() - timeSpawned >= 20000) {
            Client.world.remEntity(this)
        }
    }

    init {
        height = this.bagTier.texData.height * 4f
        pivotX = this.bagTier.texData.pivotX * 4f
        pivotY = this.bagTier.texData.pivotY * 4f
        texDataId = this.bagTier.texData.id
        width = this.bagTier.texData.width * 4f
        slots = ArrayList()
        slotGroups = Array(2) { idx ->
            val _slots = Array(4) { i -> SlotId[i + 12 + idx * 4].newInst(0.0, 0.0).also { if (i + idx * 4 in items.indices) it.item = items[i + idx * 4] } }
            slots.addAll(_slots)
            val e = SlotGroup(_slots, Client.window.midX - slot_width * 2, 0.0)
            e.bottom = idx == 1
            e.top = idx == 0
            e
        }
        this.rawItems = items.map { it.name }
        if (Client.overlay == null) {
            if (slots.any { it.item.tier == ItemTier.primal }) {
                Client.overlay = LootDropOverlay(LootDropOverlay.Type.primal)
            } else if (slots.any { it.item.tier == ItemTier.legendary }) {
                Client.overlay = LootDropOverlay(LootDropOverlay.Type.legendary)
            }
        }
    }

}

enum class BagTier(val texData:TexData) {
    empty(TexData.empty),
    pink(TexData.pink_bag),
    yellow(TexData.yellow_bag),
    white(TexData.white_bag);
}