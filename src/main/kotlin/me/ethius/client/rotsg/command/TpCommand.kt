package me.ethius.client.rotsg.command

import me.ethius.client.Client
import me.ethius.shared.network.Packet
import me.ethius.shared.rotsg.tile.tile_size
import me.ethius.shared.string
import kotlin.math.roundToInt

class TpCommand:Command(arrayOf("tp", "teleport"), "Teleport to a specific position", "<x (int), y (int)>") {
    override fun execute(args:Array<string>) {
        if (!Client.options.debug) {
            sendMessage("This command requires you to be in debug mode!")
            return
        }
        if (args.size != 2) {
            sendUsage()
            return
        }
        val x = args[0].toInt()
        val y = args[1].toInt()
        Client.player.x = x * tile_size
        Client.player.y = y * tile_size
        Client.network.send(Packet._id_move, Client.player.x.roundToInt(), Client.player.y.roundToInt())
    }
}