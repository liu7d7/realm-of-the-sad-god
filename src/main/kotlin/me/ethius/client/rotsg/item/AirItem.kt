package me.ethius.client.rotsg.item


import me.ethius.shared.opti.TexData
import me.ethius.shared.string

class AirItem:Item(TexData.empty, ItemTier.empty, "Air", "Empty Slot") {
    override fun getTooltip():List<string> {
        return listOf(desc)
    }
}