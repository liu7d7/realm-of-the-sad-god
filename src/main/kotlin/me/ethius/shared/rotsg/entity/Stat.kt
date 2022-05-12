package me.ethius.shared.rotsg.entity

import me.ethius.shared.opti.TexData
import me.ethius.shared.string

enum class Stat(val potion:TexData, val rock:TexData, val formalName:string) {
    life(TexData.life_potion, TexData.life_rock, "Life"),
    mana(TexData.mana_potion, TexData.mana_rock, "Mana"),
    atk(TexData.atk_potion, TexData.atk_rock, "Attack"),
    def(TexData.def_potion, TexData.def_rock, "Defense"),
    spd(TexData.spd_potion, TexData.spd_rock, "Speed"),
    dex(TexData.dex_potion, TexData.dex_rock, "Dexterity"),
    vit(TexData.vit_potion, TexData.vit_rock, "Vitality"),
    wis(TexData.wis_potion, TexData.wis_rock, "Wisdom"),
}