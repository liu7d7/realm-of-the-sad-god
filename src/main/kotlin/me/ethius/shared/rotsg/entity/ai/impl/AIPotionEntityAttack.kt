package me.ethius.shared.rotsg.entity.ai.impl

import me.ethius.server.rotsg.world.ServerWorld
import me.ethius.shared.bool
import me.ethius.shared.calcAngle
import me.ethius.shared.measuringTimeMS
import me.ethius.shared.rotsg.data.ProjectileData
import me.ethius.shared.rotsg.entity.Stat
import me.ethius.shared.rotsg.entity.ai.AIBase
import me.ethius.shared.rotsg.entity.enemy.Enemy
import me.ethius.shared.rotsg.entity.other.Projectile
import me.ethius.shared.wrapDegrees
import org.apache.commons.lang3.RandomUtils
import kotlin.math.roundToInt

class AIPotionEntityAttack(entityIn:Enemy):AIBase(entityIn) {
    override val type:Type = Type.attack

    private val stage:Stage
        get() {
            return when (entity.hp.roundToInt()) {
                in 30000 downTo 24000 -> Stage.begin
                in 24000 downTo 18000 -> Stage.radial
                in 18000 downTo 12000 -> Stage.rotation
                in 12000 downTo 6000 -> Stage.circles
                else -> Stage.last_hope
            }
        }

    private val stat:Stat = entityIn.getData("stat")
    private var lastAction = 0f
        set(value) {
            field = value
            uses++
        }
    private var uses = 0

    override fun update() {
        val world = entity.world as? ServerWorld ?: return
        val target = world.closestPlayer(entity) ?: return
        val time = measuringTimeMS()
        when (stage) {
            Stage.begin -> {
                if (time - lastAction >= 1000) {
                    for (i in -5..5) {
                        world.addEntity(Projectile().reset(entity, getProjInfo(), i * 36.0))
                    }
                    lastAction = time
                }
            }
            Stage.radial -> {
                if (time - lastAction >= 500) {
                    val angle = wrapDegrees(calcAngle(target.y - entity.y,
                                                      target.x - entity.x))
                    world.addEntity(Projectile().reset(entity, getProjInfo(), angle))
                    world.addEntity(Projectile().reset(entity, getProjInfo(), angle + 72))
                    world.addEntity(Projectile().reset(entity, getProjInfo(), angle + 144))
                    world.addEntity(Projectile().reset(entity, getProjInfo(), angle + 216))
                    world.addEntity(Projectile().reset(entity, getProjInfo(), angle + 288))
                    lastAction = time
                }
            }
            Stage.rotation -> {
                if (time - lastAction >= 100) {
                    for (i in 0..5) {
                        world.addEntity(Projectile().reset(entity, getProjInfo(), i * 60.0 + uses * 3.0))
                    }
                    lastAction = time
                }
            }
            Stage.circles -> {
                if (time - lastAction >= 300) {
                    for (i in 0..8) {
                        world.addEntity(Projectile().reset(entity, getProjInfo(), i * 40.0))
                    }
                    lastAction = time
                }
            }
            Stage.last_hope -> {
                if (time - lastAction >= 1500) {
                    val rand = RandomUtils.nextInt(0, 24)
                    for (i in 0..23) {
                        if (rand != i) {
                            world.addEntity(Projectile().reset(entity, getProjInfo(), i * 15.0))
                        }
                    }
                    lastAction = time
                }
            }
        }
    }

    override fun isDone():bool {
        return false
    }

    private fun getProjInfo():ProjectileData {
        return when (stage) {
            Stage.begin -> {
                ProjectileData(stat.potion, 0.0, 0.0, 7.0, 10.0, true, false, false, true, 40..95).also {
                    it.spinSpeed = 50.0
                }
            }
            Stage.radial -> {
                ProjectileData(stat.potion,
                               0.0,
                               0.0,
                               7.0,
                               10.0,
                               true,
                               false,
                               false,
                               false,
                               60..105).also { it.spinSpeed = 50.0 }
            }
            Stage.rotation -> {
                ProjectileData(stat.potion,
                               0.0,
                               0.0,
                               7.0,
                               10.0,
                               true,
                               false,
                               false,
                               false,
                               60..105).also { it.spinSpeed = 50.0 }
            }
            Stage.circles -> {
                ProjectileData(stat.potion,
                               0.0,
                               0.0,
                               7.0,
                               10.0,
                               true,
                               false,
                               false,
                               true,
                               60..115).also { it.spinSpeed = 50.0 }
            }
            Stage.last_hope -> {
                ProjectileData(stat.potion,
                               0.0,
                               0.0,
                               11.0,
                               10.0,
                               true,
                               false,
                               false,
                               true,
                               80..135).also { it.spinSpeed = 50.0 }
            }
        }
    }


    private enum class Stage {
        begin,
        radial,
        rotation,
        circles,
        last_hope
    }
}