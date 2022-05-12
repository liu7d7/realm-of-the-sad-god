package me.ethius.server.rotsg.world.biome

import me.ethius.shared.double
import me.ethius.shared.int
import me.ethius.shared.long
import me.ethius.shared.maths.WeightedCollection
import me.ethius.shared.rotsg.world.biome.BiomeFeature
import me.ethius.shared.rotsg.data.EntityInfo
import me.ethius.shared.rotsg.entity.enemy.Enemy
import me.ethius.shared.rotsg.tile.Bushery
import me.ethius.shared.string
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.jvmErasure

class BiomeType(
    val texDataId:string,
    val minRad:int,
    val maxRad:int,
    val spawnTime:long = 2000L,
    val weight:double = 1.0,
    val maxFeatures:int = 2,
    private val emptyChance:double = 1.0,
    val entities:WeightedCollection<EntityInfo<out Enemy>> = WeightedCollection(),
    val bushery:WeightedCollection<Bushery> = WeightedCollection(),
    val features:WeightedCollection<BiomeFeature.Data?> = WeightedCollection(),
) {

    var id = ""

    private fun emptyChance():BiomeType {
        features.add(emptyChance, null as BiomeFeature.Data?)
        return this
    }

    companion object {

        private val values = ArrayList<BiomeType>()

        private val weightedBiomes = WeightedCollection<BiomeType>()

        val desert = BiomeType("sand_1", 18, 30)
        val plains = BiomeType("grass_1", 18, 30, 1000L, 1.0)
        val lake = BiomeType("water_1", 12, 18, -1, 0.8)
        val dark_plains = BiomeType("grass_2", 18, 26, 1600L)
        val gore = BiomeType("gore_1", 9, 18, 1200L, 0.3)
        val mountains = BiomeType("mtns_1", 30, 48, 3000L, 0.0)
        val ice = BiomeType("snow_1", -1, -1, 2000L, 0.0, 10, 200.0).emptyChance()

        fun next():BiomeType {
            return weightedBiomes.next()!!
        }

        fun init() {
            for (i in this::class.declaredMemberProperties) {
                if (i.returnType.jvmErasure == BiomeType::class) {
                    val bt = ((i as KProperty1<Any, *>).get(this as Any) as BiomeType)
                    bt.id = i.name
                    values.add(bt)
                    if (bt.weight > 0) {
                        weightedBiomes.add(bt.weight, bt)
                    }
                }
            }
            for (it in values) {
                weightedBiomes.add(it.weight, it)
            }
        }
    }

}