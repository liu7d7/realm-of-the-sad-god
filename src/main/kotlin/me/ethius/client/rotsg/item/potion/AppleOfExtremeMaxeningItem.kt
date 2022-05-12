package me.ethius.client.rotsg.item.potion

import me.ethius.client.Client
import me.ethius.client.rotsg.item.Item
import me.ethius.client.rotsg.item.ItemTier
import me.ethius.shared.bool
import me.ethius.shared.opti.TexData
import me.ethius.shared.rotsg.entity.Stat

class AppleOfExtremeMaxeningItem:
    Item(TexData.apple, ItemTier.normal, "Apple of Extreme Maxening", "Extremely maxes out your stats.") {

    override fun consume():bool {
        Client.player.exp += 500000
        Client.player.incStat(Stat.life, 5000, true)
        Client.player.incStat(Stat.mana, 5000, true)
        Client.player.incStat(Stat.atk, 5000, true)
        Client.player.incStat(Stat.def, 5000, true)
        Client.player.incStat(Stat.spd, 5000, true)
        Client.player.incStat(Stat.dex, 5000, true)
        Client.player.incStat(Stat.wis, 5000, true)
        Client.player.incStat(Stat.vit, 5000, true)
        return true
    }

}