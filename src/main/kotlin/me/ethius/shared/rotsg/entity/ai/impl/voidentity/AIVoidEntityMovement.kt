package me.ethius.shared.rotsg.entity.ai.impl.voidentity

import me.ethius.shared.bool
import me.ethius.shared.rotsg.entity.ai.AIBase
import me.ethius.shared.rotsg.entity.ai.impl.AIWander
import me.ethius.shared.rotsg.entity.enemy.Enemy

class AIVoidEntityMovement(entityIn:Enemy):AIBase(entityIn) {
    override val type:Type = Type.move
    var centerX = 0.0
    var centerY = 0.0
    val aiWander = AIWander(entityIn)

    override fun update() {
        when (VoidEntityStage.getStage(entity)) {
            VoidEntityStage.center -> {
                centerX = entity.x
                centerY = entity.y
            }
            VoidEntityStage.circle -> {
                aiWander.update()
            }
        }
    }

    override fun isDone():bool {
        return false
    }
}