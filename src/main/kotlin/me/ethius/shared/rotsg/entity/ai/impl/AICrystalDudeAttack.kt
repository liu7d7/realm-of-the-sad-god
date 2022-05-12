package me.ethius.shared.rotsg.entity.ai.impl

import me.ethius.shared.bool
import me.ethius.shared.rotsg.data.EntityInfo
import me.ethius.shared.rotsg.data.ProjectileData
import me.ethius.shared.rotsg.entity.ai.AIBase
import me.ethius.shared.rotsg.entity.enemy.Enemy
import org.apache.commons.lang3.RandomUtils

class AICrystalDudeAttack(entityIn:Enemy):AIBase(entityIn) {
    override val type:Type = Type.attack

    override fun update() {
        if (entity.ticksExisted % 15 == 0) {
            entity.shoot(ProjectileData.crystal_dude_proj_1)
            entity.shoot(ProjectileData.crystal_dude_proj_1)
            entity.shoot(ProjectileData.crystal_dude_proj_2)
            entity.shoot(ProjectileData.crystal_dude_proj_2)
        }

        if ((entity.ticksExisted / 25) % 5 == 0) {
            if (entity.ticksExisted % 8 == 0) {
                entity.shoot(ProjectileData.crystal_dude_proj_3)
                entity.shoot(ProjectileData.crystal_dude_proj_3)
            }
        }

        if (entity.ticksExisted % 50 == 0) {
            if (RandomUtils.nextBoolean()) {
                this.entity.world?.addEntity(EntityInfo.cube_entity, entity.flooredTilePos())
                    .also { it?.lootTable = emptyList() }
            }
        }
    }

    override fun isDone():bool {
        return false
    }

}