package me.ethius.shared.rotsg.entity.ai.impl

import me.ethius.shared.bool
import me.ethius.shared.cosD
import me.ethius.shared.double
import me.ethius.shared.rotsg.entity.ai.AIBase
import me.ethius.shared.rotsg.entity.enemy.Enemy
import me.ethius.shared.rotsg.tile.tile_size
import me.ethius.shared.sinD

class AICirclePoint(
    entityIn:Enemy,
    rad:double,
    val endCondition:(Enemy) -> bool,
):AIBase(entityIn) {
    override val type:Type = Type.move
    private val rad = rad * tile_size
    private val circumference = 2.0 * Math.PI * rad
    private val speed:double
        get() {
            return circumference / 500.0 * entity.tps
        }
    private var currentAngle = 0.0

    override fun update() {
        val xDiff = cosD(currentAngle) * rad
        val yDiff = sinD(currentAngle) * rad
        val xOrig = entity.spawnPos.x
        val yOrig = entity.spawnPos.y
        entity.moveTo(xDiff + xOrig, yDiff + yOrig)
        currentAngle += speed
    }

    override fun isDone():bool {
        return endCondition(entity)
    }
}