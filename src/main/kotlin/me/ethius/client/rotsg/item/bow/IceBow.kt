package me.ethius.client.rotsg.item.bow

import me.ethius.client.rotsg.item.BowItem
import me.ethius.client.rotsg.item.ItemTier
import me.ethius.shared.opti.TexData
import me.ethius.shared.rotsg.data.ProjectileProperties
import me.ethius.shared.rotsg.entity.Stat

class IceBow:BowItem(TexData.ice_bow,
                     ItemTier.primal,
                     100..115,
                     2,
                     hashMapOf(Pair(Stat.atk, 6), Pair(Stat.dex, 6)),
                     "Ice Bow",
                     "A bow that shoots fragile but sharp ice arrows.") {

    init {
        // set the shot pattern of the weapon
        shotPattern = listOf(pData, pData)
        this.apsMultiplier = 1.33f
        this.arcGap = 10.0
    }

    companion object {
        private val pData = ProjectileProperties(TexData.ice_arrow, 0.0, 0.0, 7.0, 7.5, true, false, false, false, 100..115).also { it.renderAngleAdd = ProjectileProperties.slant_angle_correction }
    }

}