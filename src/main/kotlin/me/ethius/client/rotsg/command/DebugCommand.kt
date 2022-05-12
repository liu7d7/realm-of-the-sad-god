package me.ethius.client.rotsg.command

import me.ethius.client.Client
import me.ethius.shared.string

class DebugCommand:Command(arrayOf("debug"), "Enable/disable debug mode", "<debug (bool)>") {
    override fun execute(args:Array<string>) {
        if (args.size != 1) {
            sendUsage()
            return
        }
        val debug = args[0].toBoolean()
        Client.options.debug = debug
    }
}