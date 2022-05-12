package me.ethius.client.rotsg.item

import me.ethius.client.rotsg.entity.BagTier
import me.ethius.shared.long
import me.ethius.shared.string

enum class ItemTier(val displayString:string, val displayColor:long, val bagTier:BagTier) {
    empty(" ", 0x00000000, BagTier.empty),
    normal("T", 0xffffffff, BagTier.pink),
    heroic("UT", 0xff00a8f3, BagTier.yellow),
    legendary("UT", 0xffea0056, BagTier.white),
    primal("UT", 0xffc4141e, BagTier.white)
}