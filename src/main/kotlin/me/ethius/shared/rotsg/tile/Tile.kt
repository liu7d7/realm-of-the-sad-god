package me.ethius.shared.rotsg.tile

import it.unimi.dsi.fastutil.objects.Object2BooleanFunction
import it.unimi.dsi.fastutil.objects.Object2IntFunction
import it.unimi.dsi.fastutil.objects.Object2ObjectFunction
import me.ethius.client.Client
import me.ethius.client.ext.transform
import me.ethius.shared.*
import me.ethius.shared.maths.BoundingCircle
import me.ethius.shared.maths.Facing
import me.ethius.shared.opti.FlowingTexData
import me.ethius.shared.opti.TexData
import me.ethius.shared.rotsg.data.EffectInfo
import me.ethius.shared.rotsg.entity.player.Player
import kotlin.math.floor

const val tile_size = 40.0

class Tile(val pos:ivec2, var texDataId:string, env:Bushery? = null) {
    constructor(pos:ivec2, texData:TexData, env:Bushery? = null):this(pos, texData.id, env)
    constructor(texDataId:string, env:Bushery?):this(ivec2(), texDataId, env)
    constructor(texData:TexData, env:Bushery?):this(ivec2(), texData.id, env)

    // client-side section
    val tilesAdj = arrayOfNulls<Tile>(4)
    val shouldBlend = booleanArrayOf(false, false, false, false)
    var blendOrder = intArrayOf(0, 1, 2, 3)
    var allowBlend = false
    var slippy = 0.2f
    var depth:int = depthMap.getInt(TexData[texDataId])
    var onPlayerWalk:(Player) -> void = onPlayerWalkMap.get(TexData[texDataId])
    val hash = this.pos.hashCode()


    // shared section
    var env:Bushery? = null
        set(value) {
            field = value
            if (field != null) {
                setBoundingBox()
            }
        }
    val hasBoundingCircle:bool
        get() = env != null && env!!.boundingBoxType != Bushery.BoundingBoxType.none
    val boundingCircle:BoundingCircle = BoundingCircle(30000.0, 30000.0, -1.0)

    private val renderY:double
        get() {
            if (!Client.lookAtInit) {
                return 0.0
            }
            val vec4 = dvec4(pos.x * tile_size, pos.y * tile_size, 1.0, 1.0)
            vec4.transform(Client.lookAt)
            return vec4.y
        }
    private val renderX:double
        get() {
            if (!Client.lookAtInit) {
                return 0.0
            }
            val vec4 = dvec4(pos.x * tile_size, pos.y * tile_size, 1.0, 1.0)
            vec4.transform(Client.lookAt)
            return vec4.x
        }

    val randomRotationDirectionAngle =
        if (allowRandomRotationMap.getBoolean(TexData[texDataId])) when (Facing[floor(frand(this.hash) * Facing.values().size).toInt()]) {
            Facing.down -> 0.0
            Facing.up -> 180.0
            Facing.right -> 90.0
            Facing.left -> -90.0
        }
        else 0.0

    private fun setBoundingBox() {
        when (env!!.boundingBoxType) {
            Bushery.BoundingBoxType.centered -> {
                boundingCircle.cx = pos.x * tile_size + tile_size / 2f
                boundingCircle.cy = pos.y * tile_size + tile_size / 2f
                boundingCircle.radius = tile_size * 0.25f
            }
            Bushery.BoundingBoxType.full -> {
                boundingCircle.cx = pos.x * tile_size + tile_size / 2f
                boundingCircle.cy = pos.y * tile_size + tile_size / 2f
                boundingCircle.radius = tile_size / 2f
            }
            Bushery.BoundingBoxType.wall -> {
                boundingCircle.cx = pos.x * tile_size + tile_size / 2f
                boundingCircle.cy = pos.y * tile_size + tile_size / 2f
                boundingCircle.radius = tile_size / 1.5f
            }
            else -> {
                boundingCircle.cx = 30000.0
                boundingCircle.cy = 30000.0
                boundingCircle.radius = -1.0
            }
        }
    }

    fun playerCanSee():bool {
        var inCircle = pos.distanceSquared(floor(Client.player.x / tile_size).toInt(), floor(Client.player.y / tile_size).toInt()) <= Client.options.renderDst * Client.options.renderDst
        if (inCircle) {
            val renderX = renderX
            val renderY = renderY
            inCircle = renderX > -tile_size * 4f && renderX < Client.window.scaledWidth + tile_size * 4f &&
                       renderY > -tile_size * 4f && renderY < Client.window.scaledHeight + tile_size * 4f
        }
        return inCircle
    }

    override fun toString():string {
        return "Tile(pos=${pos}, texData=${texDataId}, env=${env?.id})"
    }

