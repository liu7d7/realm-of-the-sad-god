package me.ethius.shared.rotsg.entity.ai.impl

import me.ethius.server.rotsg.world.ServerWorld
import me.ethius.shared.bool
import me.ethius.shared.calcAngle
import me.ethius.shared.long
import me.ethius.shared.measuringTimeMS
import me.ethius.shared.rotsg.data.ProjectileProperties
import me.ethius.shared.rotsg.entity.ai.AIBase
import me.ethius.shared.rotsg.entity.enemy.Enemy
import org.apache.commons.lang3.RandomUtils
import kotlin.math.roundToInt

class AIMetallicRobotAttack(entityIn:Enemy):AIBase(entityIn) {
    override val type:Type = Type.attack

    private var shot1dirs = doubleArrayOf(-1.0, 1.0, -1.0, 1.0, -1.0, 1.0)
    private var shot1 = doubleArrayOf(0.0, 120.0, 240.0, 0.0, 120.0, 240.0)

    override fun update() {
        val world = entity.world as? ServerWorld ?: return
        val target = world.closestPlayer(entity) ?: return
        when (entity.hp.roundToInt()) {
            // starting phase, switch between hammer throws and moonbeams
            in 25000 downTo 16000 -> {
                if (entity.delayNumSeconds(1.0)) {
                    val angle = calcAngle(entity, target) + 90f
                    entity.shoot(ProjectileProperties.metallic_robot_proj_1).also { it.r = -30.0 + angle }
                    entity.shoot(ProjectileProperties.metallic_robot_proj_1).also { it.r = 0.0 + angle }
                    entity.shoot((ProjectileProperties.metallic_robot_proj_1)).also { it.r = 30.0 + angle }
                } else if (entity.delayNumSeconds(1.7)) {
                    for (i in 0..2) {
                        entity.shoot(ProjectileProperties.metallic_robot_proj_2).also { it.r = i * 120.0 + 30.0 }
                        entity.shoot(ProjectileProperties.metallic_robot_proj_2).also { it.r = i * 120.0 + 60.0 }
                        entity.shoot(ProjectileProperties.metallic_robot_proj_2).also { it.r = i * 120.0 + 90.0 }
                    }
                }
            }
            // phase 2, massive amount of moonbeams
            in 16000 downTo 8000 -> {
                if (entity.currentMoveAI !is AIStayPut) {
                    entity.currentMoveAI = AIStayPut(entity, long.MAX_VALUE)
                }
                if (entity.delayNumSeconds(0.1)) {
                    for (i in 0..5) {
                        shot1[i] += shot1dirs[i] * 3f
                    }
                    entity.shoot(ProjectileProperties.metallic_robot_proj_2).also { it.r = shot1[0]; }
                    entity.shoot(ProjectileProperties.metallic_robot_proj_2).also { it.r = shot1[1]; }
                    entity.shoot(ProjectileProperties.metallic_robot_proj_2).also { it.r = shot1[2]; }
                    entity.shoot(ProjectileProperties.metallic_robot_proj_2).also { it.r = shot1[3]; }
                    entity.shoot(ProjectileProperties.metallic_robot_proj_2).also { it.r = shot1[4]; }
                    entity.shoot(ProjectileProperties.metallic_robot_proj_2).also { it.r = shot1[5]; }
                }
                if (entity.delayNumSeconds(0.75)) {
                    for (i in 0..5) {
                        if (RandomUtils.nextBoolean()) {
                            shot1dirs[i] = -shot1dirs[i]
                        }
                    }
                }
            }
            // last phase, massive amount of spinners
            else -> {
                if (entity.delayNumSeconds(0.1)) {
                    for (i in 0..19) {
                        entity.shoot((ProjectileProperties.metallic_robot_proj_3))
                            .also { it.r = i * 18.0 + 30.0 + ((measuringTimeMS() * 0.5) % 360.0); }
                    }
                }
            }
        }
    }

    override fun isDone():bool {
        return false
    }
}