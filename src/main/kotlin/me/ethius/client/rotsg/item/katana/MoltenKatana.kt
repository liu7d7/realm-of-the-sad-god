package me.ethius.client.rotsg.item.katana

import me.ethius.client.rotsg.item.ItemTier
import me.ethius.client.rotsg.item.KatanaItem
import me.ethius.shared.opti.TexData
import me.ethius.shared.rotsg.data.ProjectileProperties

class MoltenKatana:KatanaItem(TexData.molten_katana,
                              ItemTier.heroic,
                              80..95,
                              2,
                              hashMapOf(),
                              "Molten Katana",
                              "Sizzle, Sizzle, Sizzle...") {

    val projInfo = ProjectileProperties(TexData.molten_katana_proj, 0.3, 1.7, 11.0, 4.0,
                                        true, false, false, false, 80..95).also { it.throughDef = false; it.renderAngleAdd = ProjectileProperties.slant_angle_correction }

    init {
        this.shotPattern = listOf(projInfo, projInfo)
        this.apsMultiplier = 1.25f
        this.arcGap = 8.0
    }

}