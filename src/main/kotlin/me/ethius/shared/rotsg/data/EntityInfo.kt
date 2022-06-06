package me.ethius.shared.rotsg.data

import me.ethius.server.rotsg.world.CrystalCaveWorld
import me.ethius.server.rotsg.world.FloweringGardens
import me.ethius.server.rotsg.world.IceWorld
import me.ethius.server.rotsg.world.RottenWorld
import me.ethius.server.rotsg.world.biome.BiomeType
import me.ethius.shared.double
import me.ethius.shared.int
import me.ethius.shared.long
import me.ethius.shared.loottable.LootTableEntry
import me.ethius.shared.rotsg.entity.AEntity
import me.ethius.shared.rotsg.entity.Stat
import me.ethius.shared.rotsg.entity.ai.impl.*
import me.ethius.shared.rotsg.entity.ai.impl.voidentity.AIVoidEntityAttack
import me.ethius.shared.rotsg.entity.ai.impl.voidentity.AIVoidEntityMovement
import me.ethius.shared.rotsg.entity.enemy.Enemy
import me.ethius.shared.string
import org.apache.commons.lang3.RandomUtils
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.jvmErasure

// quick suppliers for entities. needs to be called from at least once. //
class EntityInfo<T:AEntity>(val supplier:() -> T) {

    lateinit var id:string

    operator fun invoke():T {
        if (!this::id.isInitialized) {
            id = "unknown_${RandomUtils.nextInt(0, 100000)}"
        }
        return supplier().also { it.typeId = id }
    }

    // add this entity info to a specific biome. //
    fun addToBiomeType(biomeType:BiomeType, chance:double):EntityInfo<out Enemy> {
        biomeType.entities.add(chance, this as EntityInfo<out Enemy>)
        return this as EntityInfo<out Enemy>
    }

