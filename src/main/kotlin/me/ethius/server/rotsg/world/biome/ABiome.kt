package me.ethius.server.rotsg.world.biome

import me.ethius.server.rotsg.world.ServerWorld
import me.ethius.shared.bool
import me.ethius.shared.events.def.GlfwDestroyEvent
import me.ethius.shared.events.Listen
import me.ethius.shared.int
import me.ethius.shared.ivec2
import me.ethius.shared.rotsg.world.biome.BiomeFeature
import me.ethius.shared.rotsg.entity.AEntity
import me.ethius.shared.rotsg.tile.Tile
import me.ethius.shared.string
import java.util.*

abstract class ABiome {

    open val tiles = HashMap<ivec2, Tile>()
    open val entities:ArrayList<AEntity> = ArrayList()
    abstract val x:int
    abstract val y:int
    abstract val id:string

    fun addTile(tile:Tile, force:bool = false) {
        if (force) {
            this.tiles[tile.pos] = tile
        } else {
            this.tiles.putIfAbsent(tile.pos, tile)
        }
    }

    open fun addTiles(tile:Array<Tile>) {

    }

    open fun addFeature(feature:BiomeFeature):List<Tile> {
        return emptyList()
    }

    open fun worldInit(world:ServerWorld) {

    }

    fun addEntity(entity:AEntity, world:ServerWorld) {
        entities.add(entity)
        world.addEntity(entity)
    }

    companion object {
        val biomeSpawner = Timer("BiomeSpawner", true)

        @Listen
        fun glfwDestroy(event:GlfwDestroyEvent) {
            biomeSpawner.cancel()
        }
    }

}