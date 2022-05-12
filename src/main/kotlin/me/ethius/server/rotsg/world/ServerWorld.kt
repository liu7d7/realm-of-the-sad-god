package me.ethius.server.rotsg.world

import me.ethius.server.Server
import me.ethius.server.rotsg.entity.ServerPlayer
import me.ethius.server.rotsg.world.biome.ABiome
import me.ethius.shared.*
import me.ethius.shared.ext.distance2dSquared
import me.ethius.shared.ext.distanceSquared
import me.ethius.shared.ext.todvec2
import me.ethius.shared.network.Packet
import me.ethius.shared.rotsg.entity.AEntity
import me.ethius.shared.rotsg.entity.enemy.Enemy
import me.ethius.shared.rotsg.entity.player.Player
import me.ethius.shared.rotsg.tile.Tile
import me.ethius.shared.rotsg.tile.tile_size
import me.ethius.shared.rotsg.world.IWorld
import me.ethius.shared.rotsg.world.addFeatureTiles
import me.ethius.shared.rotsg.world.biome.BiomeFeature
import java.util.concurrent.CopyOnWriteArrayList

abstract class ServerWorld(name:string, vararg entities:AEntity):IWorld, Tickable(true) {
    override val name:string = name.lowercase()
    final override var tiles = HashMap<ivec2, Tile>()
    val abiomes = ArrayList<ABiome>()
    private var tilesInView:MutableSet<Tile> = HashSet()
    final override val entities = CopyOnWriteArrayList<AEntity>()
    var texDataId = "realm_portal"
    open var spawnPosition = ivec2(0, 0)
    lateinit var worldId:string

    val players:Collection<ServerPlayer>
        get() = entities.filterIsInstance<ServerPlayer>()

    final override fun addEntity(entity:AEntity) {
        if (!contains(entity)) {
            entity.world = this
            entity.init()
            entities.add(entity)
            entity.onAdd(this)
        }
        (entity as? ServerPlayer)?.let { onPlayerAdd(it) }
    }

    override fun remEntity(
        entity:AEntity,
        release:bool,
        fx:bool,
    ) {
        if (contains(entity)) {
            entities.remove(entity)
            if (release) {
                entity.release()
            }
            entity.world = null
            entity.onRem(this)
            if (entity is Enemy) {
                for (it in abiomes) {
                    it.entities.removeIf { it == entity }
                }
            }
            (entity as? ServerPlayer)?.let { onPlayerRem(it) }
        }
    }

    open fun onPlayerAdd(entity:ServerPlayer) {

    }

    open fun onPlayerRem(entity:ServerPlayer) {

    }

    override fun serverTick() {
        if (ticksExisted == 0) {
            for (biome in abiomes) {
                biome.worldInit(this)
            }
        }
        serverTickInternal()
    }

    fun closestPlayer(eye:AEntity):Player? {
        return Server.network.players.filter { it.world == this }.minByOrNull { it.pos.distance2dSquared(eye.pos) }
    }

    protected open fun serverTickInternal() {

    }

    override fun contains(entity:AEntity):bool {
        return entities.contains(entity)
    }

    fun addFeature(feature:BiomeFeature):List<Tile> {
        val list = addFeatureTiles({ addTile(it, true) }, feature)
        val str = list.joinToString(" ") {
            "${it.pos.x}|${it.pos.y}|${it.texDataId}|${it.env?.id ?: "NIL"}"
        }
        for (i in players.filter { it.pos.copy().div(tile_size).distanceSquared(list.first().pos.todvec2()) < 21 }) {
            Server.network.send(i.client, Packet._id_block_info_batch, str)
        }
        return list
    }

    init {
        for (it in entities) {
            addEntity(it)
        }
        init()
    }

}