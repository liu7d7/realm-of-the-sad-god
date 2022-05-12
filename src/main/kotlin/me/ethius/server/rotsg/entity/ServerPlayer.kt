package me.ethius.server.rotsg.entity

import me.ethius.server.Server
import me.ethius.server.network.SNetworkHandler
import me.ethius.shared.ext.distance2dSquared
import me.ethius.shared.ivec2
import me.ethius.shared.long
import me.ethius.shared.network.Packet
import me.ethius.shared.rotsg.entity.AEntity
import me.ethius.shared.rotsg.entity.createSpawnPacket
import me.ethius.shared.rotsg.entity.player.Player
import me.ethius.shared.rotsg.entity.player.PlayerClass
import me.ethius.shared.rotsg.entity.player.PlayerProfile
import me.ethius.shared.rotsg.tile.tile_size
import me.ethius.shared.rotsg.world.IWorld
import me.ethius.shared.string

class ServerPlayer(val client:SNetworkHandler.ClientView, playerProfile:PlayerProfile, override var entityId:long):
    Player(PlayerClass.valueOf(playerProfile.clazz), playerProfile) {

    val itemIds:Array<string> = Array(12) { playerProfile.items[it] }
    private val visibleEntities = HashSet<AEntity>()
    private val sentTiles = HashSet<ivec2>()

    override fun serverTick() {
        if (this.client.socket.isClosed) {
            this.world?.remEntity(this, true, false)
            return
        }
        this.world?.let {
            for (i in it.entities) {
                if (i.pos.distance2dSquared(this.pos) > 20 * tile_size * 20 * tile_size) {
                    continue
                }
                if (i !in visibleEntities) {
                    visibleEntities.add(i)
                    Server.network.send(client, i.createSpawnPacket())
                }
            }
            for (i in visibleEntities) {
                if (!i.alive || i.world != this.world) {
                    Server.network.send(client, Packet._id_delete_entity, i.entityId)
                }
            }
            visibleEntities.removeIf { !it.alive }
        }
    }

    override fun onAdd(world:IWorld) {
        sentTiles.clear()
        visibleEntities.clear()
        super.onAdd(world)
    }

    init {
        clientToPlayer[client] = this
    }

    companion object {
        val clientToPlayer = mutableMapOf<SNetworkHandler.ClientView, ServerPlayer>()

        operator fun get(clientView:SNetworkHandler.ClientView):ServerPlayer? = clientToPlayer[clientView]
    }

}