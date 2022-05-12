package me.ethius.client.rotsg.item.potion

import me.ethius.client.Client
import me.ethius.client.rotsg.data.ItemInfo
import me.ethius.client.rotsg.item.Item
import me.ethius.client.rotsg.item.ItemTier
import me.ethius.shared.bool
import me.ethius.shared.int
import me.ethius.shared.rotsg.data.Formatting
import me.ethius.shared.rotsg.entity.Stat
import kotlin.math.ceil

class PotionItem(val stat:Stat):Item(stat.potion,
                                     ItemTier.normal,
                                     "Potion of ${stat.formalName}",
                                     "A potion that permanently increases ${stat.formalName} by 1") {

    val inc = when (stat) {
        Stat.life, Stat.mana -> 10
        else -> 2
    }

    override fun consume():bool {
        val ptoMax:int = toMax(stat)
        Client.player.incStat(stat, inc, true)
        val toMax:int = toMax(stat)
        return if (ptoMax != toMax) {
            if (toMax != 0) {
                Client.inGameHud.chatHud.addChat("${Formatting.yellow}Potion of ${stat.formalName} consumed. $toMax left to max.")
            } else {
                Client.inGameHud.chatHud.addChat("${Formatting.yellow}Potion of ${stat.formalName} consumed. You're maxed!")
            }
            true
        } else {
            Client.inGameHud.chatHud.addChat("${Formatting.red}Potion of ${stat.formalName} not consumed. Already maxed!")
            false
        }
    }

    private fun toMax(stat:Stat):int {
        return when (stat) {
            Stat.mana -> {
                ceil((Client.player.pClass.maxStats[stat.ordinal] - Client.player.manaMaxed) / 5.0).toInt()
            }
            Stat.life -> {
                ceil((Client.player.pClass.maxStats[stat.ordinal] - Client.player.lifeMaxed) / 5.0).toInt()
            }
            Stat.atk -> {
                Client.player.pClass.maxStats[stat.ordinal] - Client.player.atkMaxed
            }
            Stat.def -> {
                Client.player.pClass.maxStats[stat.ordinal] - Client.player.defMaxed
            }
            Stat.spd -> {
                Client.player.pClass.maxStats[stat.ordinal] - Client.player.spdMaxed
            }
            Stat.dex -> {
                Client.player.pClass.maxStats[stat.ordinal] - Client.player.dexMaxed
            }
            Stat.wis -> {
                Client.player.pClass.maxStats[stat.ordinal] - Client.player.wisMaxed
            }
            Stat.vit -> {
                Client.player.pClass.maxStats[stat.ordinal] - Client.player.vitMaxed
            }
        }
    }

    companion object {
        fun supplierFromStat(stat:Stat):ItemInfo<PotionItem> {
            return when (stat) {
                Stat.life -> ItemInfo.life_potion
                Stat.mana -> ItemInfo.mana_potion
                Stat.atk -> ItemInfo.atk_potion
                Stat.def -> ItemInfo.def_potion
                Stat.spd -> ItemInfo.spd_potion
                Stat.dex -> ItemInfo.dex_potion
                Stat.wis -> ItemInfo.wis_potion
                Stat.vit -> ItemInfo.vit_potion
            }
        }
    }

}