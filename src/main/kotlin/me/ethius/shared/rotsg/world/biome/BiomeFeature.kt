package me.ethius.shared.rotsg.world.biome

import com.moandjiezana.toml.Toml
import me.ethius.server.rotsg.world.biome.BiomeType
import me.ethius.shared.double
import me.ethius.shared.ext.ZERO2i
import me.ethius.shared.ivec2
import me.ethius.shared.opti.TexData
import me.ethius.shared.readCached
import me.ethius.shared.rotsg.data.WorldBuilderData
import me.ethius.shared.rotsg.tile.Bushery
import me.ethius.shared.rotsg.tile.Tile
import me.ethius.shared.string
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.jvmErasure

const val feature_data_dir = "/assets/data/feature"

open class BiomeFeature {

    var tlPos:ivec2 = ZERO2i
    var feature:Array<Tile> = emptyArray()

    constructor()

    constructor(tlPos:ivec2, feature:Array<Tile>):this() {
        this.tlPos = tlPos
        this.feature = feature
    }

    constructor(tlPos:ivec2, feature:Array<Array<Tile?>>):this() {
        this.tlPos = tlPos
        this.feature = feature.flatten().filterNotNull().toTypedArray()
    }

    constructor(tlPos:ivec2, loc:string):this() {
        this.tlPos = tlPos
        val toml = Toml().readCached("$feature_data_dir/$loc.dat")
        val e = toml.to(WorldBuilderData::class.java)
        this.feature = e.data.map { Tile(it.pos, it.texData, Bushery[it.env]) }.toTypedArray()
    }

    class Data(val supplier:(ivec2) -> BiomeFeature) {
        lateinit var id:string

        operator fun invoke(ivec2:ivec2):BiomeFeature {
            return supplier(ivec2)
        }

        fun addToBiomeData(biomeType:BiomeType, weight:double):Data {
            biomeType.features.add(weight, this)
            return this
        }
    }

    companion object {
        val values = mutableListOf<Data>()

        val crystal_dude_pedestal = Data { pos ->
            return@Data BiomeFeature(pos, "CrystalDudePedestal")
        }

        val ice_crevasse = Data { pos ->
            return@Data BiomeFeature(pos, ice_crevasse_data())
        }.addToBiomeData(BiomeType.ice, 1.0)

        val crystal_room_1 = Data { pos ->
            return@Data BiomeFeature(pos, "CrystalRoom_1")
        }
        val crystal_room_2 = Data { pos ->
            return@Data BiomeFeature(pos, "CrystalRoom_2")
        }

        val forbidden_jungle_copy = Data { pos ->
            return@Data BiomeFeature(pos, "ForbiddenJungleCopy")
        }

        val potion_thingy = Data { pos ->
            return@Data BiomeFeature(pos, "PotionThingy")
        }

        val cave_floor_spread = Data { pos ->
            return@Data BiomeFeature(pos, "CaveFloorSpread")
        }

        val metallic_robot_pedestal = Data { pos ->
            return@Data BiomeFeature(pos, "MetallicRobotPedestal")
        }

        val rotten_world = Data { pos ->
            return@Data BiomeFeature(pos, "RottenWorld")
        }

        val elemental_pedestal = Data { pos ->
            return@Data BiomeFeature(pos, "ElementalPedestal")
        }

        val warbringer_pedestal = Data { pos ->
            return@Data BiomeFeature(pos, "WarbringerPedestal")
        }

        val nexus = Data { pos ->
            return@Data BiomeFeature(pos, "Nexus")
        }

        val fire_breather_pedestal = Data { pos ->
            return@Data BiomeFeature(pos, "FireBreatherPedestal")
        }

        val shadow_scale_pedestal = Data { pos ->
            return@Data BiomeFeature(pos, "ShadowScalePedestal")
        }

        fun init() {
            for (i in this::class.declaredMemberProperties) {
                if (i.returnType.jvmErasure == Data::class) {
                    values.add(((i as KProperty1<Any, *>).get(this as Any) as Data).also { it.id = i.name; })
                }
            }
        }

        // data_section
        private val ice_crevasse_data:() -> Array<Array<Tile?>> = {
            val supplier = {
                val b = Tile(TexData.ice_1, null)
                b.slippy = 0.81f
                b
            }
            arrayOf(
                arrayOf(supplier()),
                arrayOf(null, supplier(), supplier()),
                arrayOf(null, supplier(), supplier(), supplier()),
                arrayOf(null, null, supplier(), supplier()),
                arrayOf(null, null, null, supplier(), supplier())
            )
        }

    }

}