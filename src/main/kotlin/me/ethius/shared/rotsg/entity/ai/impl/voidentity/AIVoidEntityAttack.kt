package me.ethius.shared.rotsg.entity.ai.impl.voidentity

import me.ethius.server.rotsg.world.ServerWorld
import me.ethius.shared.bool
import me.ethius.shared.calcAngle
import me.ethius.shared.measuringTimeMS
import me.ethius.shared.opti.TexData
import me.ethius.shared.rotsg.data.ProjectileProperties
import me.ethius.shared.rotsg.entity.ai.AIBase
import me.ethius.shared.rotsg.entity.enemy.Enemy

class AIVoidEntityAttack(entityIn:Enemy):AIBase(entityIn) {
    override val type:Type = Type.attack
    var runs = 0
    var lastAttack = 0f
        set(value) {
            field = value
            runs++
        }

    private val projData1 =
        ProjectileProperties(TexData.void_dude_proj_1, 0.0, 0.0, 11.0, 7.0, true, false, false, false, 100..120)
    private val projData2 = ProjectileProperties(TexData.cutter_proj, 0.0, 0.0, 11.0, 7.0, true, false, false, true, 60..80)

    override fun update() {
        if (entity.world == null) return
        when (VoidEntityStage.getStage(entity)) {
            VoidEntityStage.circle -> {
                if (measuringTimeMS() - lastAttack >= 700) {
                    val target = (entity.world as ServerWorld).closestPlayer(this.entity) ?: return
                    val baseAngle = calcAngle(entity, target)
                    entity.shoot(projData1).also { it.r = baseAngle }
                    entity.shoot(projData1).also { it.r = baseAngle + 30f }
                    entity.shoot(projData1).also { it.r = baseAngle - 30f }
                    lastAttack = measuringTimeMS()
                }
            }
            VoidEntityStage.center -> {
                if (measuringTimeMS() - lastAttack >= 300) {
                    for (i in 0..9) {
                        if (i % 2 == 0) {
                            entity.shoot(projData2).also { it.r = i * 36f + runs * 3.0 }
                        } else {
                            entity.shoot(projData1).also { it.r = i * 36f + runs * 3.0 }
                        }
                    }
                    lastAttack = measuringTimeMS()
                }
            }
        }
    }

    override fun isDone():bool {
        return false
    }

}