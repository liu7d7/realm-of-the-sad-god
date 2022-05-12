package me.ethius.client.rotsg.command

import me.ethius.client.Client
import me.ethius.client.rotsg.screen.worldbuilder.WorldBuilderScreen
import me.ethius.shared.string

class NameCommand:Command(arrayOf("name"), "Name your character", "<name (str)>") {
    override fun execute(args:Array<string>) {
        if (args.size != 1) {
            sendUsage()
            return
        }

        val name = args[0]
        if (Client.screen is WorldBuilderScreen) {
            (Client.screen as WorldBuilderScreen).name = name
        }
    }
}