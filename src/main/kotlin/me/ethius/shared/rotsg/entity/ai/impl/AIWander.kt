package me.ethius.shared.rotsg.entity.ai.impl

import me.ethius.shared.*
import me.ethius.shared.rotsg.entity.ai.AIBase
import me.ethius.shared.rotsg.entity.enemy.Enemy
import me.ethius.shared.rotsg.tile.tile_size
import org.apache.commons.lang3.RandomUtils

// AI to wander around
class AIWander(entityIn:Enemy):AIBase(entityIn) {

    // the type of the AI
    override val type:Type = Type.move

    // the current target position
    private lateinit var target:dvec2
    private var angle:double = 0.0

    // the amount of targets reached
    private var targetsReached = -1

    private var lastUpdate = 0f

    // perform the AI action
    override fun update() {
        // if the target is reached or the target is not set
        if (!this::target.isInitialized || measuringTimeMS() - lastUpdate >= 6000) {
            setTarget()
            // increase the amount of targets reached
            targetsReached++
            lastUpdate = measuringTimeMS()
            angle = calcAngle(this.entity.y - target.y, this.entity.x - target.x) * DegToRadMult
        }

        // move the entity
        entity.move(
            sin(angle) * entity.tps * 0.2,
            -cos(angle) * entity.tps * 0.2
        )
    }

    // reset the AI
    override fun reset() {
        // reset the target using the entity's position plus a random offset
        setTarget()
        // reset the amount of targets reached
        targetsReached = 0
    }

    // is the AI finished?
    override fun isDone():bool {
        // if the amount of targets reached is greater than or equal to 3, return true
        return targetsReached >= 3
    }

    override fun collideWithBlocks() {
        setTarget()
    }

    private fun setTarget() {
        // set a new target using the current position plus a random offset
        target = dvec2(entity.x + (RandomUtils.nextFloat(0f, 5f) - 2.5) * tile_size,
                       entity.y + (RandomUtils.nextFloat(0f, 5f) - 2.5f) * tile_size)
        angle = calcAngle(this.entity.y - target.y, this.entity.x - target.x) * DegToRadMult
    }

}