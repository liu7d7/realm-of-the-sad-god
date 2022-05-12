package me.ethius.client.rotsg.item.bow

import me.ethius.client.rotsg.item.BowItem
import me.ethius.client.rotsg.item.ItemTier
import me.ethius.shared.opti.TexData
import me.ethius.shared.rotsg.data.ProjectileData
import me.ethius.shared.rotsg.entity.Stat

class VoidBow:BowItem(TexData.void_bow,
                      ItemTier.legendary,
                      135..205, 1,
                      hashMapOf(Pair(Stat.def, 5)),
                      "Void Bow",
                      "Every shot that hits a target frees the soul of a forgotten hero. Every shot that misses is trapped for eternity."
) {

    init {
        // set the shot pattern of the weapon
        shotPattern = listOf(entityInfo)
    }

    companion object {
        // this is the projectile supplier that will be used to create the projectile
        private val entityInfo =
            ProjectileData(TexData.void_arrow, 0.35, 0.2, 16.0, 5.6, true, false, false, false, 135..205)
    }

}