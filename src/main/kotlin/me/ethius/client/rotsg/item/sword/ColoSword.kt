package me.ethius.client.rotsg.item.sword

import me.ethius.client.rotsg.item.ItemTier
import me.ethius.client.rotsg.item.SwordItem
import me.ethius.shared.opti.TexData
import me.ethius.shared.rotsg.data.ProjectileData
import me.ethius.shared.rotsg.entity.Stat

class ColoSword:SwordItem(TexData.colo_sword_1, ItemTier.legendary,
                          180..215, 1, hashMapOf(Pair(Stat.dex, -5), Pair(Stat.def, -10), Pair(Stat.atk, 7)),
                          "Minion Sword", "An unwieldy but powerful sword.") {

    init {
        // set the shot pattern of the weapon
        shotPattern = listOf(entityInfo)
    }

    companion object {
        private val entityInfo =
            ProjectileData(TexData.colo_proj, 1.0, 0.5, 10.0, 4.5, false, false, false, false, 180..215)
    }

}