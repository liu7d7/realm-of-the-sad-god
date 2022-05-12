package me.ethius.shared.rotsg.entity.ai.impl.voidentity

import me.ethius.shared.rotsg.entity.enemy.Enemy

enum class VoidEntityStage {
    circle, center;

    companion object {
        fun getStage(entityIn:Enemy):VoidEntityStage {
            return if (entityIn.hp >= 15000) {
                center
            } else {
                circle
            }
        }
    }
}