package me.ethius.shared.rotsg.entity.ai.impl

import me.ethius.shared.bool
import me.ethius.shared.long
import me.ethius.shared.measuringTimeMS
import me.ethius.shared.rotsg.entity.ai.AIBase
import me.ethius.shared.rotsg.entity.enemy.Enemy

class AIStayPut(entityIn:Enemy, val time:long):AIBase(entityIn) {
    override val type:Type = Type.move

    override fun update() {

    }

    override fun isDone():bool {
        return measuringTimeMS() - startTime > time
    }
}