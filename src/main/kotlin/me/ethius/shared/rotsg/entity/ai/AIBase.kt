package me.ethius.shared.rotsg.entity.ai

import me.ethius.shared.bool
import me.ethius.shared.measuringTimeMS
import me.ethius.shared.rotsg.entity.enemy.Enemy

// AI base class
abstract class AIBase(val entity:Enemy) {

    // the time this AI started
    var startTime = measuringTimeMS()

    // this AI's type
    abstract val type:Type

    // perform the AI's logic
    abstract fun update()

    // is this AI finished?
    abstract fun isDone():bool

    // reset this AI
    open fun reset() {
        startTime = measuringTimeMS()
    }

    open fun collideWithBlocks() {
        // do nothing
    }

    // AI type
    enum class Type {
        move, attack
    }

}