package me.ethius.client.rotsg.item

import me.ethius.client.Client
import me.ethius.shared.int
import me.ethius.shared.opti.TexData
import me.ethius.shared.rotsg.entity.Stat
import me.ethius.shared.string
import org.apache.commons.lang3.StringUtils
import kotlin.math.sign

open class ArmorItem:Item {

    constructor(
        texData:TexData,
        tier:ItemTier,
        statMap:HashMap<Stat, int>,
        name:string,
        desc:string,
    ):super(texData, tier, name, desc) {
        this.statMap = statMap
    }

    constructor(assetLoc:string):super(assetLoc)

    override fun getTooltip():List<string> {
        val mappedList = ArrayList<string>()
        for ((k, v) in statMap) {
            val isPos = v.sign == 1
            mappedList.add("${StringUtils.capitalize(k.toString().lowercase())}: ${if (isPos) "+" else ""}$v")
        }
        val tmp = mutableListOf(name, *Client.font.wrapWords(desc, 300.0), "-------------------")
        tmp.addAll(mappedList)
        if (mappedList.isNotEmpty() || legendaryEffect != null)
            tmp.add("-------------------")
        if (legendaryEffect != null)
            tmp.addAll(legendaryEffect!!.infoList)
        return tmp
    }

}

class LightArmorItem:ArmorItem {

    constructor(
        texData:TexData,
        tier:ItemTier,
        statMap:HashMap<Stat, int>,
        name:string,
        desc:string,
    ):super(texData, tier, statMap, name, desc)

    constructor(assetLoc:string):super(assetLoc)

}

class HeavyArmorItem:ArmorItem {
    constructor(
        texData:TexData,
        tier:ItemTier,
        statMap:HashMap<Stat, int>,
        name:string,
        desc:string,
    ):super(texData, tier, statMap, name, desc)

    constructor(assetLoc:string):super(assetLoc)
}