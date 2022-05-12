package me.ethius.client.rotsg.command

import me.ethius.client.Client
import me.ethius.client.rotsg.data.ItemInfo
import me.ethius.client.rotsg.item.AirItem
import me.ethius.shared.string

class GiveItemCommand:
    Command(arrayOf("give", "giveitem"), "Give yourself an item. Requires debug mode.", "<item id (str)>") {
    override fun execute(args:Array<string>) {
        if (!Client.options.debug) {
            sendMessage("You must be in debug mode to use this command.")
            return
        }
        if (args.size != 1) {
            sendUsage()
            return
        }
        if (!Client.worldInit) {
            sendMessage("You must be in a world to use this command.")
            return
        }
        val id = args[0]
        val itemInf = ItemInfo.values.find { it.id == id }
        if (itemInf == null) {
            sendMessage("Invalid item id.")
            return
        }
        val item = itemInf()
        for (i in 4..11) {
            if (Client.player.inventory.slots[i].item is AirItem) {
                Client.player.inventory.slots[i].item = item
                sendMessage("You have been given ${itemInf.id}.")
                return
            }
        }
    }
}