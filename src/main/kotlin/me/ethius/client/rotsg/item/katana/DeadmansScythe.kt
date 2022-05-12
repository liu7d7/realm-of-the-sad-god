package me.ethius.client.rotsg.item.katana

import me.ethius.client.rotsg.item.ItemTier
import me.ethius.client.rotsg.item.KatanaItem
import me.ethius.client.rotsg.item.LegendaryEffect
import me.ethius.shared.opti.TexData
import me.ethius.shared.rotsg.data.ProjectileData
import me.ethius.shared.rotsg.entity.Stat

class DeadmansScythe:KatanaItem(TexData.dead_man_scythe,
                                ItemTier.legendary,
                                listOf(ProjectileData(TexData.rotting_boss_proj,
                                                      0.0,
                                                      0.0,
                                                      10.0,
                                                      4.0,
                                                      false,
                                                      false,
                                                      false,
                                                      false,
                                                      345..460).also { it.renderAngleAdd = -45.0 }),
                                hashMapOf(Stat.dex to 5, Stat.vit to 5, Stat.life to 20),
                                "Dead Man's Scythe",
                                "A two-faced blade enchanted with the wicked power of immortal beings.") {

    init {
        this.apsMultiplier = 0.33f
        this.legendaryEffect = LegendaryEffect.scythe
    }

}