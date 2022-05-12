package me.ethius.client.rotsg.command

import me.ethius.client.Client
import me.ethius.shared.rotsg.data.Formatting
import me.ethius.shared.rotsg.tile.tile_size
import me.ethius.shared.string

class FindMobCommand:Command(arrayOf("findmob", "fm"), "Finds a mob by string id", "<entity id (str)>") {
    override fun execute(args:Array<string>) {
        if (args.size != 1) {
            sendUsage()
            return
        }
        if (!Client.worldInit) {
            sendMessage("You must be in a world to use this command")
            return
        }
        val id = args[0]
        val mob = Client.world.entities.filter { it.typeId == id }
        if (mob.isEmpty()) {
            sendMessage("No mob with id $id")
            return
        }
        val m = mob.random()
        sendMessage("Found $id at ${Formatting.green}[${(m.x / tile_size).toInt()}, ${(m.y / tile_size).toInt()}]")
    }
}