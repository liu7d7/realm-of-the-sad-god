package me.ethius.client.rotsg.item.katana

import me.ethius.client.rotsg.item.ItemTier
import me.ethius.client.rotsg.item.KatanaItem
import me.ethius.shared.opti.TexData
import me.ethius.shared.rotsg.data.ProjectileData
import me.ethius.shared.rotsg.entity.Stat

class MoonbeamKatana:KatanaItem(TexData.moonbeam_katana,
                                ItemTier.heroic,
                                85..85,
                                1,
                                hashMapOf(Pair(Stat.dex, 6)),
                                "Moonbeam Katana",
                                "This katana shoots moonbeams.") {
    init {
        this.shotPattern = listOf(ProjectileData(TexData.moon_beam,
                                                 0.0,
                                                 0.0,
                                                 14.0,
                                                 4.5,
                                                 true,
                                                 false,
                                                 false,
                                                 false,
                                                 85..85).also { it.throughDef = true })
        this.apsMultiplier = 1.66f
    }
}