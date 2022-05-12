package me.ethius.shared.loottable

import me.ethius.shared.double
import me.ethius.shared.maths.WeightedCollection
import me.ethius.shared.rotsg.entity.Stat
import me.ethius.shared.string
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.jvmErasure

class LootTableEntry:WeightedCollection<string?>() {

    lateinit var id:string

    fun addPair(weight:double, result:string?):LootTableEntry {
        if (result != null) {
            super.add(weight, result)
        } else {
            super.add(weight, null as string?)
        }
        return this
    }

    override fun equals(other:Any?):Boolean {
        if (other !is LootTableEntry) return false
        return id == other.id
    }

    override fun hashCode():Int {
        return id.hashCode()
    }

    companion object {
        val values = ArrayList<LootTableEntry>()

        val empty = LootTableEntry()

        val t1_gear = LootTableEntry()
            .addPair(0.2, "basic_katana_2")
            .addPair(0.2, "light_armor_1")
            .addPair(0.2, "heavy_armor_1")
            .addPair(0.2, "basic_ring")
            .addPair(1.6, null)

        val t2_gear = LootTableEntry()
            .addPair(0.2, "ninja_star_2")
            .addPair(0.2, "quiver_2")
            .addPair(0.2, "adv_dasher")
            .addPair(1.2, null)

        val heroics = LootTableEntry()
            .addPair(0.2, "moonbeam_katana")
            .addPair(0.2, "adv_dirk")
            .addPair(0.2, "doom_bow")
            .addPair(0.2, "agateclaw_dagger")
            .addPair(2.2, null)

        val flayer_table = LootTableEntry()
            .addPair(0.3, "light_armor_2")
            .addPair(0.1, "colo_sword")
            .addPair(2.7, null)

        val rotten_dude_leggys = LootTableEntry()
            .addPair(0.2, "bone_ring")
            .addPair(0.2, "bone_armor")
            .addPair(2.7, null)

        val ice_demon_table = LootTableEntry()
            .addPair(0.2, "frozen_katana")
            .addPair(0.2, "ice_bow")
            .addPair(1.8, null)

        val flame_table = LootTableEntry()
            .addPair(0.2, "molten_katana")
            .addPair(1.0, null)

        val rotten_dude_primals = LootTableEntry()
            .addPair(0.2, "rotting_arm")
            .addPair(1.0, null)

        val random_potion = LootTableEntry()
            .addPair(0.2, "vit_potion")
            .addPair(0.2, "atk_potion")
            .addPair(0.2, "def_potion")
            .addPair(0.2, "spd_potion")
            .addPair(0.2, "life_potion")
            .addPair(0.2, "mana_potion")
            .addPair(0.2, "wis_potion")
            .addPair(1.2, null)

        val void_dude_table = LootTableEntry()
            .addPair(0.2, "stardust_cutter")
            .addPair(0.2, "void_bow")
            .addPair(1.2, null)

        val wis_potion = LootTableEntry()
            .addPair(0.2, "wis_potion")
            .addPair(0.3, null)

        val life_potion = LootTableEntry()
            .addPair(0.2, "life_potion")
            .addPair(0.3, null)

        val mana_potion = LootTableEntry()
            .addPair(0.2, "mana_potion")
            .addPair(0.3, null)

        val vit_potion = LootTableEntry()
            .addPair(0.2, "vit_potion")
            .addPair(0.3, null)

        val atk_potion = LootTableEntry()
            .addPair(0.2, "atk_potion")
            .addPair(0.3, null)

        val def_potion = LootTableEntry()
            .addPair(0.2, "def_potion")
            .addPair(0.3, null)

        val spd_potion = LootTableEntry()
            .addPair(0.2, "spd_potion")
            .addPair(0.3, null)

        val dex_potion = LootTableEntry()
            .addPair(0.2, "dex_potion")
            .addPair(0.3, null)

        val metallic_robot_table = LootTableEntry()
            .addPair(0.2, "kalon")
            .addPair(0.2, "murena")
            .addPair(1.4, null)

        val crystal_dude_table = LootTableEntry()
            .addPair(0.2, "dual_crystal_cutters")
            .addPair(1.3, null)

        val warbringer_leggys = LootTableEntry()
            .addPair(0.2, "warbringers_bow")
            .addPair(0.2, "warbringers_dagger")
            .addPair(0.2, "warbringers_katana")
            .addPair(1.8, null)

        val shadow_scale_leggys = LootTableEntry()
            .addPair(0.2, "shadow_crusher")
            .addPair(0.6, null)

        val fire_breather_leggys = LootTableEntry()
            .addPair(0.2, "fire_breather_tail")
            .addPair(0.6, null)

        val flying_brain_leggys = LootTableEntry()
            .addPair(0.2, "stem_of_the_brain")
            .addPair(0.6, null)


        fun getPotFromStat(stat:Stat):LootTableEntry {
            return when (stat) {
                Stat.wis -> wis_potion
                Stat.life -> life_potion
                Stat.mana -> mana_potion
                Stat.vit -> vit_potion
                Stat.atk -> atk_potion
                Stat.def -> def_potion
                Stat.spd -> spd_potion
                Stat.dex -> dex_potion
            }
        }

        fun find(name:String):LootTableEntry {
            return values.find { it.id == name } ?: empty
        }

        fun init() {
            for (i in this::class.declaredMemberProperties) {
                if (i.returnType.jvmErasure == LootTableEntry::class) {
                    values.add((i as KProperty1<Companion, LootTableEntry>).get(this).also { it.id = i.name })
                }
            }
        }
    }

}