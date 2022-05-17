package me.ethius.client.rotsg.item.bow

import me.ethius.client.rotsg.item.BowItem
import me.ethius.client.rotsg.item.ItemTier
import me.ethius.shared.opti.TexData
import me.ethius.shared.rotsg.data.ProjectileData
import me.ethius.shared.rotsg.entity.Stat

class DoomBow:BowItem(TexData.doom_bow,
                      ItemTier.heroic,
                      listOf(ProjectileData(TexData.doom_arrow,
                                            0.0,
                                            0.0,
                                            14.0,
                                            7.5,
                                            true,
                                            false,
                                            false,
                                            false,
                                            350..450).also { it.renderAngleAdd = ProjectileData.slant_angle_correction }),
                      hashMapOf(Pair(
                          Stat.vit, 4)),
                      "Doom Bow",
                      "The most doom bow.") {
    init {
        this.apsMultiplier = 0.33f
    }
}