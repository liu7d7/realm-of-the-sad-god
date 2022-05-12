package me.ethius.shared.rotsg.entity.ai.impl

import me.ethius.server.rotsg.world.ServerWorld
import me.ethius.shared.bool
import me.ethius.shared.calcAngle
import me.ethius.shared.ext.distance2d
import me.ethius.shared.ext.distance2dSquared
import me.ethius.shared.rotsg.entity.ai.AIBase
import me.ethius.shared.rotsg.entity.enemy.Aoe
import me.ethius.shared.rotsg.entity.enemy.Enemy
import me.ethius.shared.rotsg.tile.tile_size

class AIMedusaAttack(entityIn:Enemy):AIBase(entityIn) {
    override val type:Type = Type.attack
    private val aiAttackDefault = AIAttackDefault(entityIn)

    override fun update() {
        aiAttackDefault.update()
        val world = entity.world as ServerWorld
        val player = world.closestPlayer(this.entity) ?: return
        if (player.pos.distance2dSquared(entity.pos) < 25 * tile_size * tile_size) {
            if (entity.ticksExisted % 150 == 0) {
                world.addEntity(Aoe(entity.x, entity.y).also {
                    it.owner = this.entity
                    it.damage = 150.0
                    it.radius = 2.8
                    it.lifetime = 1200.0
                    it.speed = player.pos.distance2d(entity.pos) / 50.0
                    it.direction = calcAngle(entity, player)
                    it.texDataId = "vit_rock"
                })
            }
        }
    }

    override fun isDone():bool {
        return false
    }
}