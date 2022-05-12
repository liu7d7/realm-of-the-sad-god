package me.ethius.server.rotsg.world

import me.ethius.shared.*
import me.ethius.shared.rotsg.data.EntityInfo
import me.ethius.shared.rotsg.entity.AEntity
import me.ethius.shared.rotsg.entity.Stat
import me.ethius.shared.rotsg.entity.enemy.Enemy
import me.ethius.shared.rotsg.world.biome.BiomeFeature
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.jvmErasure

abstract class WorldEvent {
    lateinit var id:string

    lateinit var pos:ivec2
    val entities = mutableListOf<AEntity>()

    fun executeAtPos(world:ServerWorld, x:int, y:int) {
        pos = ivec2(x, y)
        try {
            execute(world, x, y)
        } catch (e:Exception) {
            if (world.name == "realm") {
                (world as Realm).worldEvent = null
            }
            e.printStackTrace()
        }
    }

    protected abstract fun execute(world:ServerWorld, x:int, y:int)

    open fun isFinished(world:ServerWorld):bool {
        return entities.all { !it.alive }.also { if (it) entities.clear() }
    }

    fun addEntity(world:ServerWorld, entity:AEntity) {
        entities.add(entity)
        world.addEntity(entity)
    }

    fun addEntity(world:ServerWorld, entity:EntityInfo<out Enemy>, pos:ivec2) {
        entities.add(world.addEntity(entity, pos))
    }

    companion object {
        val values = mutableListOf<WorldEvent>()

        val forbidden_jungle_copy = object:WorldEvent() {
            override fun execute(world:ServerWorld, x:int, y:int) {
                val feature = BiomeFeature.forbidden_jungle_copy(ivec2(x, y))
                world.addFeature(feature)
                addEntity(world, EntityInfo.flame, ivec2(x + 11, y + 9))
                val arr = arrayOf(EntityInfo.fb_clone_1, EntityInfo.fb_clone_2, EntityInfo.fb_clone_3)
                for (i in fb_enemy_poses) {
                    addEntity(world, arr.random(), i.copy().add(x, y))
                }
            }
        }

        val potion_thingy = object:WorldEvent() {
            override fun execute(world:ServerWorld, x:int, y:int) {
                val stat = Stat.values().random()
                val feature = BiomeFeature.potion_thingy(ivec2(x, y))
                feature.feature.forEach { if (it.texDataId == "empty") it.texDataId = stat.rock.id }
                world.addFeature(feature)
                addEntity(world, EntityInfo.potion_entity(stat), ivec2(x + 5, y + 5))
            }
        }

        val warbringer = object:WorldEvent() {
            override fun execute(world:ServerWorld, x:int, y:int) {
                val feature = BiomeFeature.warbringer_pedestal(ivec2(x, y))
                world.addFeature(feature)
                addEntity(world, EntityInfo.warbringer, ivec2(x + 5, y + 5))
            }
        }

        val void_thing = object:WorldEvent() {
            override fun execute(world:ServerWorld, x:int, y:int) {
                val feature = BiomeFeature.cave_floor_spread(ivec2(x, y))
                world.addFeature(feature)
                addEntity(world, EntityInfo.void_dude, ivec2(x + 5, y + 5))
            }
        }

        val metallic_robot = object:WorldEvent() {
            override fun execute(world:ServerWorld, x:int, y:int) {
                val feature = BiomeFeature.metallic_robot_pedestal(ivec2(x, y))
                world.addFeature(feature)
                addEntity(world, EntityInfo.metallic_robot, ivec2(x + 5, y + 6))
            }
        }

        val crystal_dude = object:WorldEvent() {
            override fun execute(world:ServerWorld, x:int, y:int) {
                val feature = BiomeFeature.crystal_dude_pedestal(ivec2(x, y))
                world.addFeature(feature)
                addEntity(world, EntityInfo.crystal_dude, ivec2(x + 8, y + 8))
            }
        }

        val elemental_dude = object:WorldEvent() {
            override fun execute(world:ServerWorld, x:int, y:int) {
                val feature = BiomeFeature.elemental_pedestal(ivec2(x, y))
                world.addFeature(feature)
                addEntity(world, EntityInfo.elemental_dude, ivec2(x + 18, y + 18))
            }
        }

        val shadow_scale = object:WorldEvent() {
            override fun execute(world:ServerWorld, x:int, y:int) {
                val feature = BiomeFeature.shadow_scale_pedestal(ivec2(x, y))
                world.addFeature(feature)
                addEntity(world, EntityInfo.shadow_scale, ivec2(x + 16, y + 17))
            }
        }

        val fire_breather = object:WorldEvent() {
            override fun execute(world:ServerWorld, x:int, y:int) {
                val feature = BiomeFeature.fire_breather_pedestal(ivec2(x, y))
                world.addFeature(feature)
                addEntity(world, EntityInfo.fire_breather, ivec2(x + 20, y + 17))
            }
        }

        fun init() {
            for (i in this::class.declaredMemberProperties) {
                if (i.returnType.jvmErasure == WorldEvent::class) {
                    values.add(((i as KProperty1<Any, *>).get(this as Any) as WorldEvent).also { it.id = i.name })
                }
            }
        }


        // data section
        val fb_enemy_poses = arrayOf(
            ivec2(5, 3),
            ivec2(5, 13),
            ivec2(19, 10),
            ivec2(11, 13),
            ivec2(17, 1),
            ivec2(19, 5),
            ivec2(24, 4),
            ivec2(25, 5),
            ivec2(3, 16),
            ivec2(8, 18),
            ivec2(13, 20),
            ivec2(16, 17),
            ivec2(23, 12),
            ivec2(18, 21),
            ivec2(11, 18),
            ivec2(10, 22),
            ivec2(27, 8),
            ivec2(27, 17),
        )
    }
}