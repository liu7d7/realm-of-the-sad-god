package me.ethius.shared.rotsg.entity.ai.impl

import me.ethius.server.rotsg.world.ServerWorld
import me.ethius.shared.bool
import me.ethius.shared.calcAngle
import me.ethius.shared.ext.distance2d
import me.ethius.shared.measuringTimeMS
import me.ethius.shared.rotsg.entity.ai.AIBase
import me.ethius.shared.rotsg.entity.enemy.Enemy
import me.ethius.shared.rotsg.tile.tile_size

// AI that makes the entity follow the player
class AIFollowPlayer(entityIn:Enemy):AIBase(entityIn) {

    // the type of this AI
    override val type:Type = Type.move

    // perform the AI action
    override fun update() {
        val world = entity.world as? ServerWorld ?: return
        val target = world.closestPlayer(entity) ?: return
        if (this.entity.pos.distance2d(target.pos) > this.entity.vision * tile_size) {
            return
        }
        // move the entity
        entity.moveDirection(
            calcAngle(entity, target),
            entity.tps.toDouble()
        )
    }

    // is the AI action finished?
    override fun isDone():bool {
        val world = entity.world as? ServerWorld ?: return true
        val target = world.closestPlayer(entity) ?: return true
        return entity.pos.distanceSquared(target.pos) <= tile_size * tile_size * 2.0 || measuringTimeMS() - startTime >= 5000
    }

}