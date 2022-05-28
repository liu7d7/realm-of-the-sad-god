package me.ethius.shared.rotsg.entity.ai.impl

import me.ethius.shared.bool
import me.ethius.shared.rotsg.data.ProjectileData
import me.ethius.shared.rotsg.data.ProjectileData.Companion.ms
import me.ethius.shared.rotsg.entity.ai.AIBase
import me.ethius.shared.rotsg.entity.enemy.Enemy
import kotlin.math.roundToInt

class AIElementalDudeAttack(entityIn:Enemy):AIBase(entityIn) {
    override val type:Type = Type.attack
    private val wander = AIWander(entityIn)
    private val circlePoint = AICirclePoint(entityIn, 6.0) { false }

    override fun update() {
        when (entity.hp.roundToInt()) {
            in 12_000 downTo 6_000 -> // blu
            {
                if (entity.texDataId != "blue_elemental_dude") {
                    entity.setTexData("blue_elemental_dude")
                }
                if (entity.currentMoveAI !is AIWander) {
                    entity.currentMoveAI = wander
                }
                if (entity.delayNumSeconds(0.3)) {
                    entity.shoot(ProjectileData.elemental_dude_proj_2.also {
                        it.baseAngle = 30.0; it.lifetime = ms(4.0, it.speed)
                    })
                    entity.shoot(ProjectileData.elemental_dude_proj_2.also {
                        it.baseAngle = 0.0; it.lifetime = ms(4.0, it.speed)
                    })
                    entity.shoot(ProjectileData.elemental_dude_proj_2.also {
                        it.baseAngle = -30.0; it.lifetime = ms(4.0, it.speed)
                    })
                }
            }
            else -> // org
            {
                if (entity.texDataId != "orange_elemental_dude") {
                    entity.setTexData("orange_elemental_dude")
                }
                when ((entity.ticksExisted % 300) / 150) {
                    0 -> {
                        if (entity.currentMoveAI !is AIWander) {
                            entity.currentMoveAI = wander
                        }
                    }
                    1 -> {
                        if (entity.currentMoveAI !is AICirclePoint) {
                            entity.currentMoveAI = circlePoint
                        }
                    }
                }
                if (entity.delayNumSeconds(0.5)) {
                    ProjectileData.elemental_dude_proj_1.atPlayer = false
                    ProjectileData.elemental_dude_proj_2.atPlayer = false
                    for (i in 0..5) {
                        entity.shoot(ProjectileData.elemental_dude_proj_1.also {
                            it.baseAngle = i * 72.0; it.lifetime = ms(5.5, it.speed)
                        })
                        entity.shoot(ProjectileData.elemental_dude_proj_2.also {
                            it.baseAngle = i * 72.0 + 36.0; it.lifetime = ms(4.0, it.speed)
                        })
                    }
                    ProjectileData.elemental_dude_proj_1.baseAngle = 0.0
                    ProjectileData.elemental_dude_proj_2.baseAngle = 0.0
                    ProjectileData.elemental_dude_proj_1.atPlayer = true
                    ProjectileData.elemental_dude_proj_2.atPlayer = true
                }
            }
        }
    }

    override fun isDone():bool {
        return false
    }
}