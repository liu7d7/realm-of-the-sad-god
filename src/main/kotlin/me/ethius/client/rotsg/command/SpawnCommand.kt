package me.ethius.client.rotsg.command

import me.ethius.client.Client
import me.ethius.shared.rotsg.data.EntityInfo
import me.ethius.shared.rotsg.tile.tile_size
import me.ethius.shared.string

class SpawnCommand:Command(arrayOf("spawn"),
                           "Spawns an entity at your location or the given one.",
                           "<entity id (str), pos x (int, optional), pos y (int, optional)>") {
    override fun execute(args:Array<string>) {
        if (!Client.options.debug) {
            sendMessage("This command is only available in debug mode.")
            return
        }
        if (!Client.player.inWorld()) {
            sendMessage("You must be in a world to use this command.")
            return
        }
        if (args.isEmpty()) {
            sendUsage()
            return
        }

        val entityId = args[0]
        val posX = if (args.size > 1) args[1].toFloat() * tile_size else Client.player.x
        val posY = if (args.size > 2) args[2].toFloat() * tile_size else Client.player.y

        val e = EntityInfo[entityId]
        if (e == null) {
            sendMessage("Invalid entity id.")
            return
        }

        Client.world.addEntity(e().also { it.x = posX; it.y = posY })
    }
}