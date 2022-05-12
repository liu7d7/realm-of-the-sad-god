package me.ethius.client.rotsg.item.sword

import me.ethius.client.rotsg.item.ItemTier
import me.ethius.client.rotsg.item.SwordItem
import me.ethius.shared.opti.TexData
import me.ethius.shared.rotsg.data.ProjectileData
import me.ethius.shared.rotsg.entity.Stat

class RottingArm:SwordItem(TexData.rotting_arm, ItemTier.primal,
                           135..185, 5,
                           hashMapOf(Pair(Stat.def, 4)),
                           "Rotting Arm",
                           "Despite its fragile and disgusting appearance, this cut-off arm packs a powerful hit.") {

    init {
        // set the shot pattern of the weapon
        shotPattern = listOf(
            ProjectileData.rotten_shot(false, 135..185),
            ProjectileData.rotten_shot(false, 135..185),
            ProjectileData.rotten_shot(false, 135..185),
            ProjectileData.rotten_shot(false, 135..185),
            ProjectileData.rotten_shot(false, 135..185),
        )
        this.arcGap = 14.0
    }

}