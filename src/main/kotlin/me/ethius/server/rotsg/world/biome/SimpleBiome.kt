package me.ethius.server.rotsg.world.biome

import me.ethius.server.rotsg.world.ServerWorld
import me.ethius.shared.events.def.GlfwDestroyEvent
import me.ethius.shared.events.Listen
import me.ethius.shared.fillCircle
import me.ethius.shared.int
import me.ethius.shared.ivec2
import me.ethius.shared.rotsg.world.biome.BiomeFeature
import me.ethius.shared.rotsg.entity.AEntity
import me.ethius.shared.rotsg.tile.Bushery
import me.ethius.shared.rotsg.tile.Tile
import me.ethius.shared.rotsg.world.addFeatureTiles
import me.ethius.shared.string
import org.apache.commons.lang3.RandomUtils
import java.util.*

class SimpleBiome(
    centerX:int,
    centerY:int,
    val world:ServerWorld,
    rad:int = 0,
    type:BiomeType? = null,
):ABiome() {

    override val x = centerX
    override val y = centerY
    val type:BiomeType = type ?: BiomeType.next()
    override val id:string = this.type.id
    private val radius:ClosedRange<int> = if (rad == 0) this.type.minRad..this.type.maxRad else rad..rad + 1
    override val entities = ArrayList<AEntity>()
    lateinit var spawner:TimerTask
    var maxEntities = 6

    override fun addTiles(tile:Array<Tile>) {
        this.tiles.putAll(tile.map { it.pos to it })
    }

    override fun addFeature(feature:BiomeFeature):List<Tile> {
        return addFeatureTiles({ addTile(it) }, feature)
    }

    override fun worldInit(world:ServerWorld) {
        if (this::spawner.isInitialized || world != this.world)
            return
        if (this.type.spawnTime == -1L)
            return
        this.spawner = object:TimerTask() {
            override fun run() {
                if (entities.size >= maxEntities) {
                    return
                }
                if (tiles.isNotEmpty()) {
                    val b = tiles.values.random()
                    if (!type.entities.isEmpty()) {
                        world.addEntity(type.entities.next()!!, b.pos).also { entities.add(it) }
                    }
                }
            }
        }
        biomeSpawner.scheduleAtFixedRate(spawner,
                                         RandomUtils.nextLong(type.spawnTime / 2,
                                                              type.spawnTime * 5 - type.spawnTime / 2),
                                         type.spawnTime * 15)
    }

    @Listen
    fun worldDestroy(event:GlfwDestroyEvent) {
        if (::spawner.isInitialized) {
            spawner.cancel()
        }
    }

    init {
        fillCircle(world, this,
                   { Tile(it, this.type.texDataId, Bushery.copy(this.type.bushery.next())) },
                   ivec2(x, y), RandomUtils.nextInt(radius.start, radius.endInclusive), false)
        if (!this.type.features.isEmpty()) {
            val vals = tiles.values
            for (i in 0..this.type.maxFeatures) {
                val a1 = this.type.features.next()
                a1?.supplier?.invoke(vals.random().pos)?.let()
                { bi_ftr ->
                    this.addFeature(bi_ftr)
                }
            }
        }
    }

}