package me.ethius.client.rotsg.item.katana

import me.ethius.client.rotsg.item.ItemTier
import me.ethius.client.rotsg.item.KatanaItem
import me.ethius.shared.opti.TexData
import me.ethius.shared.rotsg.data.ProjectileData
import me.ethius.shared.rotsg.entity.Stat

class StardustCutter:KatanaItem(TexData.stardust_cutter,
                                ItemTier.legendary,
                                160..260,
                                1,
                                hashMapOf(Pair(Stat.dex, 3)),
                                "Stardust Cutter",
                                "Winston.") {

    init {
        // set the shot pattern of the weapon
        shotPattern = listOf(entityInfo)
    }

    companion object {
        // this is the projectile supplier that will be used to create the projectile
        private val entityInfo =
            ProjectileData(TexData.cutter_proj, 0.35, 0.2, 13.0, 4.2, true, false, false, false, 160..260)
    }

}