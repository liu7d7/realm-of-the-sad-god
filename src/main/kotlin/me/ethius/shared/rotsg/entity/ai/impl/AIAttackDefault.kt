package me.ethius.shared.rotsg.entity.ai.impl

import me.ethius.server.rotsg.world.ServerWorld
import me.ethius.shared.bool
import me.ethius.shared.ext.distance2dSquared
import me.ethius.shared.measuringTimeMS
import me.ethius.shared.rotsg.entity.ai.AIBase
import me.ethius.shared.rotsg.entity.enemy.Enemy
import me.ethius.shared.rotsg.tile.tile_size

// AI that attacks the target using default attack
class AIAttackDefault(entityIn:Enemy):AIBase(entityIn) {

    // set the type of this AI
    override val type:Type = Type.attack

    // set the last time this AI action was performed
    private var lastAttackTime = 0f

    // perform the AI action
    override fun update() {
        if (this.entity.world == null) {
            return
        }
        val world = this.entity.world!! as ServerWorld
        val target = world.closestPlayer(this.entity) ?: return
        // if the entity is not in range, return
        if ((target.pos.distance2dSquared(entity.pos) / (tile_size * tile_size)) > (entity.vision * entity.vision)) return
        // the current time in milliseconds
        val time = measuringTimeMS()
        // if the time is greater than the last attack time plus the attack delay, attack
        if (time - lastAttackTime >= (1000f / this.entity.aps)) {
            // set the last attack time to the current time
            lastAttackTime = time
            // attack
            entity.fire()
        }
    }

    // is the AI action finished?
    override fun isDone():bool {
        // the AI action is finished if it has been longer than 5000 milliseconds
        return measuringTimeMS() - startTime >= 5000
    }

    // reset the AI action
    override fun reset() {
        super.reset()
        // reset the last attack time
        lastAttackTime = 0f
    }

}