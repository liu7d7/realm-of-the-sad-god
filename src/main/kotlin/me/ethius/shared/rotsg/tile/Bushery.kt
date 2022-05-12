package me.ethius.shared.rotsg.tile

import me.ethius.server.rotsg.world.biome.BiomeType
import me.ethius.shared.bool
import me.ethius.shared.double
import me.ethius.shared.int
import me.ethius.shared.opti.TexData
import me.ethius.shared.string
import org.apache.commons.lang3.RandomUtils
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.jvmErasure

open class Bushery(
    val texDataId:string,
    val randomCenterMax:double = 0.0,
    scale:double = 1.0,
    val windMultiplier:double = 1.0,
    open var boundingBoxType:BoundingBoxType = BoundingBoxType.none,
) {
    var id:string = ""
    open var is3d:bool = false
    val scale = scale
        get() = field * 2.35 * 2.35
    val randomRotationAngle = if (RandomUtils.nextBoolean()) 180.0 else 0.0

    fun addToBiomeType(weight:double, vararg biomeData:BiomeType):Bushery {
        for (it in biomeData) {
            it.bushery.add(weight, this)
        }
        return this
    }

    override fun equals(other:Any?):bool {
        if (other == null)
            return false
        if (other.javaClass != this.javaClass)
            return false
        if (other !is Bushery)
            return false
        return texDataId == other.texDataId && randomCenterMax == other.randomCenterMax && scale == other.scale
    }

    open fun copy():Bushery {
        return this
    }

    override fun hashCode():int {
        var result = texDataId.hashCode()
        result = 31 * result + randomCenterMax.hashCode()
        result = 31 * result + scale.hashCode()
        result = 31 * result + windMultiplier.hashCode()
        result = 31 * result + boundingBoxType.hashCode()
        return result
    }

    companion object {
        // busheryinfo, id
        val values = ArrayList<Bushery>()
        private val centerXS = { bInfo:Bushery ->
            tile_size / 2f + (RandomUtils.nextDouble(0.0,
                                                     bInfo.randomCenterMax * 0.5) - bInfo.randomCenterMax)
        }
        private val centerYS = { bInfo:Bushery ->
            tile_size / 2f + RandomUtils.nextDouble(0.0,
                                                    bInfo.randomCenterMax * 0.5) - bInfo.randomCenterMax
        }

        val empty = Bushery("empty").addToBiomeType(15.0,
                                                          BiomeType.lake,
                                                          BiomeType.gore,
                                                          BiomeType.desert,
                                                          BiomeType.dark_plains,
                                                          BiomeType.plains)
            .addToBiomeType(9.0, BiomeType.mountains)
        val empty_bb = Bushery("empty", 0.0, 0.0, 0.0, BoundingBoxType.wall)
        val tree_1 =
            Bushery("tree_1", 2.0, 1.0, .3, BoundingBoxType.centered).addToBiomeType(1.0, BiomeType.plains)
                .addToBiomeType(.5,
                                BiomeType.dark_plains)
        val mushroom_1 =
            Bushery("mushroom_1", 3.0, .8).addToBiomeType(.9, BiomeType.dark_plains).addToBiomeType(.6,
                                                                                                          BiomeType.plains)
        val mushroom_2 = Bushery("mushroom_2", 4.3, .8).addToBiomeType(.6, BiomeType.dark_plains)
        val desert_bush_1 = Bushery("desert_bush_1", 5.0, .8).addToBiomeType(.3, BiomeType.desert)
        val desert_bush_2 = Bushery("desert_bush_2", 5.0, .8).addToBiomeType(.3, BiomeType.desert)
        val gore_bush_1 = Bushery("gore_bush_1", 6.0, .8, 1.8).addToBiomeType(.35, BiomeType.gore)
        val gore_bush_2 = Bushery("gore_bush_2", 6.0, .8, 1.8).addToBiomeType(.35, BiomeType.gore)
        val crystal_1 = Bushery("crystal_1", 7.0, .8, 0.0, BoundingBoxType.centered)
        val bone_torch_1 = Bushery("bone_torch_1", 8.0, .8, 0.3)
        val bone_torch_2 = Bushery("bone_torch_2", 8.0, .8, 0.3)
        val metal_fence_1 = Bushery("metal_fence_1", 0.0, .925, 0.1, BoundingBoxType.full)
        val metal_torch_1 = Bushery("metal_torch_1", 0.0, .925, 0.1)
        val metal_torch_2 = Bushery("metal_torch_2", 0.0, .925, 0.1)
        val palm_tree_1 =
            Bushery("palm_tree_1", 5.0, .925, 0.1, BoundingBoxType.centered).addToBiomeType(.3, BiomeType.desert)
        val fir_tree_1 = Bushery("fir_tree_1", 5.0, .925, 0.1, BoundingBoxType.centered).addToBiomeType(.15,
                                                                                                              BiomeType.dark_plains)
        val fir_tree_2 = Bushery("fir_tree_2", 5.0, .925, 0.1, BoundingBoxType.centered).addToBiomeType(.15,
                                                                                                              BiomeType.dark_plains)
        val three_dee:Bushery = ThreeDWall()
        val stone_wall:Bushery = ThreeDWall().also {
            it.renderData = ThreeDWall.RenderData("cobble_floor_1",
                                                  "cobble_floor_1",
                                                  "cobble_floor_1",
                                                  "cobble_floor_1",
                                                  "cobble_floor_1")
        }
        val blue_crystal_wall:Bushery = ThreeDWall().also {
            it.renderData = ThreeDWall.RenderData("edge_blue_crystal_tile_u",
                                                  "edge_blue_crystal_tile_u",
                                                  "edge_blue_crystal_tile_u",
                                                  "edge_blue_crystal_tile_u",
                                                  "light_blue_crystal_tile")
        }
        val purple_crystal_wall:Bushery = ThreeDWall().also {
            it.renderData = ThreeDWall.RenderData("edge_purple_crystal_tile_u",
                                                  "edge_purple_crystal_tile_u",
                                                  "edge_purple_crystal_tile_u",
                                                  "edge_purple_crystal_tile_u",
                                                  "light_purple_crystal_tile")
        }
        val elemental_orange_wall:Bushery = ThreeDWall().also {
            it.renderData = ThreeDWall.RenderData("stone_1",
                                                  "stone_1",
                                                  "stone_1",
                                                  "stone_1",
                                                  "elemental_org")
        }
        val elemental_blue_wall:Bushery = ThreeDWall().also {
            it.renderData = ThreeDWall.RenderData("stone_1",
                                                  "stone_1",
                                                  "stone_1",
                                                  "stone_1",
                                                  "elemental_blu")
        }
        val stone_1_wall:Bushery = ThreeDWall().also {
            it.renderData = ThreeDWall.RenderData("stone_1",
                                                  "stone_1",
                                                  "stone_1",
                                                  "stone_1",
                                                  "stone_1")
        }
        val fire_pyramid:Bushery = GeneralThreeDBushery("pyramid", "vit_rock", BoundingBoxType.centered)
        val sand_pyramid:Bushery = GeneralThreeDBushery("pyramid", "sand_stone", BoundingBoxType.centered).addToBiomeType(.1, BiomeType.desert)
        val mtn_bushery = Bushery("mtn_bush_1", 7.0, .925, 1.1, BoundingBoxType.centered).addToBiomeType(.3,
                                                                                                               BiomeType.mountains)
        val wooden_wall:Bushery = ThreeDWall().also {
            it.renderData = ThreeDWall.RenderData("wood_2_v",
                                                  "wood_2_v",
                                                  "wood_2_v",
                                                  "wood_2_v",
                                                  "wood_3")
        }
        val fire_bush_1 = Bushery(TexData.fire_bush_1.id, 1.0, .925, 1.1, BoundingBoxType.centered)
        val fire_bush_2 = Bushery(TexData.fire_bush_2.id, 1.0, .925, 1.1, BoundingBoxType.centered)
        val shadow_bush_1 = Bushery(TexData.shadow_bush_1.id, 1.0, .925, 1.1, BoundingBoxType.centered)
        val shadow_bush_2 = Bushery(TexData.shadow_bush_2.id, 1.0, .925, 1.1, BoundingBoxType.centered)

        fun find(name:string?):Bushery? {
            return copy(values.find { it.id == name })
        }

        operator fun get(name:string):Bushery? {
            return copy(values.find { it.id == name })
        }

        fun init() {
            for (i in this::class.declaredMemberProperties) {
                if (i.returnType.jvmErasure == Bushery::class) {
                    values.add(((i as KProperty1<Any, *>).get(this as Any) as Bushery).also { it.id = i.name; })
                }
            }
        }

        fun copy(bushery:Bushery?):Bushery? {
            if (bushery == null)
                return null
            return bushery.copy()
        }
    }

    enum class BoundingBoxType {
        none,
        centered,
        full,
        wall
    }

}