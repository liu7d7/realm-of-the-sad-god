package me.ethius.shared.rotsg.entity.ai.impl

import me.ethius.server.rotsg.world.ServerWorld
import me.ethius.shared.bool
import me.ethius.shared.calcAngle
import me.ethius.shared.getRandomInRange
import me.ethius.shared.measuringTimeMS
import me.ethius.shared.rotsg.data.ProjectileProperties
import me.ethius.shared.rotsg.entity.ai.AIBase
import me.ethius.shared.rotsg.entity.enemy.Enemy
import org.apache.commons.lang3.RandomUtils
import kotlin.math.roundToInt

class AIRottingBossAttack(entityIn:Enemy):AIBase(entityIn) {
    override val type:Type = Type.attack
    private val aiStayPut = AIStayPut(entityIn, 1000000000000L)
    private var lastShot = 0f

    private val shots = Array(6) { it * 60.0 }
    private val shotDirs = Array(6) { if (RandomUtils.nextBoolean()) -1 else 1 }

    override fun update() {
        val world = entity.world as? ServerWorld ?: return
        val target = world.closestPlayer(entity) ?: return
        when (entity.hp.roundToInt()) {
            in 178000 downTo 100000 -> {
                if (measuringTimeMS() - lastShot >= 500f) {
                    var angle = calcAngle(entity, target)
                    angle += RandomUtils.nextFloat(0f, 40f)
                    angle -= 20f
                    entity.shoot(ProjectileProperties.rotting_boss_proj).also { it.r = angle }
                    lastShot = measuringTimeMS()
                }
            }
            in 100000 downTo 50000 -> {
                if (measuringTimeMS() - lastShot >= 500f) {
                    val angle = calcAngle(entity, target)
                    for (i in 0..2) {
                        entity.shoot(ProjectileProperties.rotting_boss_proj)
                            .also { it.r = angle + getRandomInRange(-40f, 40f) }
                    }
                    lastShot = measuringTimeMS()
                }
            }
            in 50000 downTo 0 -> {
                if (entity.currentMoveAI !is AIStayPut) {
                    entity.currentMoveAI = aiStayPut
                }

                if (entity.delayNumSeconds(0.04)) {
                    for (i in 0..6) {
                        entity.shoot(ProjectileProperties.rotting_boss_proj).also {
                            it.r = shots[i]
                            it.projProps = it.projProps.copy()
                            it.projProps.lifetime = ProjectileProperties.ms(6.0, it.projProps.speed)
                            it.projProps.frequency = 1.0
                            it.projProps.amplitude = 0.5
                        }
                    }

                    for (i in 0..5) {
                        shots[i] += shotDirs[i] * 2.0
                    }

                    if (entity.delayNumSeconds(0.6)) {
                        for (i in 0..5) {
                            shotDirs[i] = if (RandomUtils.nextBoolean()) -1 else 1
                        }
                    }
                }
            }
        }
    }

    override fun isDone():bool {
        return false
    }
}