    companion object {

        val values = HashMap<string, EntityInfo<out AEntity>>()

        val cube_entity = EntityInfo {
            Enemy.newEnemy(Enemy.Builder.newBuilder().withHealth(80).withScale(0.7).withDex(5)
                                     .withShotPattern {
                                         listOf(ProjectileProperties.salmon_proj(9..15, 5.0, 8.0, true))
                                     }.withTexData("cube")
                                     .addToLootTable(LootTableEntry.t1_gear).addToLootTable(LootTableEntry.t1_gear)
                                     .withExp(50)).also {
                it.movementAIs.add(15.0, AIWander(it))
                it.attackAIs.add(1.0, AIAttackDefault(it))
            }
        }.addToBiomeType(BiomeType.desert, 1.0)

        val minion_entity = EntityInfo {
            Enemy.newEnemy(Enemy.Builder.newBuilder().withHealth(300).withScale(0.95).withDex(3)
                                     .withShotPattern {
                                         val tmp = ArrayList<ProjectileProperties>()
                                         for (i in -5..5) {
                                             tmp.add(ProjectileProperties.salmon_proj(20..30, 2.5, 6.5, false))
                                         }
                                         tmp
                                     }.withTexData("minion")
                                     .withDef(10)
                                     .addToPortalTable(0.5) { IceWorld() }
                                     .withArcGap(32.72727)
                                     .addToLootTable(LootTableEntry.heroics).withExp(260)
                                     .emptyPortalChance(1.0)).also {
                it.movementAIs.add(1.0, AIFollowPlayer(it))
                it.movementAIs.add(6.0, AIWander(it))
                it.attackAIs.add(1.0, AIAttackDefault(it))
            }
        }.addToBiomeType(BiomeType.dark_plains, 1.0)

        val swordsman_entity = EntityInfo {
            Enemy.newEnemy(Enemy.Builder.newBuilder().withHealth(160).withDef(10).withDex(6)
                                     .withShotPattern {
                                         listOf(ProjectileProperties.basic_proj_x1)
                                     }.withTexData("swords_man")
                                     .withScale(0.85)
                                     .addToLootTable(LootTableEntry.t1_gear)
                                     .addToLootTable(LootTableEntry.t1_gear)
                                     .addToLootTable(LootTableEntry.t1_gear)
                                     .withExp(85)
                                     .emptyPortalChance(1.0)).also {
                it.movementAIs.add(1.0, AIWander(it))
                it.attackAIs.add(1.0, AIAttackDefault(it))
            }
        }.addToBiomeType(BiomeType.plains, 1.0)

        val medusa_entity = EntityInfo {
            Enemy.newEnemy(Enemy.Builder.newBuilder("/assets/data/entity/Medusa.dat")).also {
                it.movementAIs.add(1.0, AIWander(it))
                it.attackAIs.add(1.0, AIMedusaAttack(it))
            }
        }.addToBiomeType(BiomeType.mountains, 0.5)

        val flying_brain_entity = EntityInfo {
            Enemy.newEnemy(Enemy.Builder.newBuilder("/assets/data/entity/FlyingBrain.dat")).also {
                it.movementAIs.add(1.0, AIWander(it))
                it.attackAIs.add(1.0, AIAttackDefault(it))
            }
        }.addToBiomeType(BiomeType.mountains, 0.5)

        val flayer_entity = EntityInfo {
            Enemy.newEnemy(Enemy.Builder.newBuilder().withHealth(1500).withDef(15)
                                     .withTexData("flayer_entity")
                                     .withExp(1500).withScale(1.05).withShotPattern {
                    listOf(ProjectileProperties.flayer_proj_1)
                }.addToLootTable(LootTableEntry.flayer_table, LootTableEntry.t1_gear)
                                     .emptyPortalChance(1.0)
                                     .addToPortalTable(0.5) { FloweringGardens() }).also {
                it.movementAIs.add(1.0, AIWander(it))
                it.movementAIs.add(1.0, AIFollowPlayer(it))
                it.attackAIs.add(1.0, AIAttackDefault(it))
            }
        }.addToBiomeType(BiomeType.plains, 0.15)

        val rotten_dude1 = EntityInfo {
            Enemy.newEnemy(Enemy.Builder.newBuilder().withHealth(9000).withDef(20)
                                     .withTexData("rotting_dude1")
                                     .withShotPattern {
                                         listOf(ProjectileProperties.rotten_shot(true, 90..100),
                                                ProjectileProperties.rotten_shot(true, 90..100))
                                     }.addToLootTable(LootTableEntry.rotten_dude_leggys).withArcGap(30.0)
                                     .withExp(4500)
                                     .emptyPortalChance(0.6).addToPortalTable(0.1) { RottenWorld() }).also {
                it.movementAIs.add(1.0, AIFollowPlayer(it))
                it.attackAIs.add(1.0, AIAttackDefault(it))
                it.spd = 10
            }
        }.addToBiomeType(BiomeType.gore, 1.0)

        val rotten_dude2 = EntityInfo {
            Enemy.newEnemy(Enemy.Builder.newBuilder("/assets/data/entity/RottingDude_2.dat")).also {
                it.movementAIs.add(1.0, AIFollowPlayer(it))
                it.attackAIs.add(1.0, AIAttackDefault(it))
                it.spd = 10
            }
        }

        val rotten_boss = EntityInfo {
            Enemy.newEnemy(Enemy.Builder.newBuilder("/assets/data/entity/RottingBoss.dat")).also {
                it.movementAIs.add(1.0, AIWander(it))
                it.attackAIs.add(1.0, AIRottingBossAttack(it))
                it.spd = 12
            }
        }

        val hermit_crab = EntityInfo {
            Enemy.newEnemy(Enemy.Builder.newBuilder("/assets/data/entity/HermitCrab.dat")).also {
                it.movementAIs.add(1.0, AIWander(it))
                it.attackAIs.add(1.0, AIAttackDefault(it))
            }
        }.addToBiomeType(BiomeType.desert, 0.5)

        val warbringer = EntityInfo {
            Enemy.newEnemy(Enemy.Builder.newBuilder("/assets/data/entity/Warbringer.dat")).also {
                it.movementAIs.add(1.0, AIWander(it))
                it.movementAIs.add(0.3, AIFollowPlayer(it))
                it.attackAIs.add(1.0, AIAttackDefault(it))
            }
        }

        val white_demon = EntityInfo {
            Enemy.newEnemy(Enemy.Builder.newBuilder().withHealth(12000).withDef(30)
                                     .withTexData("white_demon")
                                     .withExp(10000).withDex(15).withScale(1.25).withShotPattern {
                    listOf(
                        ProjectileProperties.white_demon_shot(0.0),
                        ProjectileProperties.white_demon_shot(30.0),
                        ProjectileProperties.white_demon_shot(-30.0)
                    )
                }.addToLootTable(LootTableEntry.ice_demon_table,
                                 LootTableEntry.random_potion,
                                 LootTableEntry.random_potion)).also {
                it.movementAIs.add(1.0, AIFollowPlayer(it))
                it.attackAIs.add(1.0, AIAttackDefault(it))
                it.spd = 10
            }
        }.addToBiomeType(BiomeType.ice, 1.0)

        val flame = EntityInfo {
            Enemy.newEnemy(Enemy.Builder.newBuilder().withHealth(25000).withDef(30)
                                     .withTexData("flame")
                                     .withExp(7000)
                                     .withDex(17)
                                     .withScale(1.05)
                                     .addToLootTable(LootTableEntry.flame_table,
                                                     LootTableEntry.random_potion,
                                                     LootTableEntry.random_potion,
                                                     LootTableEntry.random_potion))
                .also { i ->
                    i.movementAIs.add(1.0, AIStayPut(i, long.MAX_VALUE).also { j -> i.currentMoveAI = j })
                    i.attackAIs.add(1.0, AIFlameEntityAttack(i).also { j -> i.currentAttackAI = j })
                    i.spd = 0
                }
        }

        val potion_entity = { stat:Stat ->
            EntityInfo {
                val lte = LootTableEntry.getPotFromStat(stat)
                Enemy.newEnemy(Enemy.Builder.newBuilder()
                                         .withHealth(30000)
                                         .withDef(30)
                                         .withDex(17)
                                         .withExp(10000)
                                         .withScale(1.05)
                                         .addToLootTable(lte, lte, lte, lte, lte, lte, lte, lte)
                                         .withTexData("potion_dude")
                ).also {
                    it.pushData("stat", stat)
                    it.movementAIs.add(1.0, AIStayPut(it, long.MAX_VALUE).also { j -> it.currentMoveAI = j })
                    it.attackAIs.add(1.0, AIPotionEntityAttack(it).also { j -> it.currentAttackAI = j })
                    if (stat == Stat.mana) {
                        it.lootTable.add(LootTableEntry.radiant_ring)
                    }
                }
            }
        }

        val fb_clone_1 = EntityInfo {
            Enemy.newEnemy(Enemy.Builder.newBuilder().withHealth(5000).withDef(10).withExp(2000)
                                     .withTexData("fb_1").withDex(20).withShotPattern {
                    listOf(ProjectileProperties.fb_1_proj)
                }.addToLootTable(LootTableEntry.random_potion, LootTableEntry.random_potion)
                                     .emptyPortalChance(1.5).addToPortalTable(0.9) { CrystalCaveWorld() }).also {
                it.attackAIs.add(1.0, AIAttackDefault(it))
                it.movementAIs.add(0.5, AIFollowPlayer(it))
                it.movementAIs.add(0.5, AIStayPut(it, 1000L))
            }
        }

        val fb_clone_2 = EntityInfo {
            Enemy.newEnemy(Enemy.Builder.newBuilder().withHealth(5000).withDef(10)
                                     .withTexData("fb_2")
                                     .withExp(2000).withDex(20).withShotPattern {
                    listOf(ProjectileProperties.fb_2_proj, ProjectileProperties.fb_2_proj)
                }.addToLootTable(LootTableEntry.random_potion, LootTableEntry.random_potion)
                                     .emptyPortalChance(1.5).addToPortalTable(0.9) { CrystalCaveWorld() }).also {
                it.attackAIs.add(1.0, AIAttackDefault(it))
                it.movementAIs.add(0.5, AIWander(it))
                it.movementAIs.add(0.5, AIStayPut(it, 500L))
            }
        }

        val fb_clone_3 = EntityInfo {
            Enemy.newEnemy(Enemy.Builder.newBuilder().withHealth(5000).withDef(10)
                                     .withTexData("fb_3")
                                     .withExp(2000).withDex(20).withShotPattern {
                    listOf(ProjectileProperties.fb_3_proj, ProjectileProperties.fb_3_proj, ProjectileProperties.fb_3_proj)
                }.addToLootTable(LootTableEntry.random_potion, LootTableEntry.random_potion)
                                     .emptyPortalChance(1.5).addToPortalTable(0.9) { CrystalCaveWorld() }).also {
                it.attackAIs.add(1.0, AIAttackDefault(it))
                it.movementAIs.add(0.5, AIWander(it))
                it.movementAIs.add(0.5, AIStayPut(it, 1000L))
            }
        }

        val void_dude = EntityInfo {
            Enemy.newEnemy(Enemy.Builder.newBuilder().withHealth(30000).withDef(20).withExp(8000)
                                     .withScale(1.05)
                                     .withDex(17)
                                     .withTexData("void_dude")
                                     .addToLootTable(LootTableEntry.void_dude_table,
                                                     LootTableEntry.random_potion,
                                                     LootTableEntry.random_potion)
                                     .emptyPortalChance(1.5)).also {
                it.attackAIs.add(1.0, AIVoidEntityAttack(it).also { j -> it.currentAttackAI = j })
                it.movementAIs.add(1.0, AIVoidEntityMovement(it).also { j -> it.currentMoveAI = j })
            }
        }

        val metallic_robot = EntityInfo {
            Enemy.newEnemy(Enemy.Builder.newBuilder("/assets/data/entity/MetallicRobot.dat")).also {
                it.attackAIs.add(1.0, AIMetallicRobotAttack(it).also { j -> it.currentAttackAI = j })
                it.movementAIs.add(1.0, AIWander(it).also { j -> it.currentMoveAI = j })
            }
        }

        val crystal_dude = EntityInfo {
            Enemy.newEnemy(Enemy.Builder.newBuilder("/assets/data/entity/CrystalDude.dat")).also {
                it.attackAIs.add(1.0, AICrystalDudeAttack(it).also { j -> it.currentAttackAI = j })
                it.movementAIs.add(1.0, AIWander(it).also { j -> it.currentMoveAI = j })
            }
        }

        val elemental_dude = EntityInfo {
            Enemy.newEnemy(Enemy.Builder.newBuilder("/assets/data/entity/ElementalDude.dat")).also {
                it.currentAttackAI = AIElementalDudeAttack(it)
            }
        }

        val shadow_scale = EntityInfo {
            Enemy.newEnemy(Enemy.make {
                scale = 1.6
                health = 25000
                def = 20
                exp = 5000
                speed = 30
                texDataId = "shadow_scale"
                dex = 6
                shotPattern = {
                    it.pushDataIfAbsent("shots", 0)
                    val list = mutableListOf<ProjectileProperties>()
                    when (it.getData<int>("shots") % 2) {
                        1 -> {
                            it.arcGap = 8.0
                            list.addAll(arrayOf(ProjectileProperties.shadow_scale_proj_1, ProjectileProperties.shadow_scale_proj_1, ProjectileProperties.shadow_scale_proj_1))
                        }
                        0 -> {
                            it.arcGap = 15.0
                            list.addAll(arrayOf(ProjectileProperties.shadow_scale_proj_2, ProjectileProperties.shadow_scale_proj_2, ProjectileProperties.shadow_scale_proj_2))
                        }
                    }
                    it.pushData("shots", it.getData<int>("shots") + 1)
                    list
                }
                lootTable += LootTableEntry.shadow_scale_leggys
            }).also {
                it.attackAIs.add(1.0, AIAttackDefault(it).also { j -> it.currentAttackAI = j })
                it.movementAIs.add(1.0, AIWander(it).also { j -> it.currentMoveAI = j })
            }
        }

        val fire_breather = EntityInfo {
            Enemy.newEnemy(Enemy.make {
                scale = 1.6
                health = 25000
                def = 20
                exp = 5000
                speed = 40
                texDataId = "fire_breather"
                dex = 6
                shotPattern = {
                    it.pushDataIfAbsent("shots", 0)
                    val list = mutableListOf<ProjectileProperties>()
                    when (it.getData<int>("shots") % 2) {
                        1 -> {
                            it.arcGap = 8.0
                            list.addAll(arrayOf(ProjectileProperties.fire_breather_proj_1, ProjectileProperties.fire_breather_proj_1, ProjectileProperties.fire_breather_proj_1))
                        }
                        0 -> {
                            it.arcGap = 12.0
                            list.addAll(arrayOf(ProjectileProperties.fire_breather_proj_2,
                                                ProjectileProperties.fire_breather_proj_2,
                                                ProjectileProperties.fire_breather_proj_2,
                                                ProjectileProperties.fire_breather_proj_2))
                        }
                    }
                    it.pushData("shots", it.getData<int>("shots") + 1)
                    list
                }
            }).also {
                it.attackAIs.add(1.0, AIAttackDefault(it).also { j -> it.currentAttackAI = j })
                it.movementAIs.add(1.0, AIWander(it).also { j -> it.currentMoveAI = j })
            }
        }

        operator fun get(name:string):EntityInfo<out AEntity>? {
            return values[name]
        }

        fun init() {
            for (i in this::class.declaredMemberProperties) {
                if (i.returnType.jvmErasure == EntityInfo::class) {
                    values[i.name] = ((i as KProperty1<Any, *>).get(this as Any) as EntityInfo<out AEntity>).also {
                        it.id = i.name
                    }
                }
            }
        }
    }

}