package me.ethius.shared.rotsg.entity.ai.impl

import me.ethius.server.rotsg.world.ServerWorld
import me.ethius.shared.bool
import me.ethius.shared.measuringTimeMS
import me.ethius.shared.opti.TexData
import me.ethius.shared.rotsg.data.ProjectileProperties
import me.ethius.shared.rotsg.entity.ai.AIBase
import me.ethius.shared.rotsg.entity.enemy.Enemy
import me.ethius.shared.rotsg.entity.other.Projectile
import me.ethius.shared.wrapDegrees

class AIFlameEntityAttack(entityIn:Enemy):AIBase(entityIn) {
    override val type:Type = Type.attack
    private var lastAct = 0f
    private var runs = 0f

    override fun update() {
        val world = entity.world as? ServerWorld ?: return
        if (measuringTimeMS() - lastAct >= 100f) {
            for (i in 0..5) {
                world.addEntity(Projectile().reset(entity, pData).also { it.r = wrapDegrees(runs + i * 60.0); })
            }
            runs += 3f
            lastAct = measuringTimeMS()
        }
    }

    override fun isDone():bool {
        return false
    }

    companion object {
        private val pData = ProjectileProperties(TexData.fb_2_proj,
                                                 0.0,
                                                 0.0,
                                                 7.5,
                                                 10.0,
                                                 false,
                                                 false,
                                                 false,
                                                 false,
                                                 90..125).also { it.spinSpeed = 50.0 }
    }
}