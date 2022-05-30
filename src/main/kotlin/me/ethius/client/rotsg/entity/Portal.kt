package me.ethius.client.rotsg.entity

import me.ethius.client.Client
import me.ethius.client.rotsg.gui.Button
import me.ethius.client.rotsg.gui.ldu
import me.ethius.shared.*
import me.ethius.shared.events.def.MouseClickedEvent
import me.ethius.shared.network.Packet
import me.ethius.shared.opti.TexData
import me.ethius.shared.rotsg.entity.AEntity
import me.ethius.shared.rotsg.entity.PassableEntity
import me.ethius.shared.rotsg.entity.player.Player
import me.ethius.shared.rotsg.tile.tile_size
import me.ethius.shared.rotsg.world.IWorld
import org.joml.Matrix4dStack
import kotlin.math.floor

open class Portal(final override val texDataId:string, val worldId:string, val name:string):
    PassableEntity() {

    final override var height:double = 0.0
    final override var pivotX = 0.0
    final override var pivotY = 0.0
    final override var width:double = 0.0

    init {
        ifclient {
            val texData = TexData[texDataId]
            height = texData.height * (46f / texData.width) * 0.8
            pivotX = texData.pivotX * ((46.0 / texData.width) * 0.8)
            pivotY = texData.pivotY * ((46.0 / texData.width) * 0.8)
            width = texData.width * (46f / texData.width) * 0.8
        }
    }

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
        if (this.world != null) {
            val tile = this.world!!.tileAt(ivec2(floor(this.x / tile_size).toInt(), floor(this.y / tile_size).toInt()))
            if (tile != null) {
                depth = tile.depth
            }
        }
    }

    private val nameLines = if (Side._client) Client.font.wrapWords(name, 200.0) else emptyArray()
    val renderHeight = nameLines.size * 22 + 40
    var invulnerable = false
    private val renderWidth = 200
    private val button = Button().setText { "Enter" }.setOnLeft {
        Client.network.send(Packet._id_world_request, this.worldId)
        Client.inGameHud.portals.clear()
        Client.player.inventory.bags.clear()
    }

    var offset = 0f

    override fun collideWith(other:AEntity) {
        if (other is Player && !Client.inGameHud.portals.contains(this)) {
            Client.inGameHud.portals.add(this)
        }
    }

    override fun clientTick() {
        prevX = x
        prevY = y
        boundingCircle.cx = x
        boundingCircle.cy = y
        if (!Client.player.collidesWith(this) && Client.inGameHud.portals.contains(this)) {
            Client.inGameHud.portals.remove(this)
        }
        if (measuringTimeMS() - timeSpawned >= 20000 && !invulnerable) {
            Client.world.remEntity(this)
            Client.inGameHud.portals.remove(this)
        }
    }

    fun renderInHud(matrix:Matrix4dStack) {
        val x = Client.window.midX.toDouble()
        val y = Client.window.midY.toDouble() + offset
        Client.render.drawRectAlphaWithoutEnding(matrix, x, y, x + renderWidth, y + renderHeight, ldu, 0.9f)
        for ((i, v) in nameLines.withIndex()) {
            Client.font.drawWithoutEnding(matrix,
                                          v,
                                          x + 2.5f,
                                          y + 2.5f + i * (Client.font.getHeight(true) + 5f),
                                          0xffffffff,
                                          true)
        }
        button.setCenterX(x + renderWidth / 2f).setCenterY(y + nameLines.size * (Client.font.getHeight(true) + 22f))
            .setWidth(75.0)
            .setHeight(26.0).renderWithoutEnding(matrix)
    }

    fun click(event:MouseClickedEvent) {
        button.onMouse(event)
    }

    companion object {
        val empty_portal = EmptyPortal()
    }

}

class EmptyPortal:Portal("sand_1", "", "EMPTY (BUG IF SEEN :(( )")