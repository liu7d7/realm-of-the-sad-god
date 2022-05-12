package me.ethius.server.rotsg.world.biome

import me.ethius.server.rotsg.world.ServerWorld
import me.ethius.shared.events.def.GlfwDestroyEvent
import me.ethius.shared.events.Listen
import me.ethius.shared.fillCircle
import me.ethius.shared.int
import me.ethius.shared.ivec2
import me.ethius.shared.maths.WeightedCollection
import me.ethius.shared.rotsg.data.EntityInfo
import me.ethius.shared.rotsg.entity.AEntity
import me.ethius.shared.rotsg.entity.enemy.Enemy
import me.ethius.shared.rotsg.tile.Tile
import me.ethius.shared.string
import org.apache.commons.lang3.RandomUtils
import java.util.*

class CustomBiome(
    centerX:int,
    centerY:int,
    val world:ServerWorld,
    rad:int = 0,
    tile:(ivec2) -> Tile,
):ABiome() {

    override val id:string = "custom_biome"
    override val entities:ArrayList<AEntity> = ArrayList()
    lateinit var spawner:TimerTask
    override val x:int = centerX
    override val y:int = centerY
    var spawnTime = 10000L
    var maxEntities = 6
    val spawnableEntities:WeightedCollection<EntityInfo<out Enemy>> = WeightedCollection()

    override fun worldInit(world:ServerWorld) {
        if (this::spawner.isInitialized || world != this.world)
            return
        this.spawner = object:TimerTask() {
            override fun run() {
                if (entities.size >= maxEntities) {
                    return
                }
                if (tiles.isNotEmpty()) {
                    val b = tiles.values.random()
                    if (!spawnableEntities.isEmpty()) {
                        spawnableEntities.next()?.let { world.addEntity(it, b.pos).also { entities.add(it) } }
                    }
                }
            }
        }
        biomeSpawner.scheduleAtFixedRate(spawner,
                                         RandomUtils.nextLong(spawnTime / 2, spawnTime * 5 - spawnTime / 2),
                                         spawnTime * 15)
    }

    @Listen
    fun glfwDestroy(event:GlfwDestroyEvent) {
        if (this::spawner.isInitialized) {
            this.spawner.cancel()
        }
    }

    init {
        fillCircle(world,
                   this,
                   { tile(it) },
                   ivec2(x, y),
                   RandomUtils.nextInt(rad, rad + 1),
                   false)
    }

}