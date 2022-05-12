package me.ethius.shared.rotsg.world

import com.google.common.collect.Lists
import me.ethius.shared.*
import me.ethius.shared.ext.toivec2
import me.ethius.shared.maths.BoundingCircle
import me.ethius.shared.rotsg.data.EntityInfo
import me.ethius.shared.rotsg.entity.AEntity
import me.ethius.shared.rotsg.entity.enemy.Enemy
import me.ethius.shared.rotsg.tile.Tile
import me.ethius.shared.rotsg.tile.tile_size
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.roundToInt

interface IWorld {


    // shared member variables
    val tiles:MutableMap<ivec2, Tile>
    val entities:CopyOnWriteArrayList<AEntity>
    val name:string


    // shared functions
    fun getEntityById(id:long):AEntity? {
        return entities.find { it.entityId == id }
    }

    fun addEntity(entity:AEntity)
    fun addEntity(entity:EntityInfo<out Enemy>, tilePos:ivec2):Enemy {
        val ent = entity().initPosition(tilePos.x * tile_size, tilePos.y * tile_size)
        this.addEntity(ent)
        return ent
    }

    fun remEntity(
        entity:AEntity,
        release:bool = true,
        fx:bool = false,
    )

    fun contains(entity:AEntity):bool

    fun getBoundingCircles(boundingCircle:BoundingCircle):List<BoundingCircle> {
        val list = Lists.newArrayList<BoundingCircle>()
        val xs = ((boundingCircle.cx - boundingCircle.radius) / tile_size).roundToInt() - 1
        val ys = ((boundingCircle.cy - boundingCircle.radius) / tile_size).roundToInt() - 1
        val xe = ((boundingCircle.cx + boundingCircle.radius) / tile_size).roundToInt() + 1
        val ye = ((boundingCircle.cy + boundingCircle.radius) / tile_size).roundToInt() + 1
        for (i in xs..xe) {
            for (j in ys..ye) {
                _s_pos.x = i
                _s_pos.y = j
                val tile = tileAt(_s_pos)
                if (tile != null && tile.hasBoundingCircle) {
                    val boundingCircle1 = tile.boundingCircle
                    if (boundingCircle1.collidesWith(boundingCircle)) {
                        list.add(boundingCircle1)
                    }
                }
            }
        }
        return list
    }

    fun addTile(tile:Tile, force:bool = false):bool {
        return if (force) {
            this.tiles[tile.pos] = tile
            true
        } else {
            this.tiles.putIfAbsent(tile.pos, tile) == null
        }
    }

    fun tileAt(pos:ivec2):Tile? {
        return tiles[pos]
    }

    fun tileAt(pos:dvec2):Tile? {
        return tileAt(pos.copy().div(tile_size).toivec2())
    }

    fun tileAtAbsolute(pos:ivec2):Tile? {
        return tiles[pos]
    }

    fun addAllTiles(list:Collection<Tile>) {
        this.tiles.putAll(list.map { it.pos to it })
    }

    companion object {
        private val _s_pos = ivec2()
    }

}