    override fun equals(other:Any?):bool {
        if (Client.worldInit || Side._server) {
            return super.equals(other)
        }
        if (other is Tile) {
            return other.texDataId == texDataId && other.env == env && other.pos == pos
        }
        return false
    }

    override fun hashCode():Int {
        if (Client.worldInit || Side._server) {
            return super.hashCode()
        }
        return texDataId.hashCode() * 31 + (env?.id?.hashCode() ?: 0)
    }

    fun modulatedTexData():TexData {
        return TexData[this.texDataId].texData(this.hash, Client.worldRenderer.windX * 4.0f)
    }

    fun updateAdjacentTiles(
        tileLeft:Tile?,
        tileRight:Tile?,
        tileUp:Tile?,
        tileDown:Tile?,
    ) {
        val thisTexData = modulatedTexData()
        if (tileLeft != null) {
            val tLeftTexData = tileLeft.modulatedTexData()
            this.tilesAdj[0] = tileLeft
            this.shouldBlend[0] = tLeftTexData != thisTexData && isColor1Darker(tLeftTexData.avgColor, thisTexData.avgColor) && this.allowBlend
        }
        if (tileRight != null) {
            val tRightTexData = tileRight.modulatedTexData()
            this.tilesAdj[1] = tileRight
            this.shouldBlend[1] = tRightTexData != thisTexData && isColor1Darker(tRightTexData.avgColor, thisTexData.avgColor) && this.allowBlend
        }
        if (tileUp != null) {
            val tUpTexData = tileUp.modulatedTexData()
            this.tilesAdj[2] = tileUp
            this.shouldBlend[2] = tUpTexData != thisTexData && isColor1Darker(tUpTexData.avgColor, thisTexData.avgColor) && this.allowBlend
        }
        if (tileDown != null) {
            val tDownTexData = tileDown.modulatedTexData()
            this.tilesAdj[3] = tileDown
            this.shouldBlend[3] = tDownTexData != thisTexData && isColor1Darker(tDownTexData.avgColor, thisTexData.avgColor) && this.allowBlend
        }
        blendOrder = blendOrder.sortedBy { 255f - darknessOf(if (tilesAdj[it] != null) tilesAdj[it]!!.modulatedTexData().avgColor else 0xffffffff) }.toIntArray()
    }

    init {
        this.env = env
        ifclient {
            this.allowBlend = allowBlendMap.getBoolean(TexData[texDataId])
        }
    }

    companion object {
        val blend_rotation_arr = arrayOf(-90.0, 90.0, 0.0, 180.0)
        val blend_rotation_outcorner_arr = arrayOf(0.0, 90.0, 180.0, -90.0)

        private val depthMap = object:Object2IntFunction<TexData> {
            override fun getInt(key:Any?):Int {
                if (key == null) {
                    return 0
                }
                if (key is TexData) {
                    return when (key) {
                        TexData.water_1, TexData.border_water_1, TexData.lava_1, TexData.sick, TexData.lava_2 -> 10
                        TexData.water_2 -> 3
                        TexData.water_3 -> 7
                        else -> 0
                    }
                }
                return 0
            }
        }

        private val ptv = { _: Player -> }

        private val onPlayerWalkMap = object:Object2ObjectFunction<TexData, (Player) -> void> {
            override fun get(key:Any?):(Player) -> void {
                if (key == null) {
                    return ptv
                }
                if (key is TexData) {
                    return when (key) {
                        TexData.lava_1 -> { player:Player ->
                            player.damage(45.0, true, -1)
                        }
                        TexData.sick -> { player:Player ->
                            player.addEffect(EffectInfo.sick(600L))
                        }
                        else -> {
                            ptv
                        }
                    }
                }
                return ptv
            }
        }

        private val allowRandomRotationMap = object:Object2BooleanFunction<TexData> {
            override fun getBoolean(key:Any?):bool {
                if (key == null) {
                    return false
                }
                if (key is TexData) {
                    return when (key) {
                        is FlowingTexData -> false
                        TexData.wood_1, TexData.metal_floor_1, TexData.metal_floor_2, TexData.cobble_floor_1 -> false
                        else -> key.randomRotation
                    }
                }
                return false
            }
        }

        private val allowBlendMap = object:Object2BooleanFunction<TexData> {
            override fun getBoolean(key:Any?):bool {
                if (key == null) {
                    return false
                }
                if (key is TexData) {
                    return when (key) {
                        TexData.wood_1, TexData.metal_floor_1, TexData.metal_floor_2 -> false
                        else -> key.blend
                    }
                }
                return false
            }
        }
    }

}

class TileData(pos:ivec2, texData:string, env:string?) {
    val pos = pos
    val texData = texData
    val env = env ?: "NIL"
}