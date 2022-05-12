package me.ethius.client.rotsg.command

import me.ethius.shared.rotsg.data.Formatting
import me.ethius.shared.string

class HelpCommand:Command(arrayOf("help", "h"), "Shows help for commands", "<page (1-2)>") {
    override fun execute(args:Array<string>) {
        if (args.size != 1) {
            this.sendUsage()
            return
        }
        val idx = args[0].toInt()
        for (i in (idx - 1) * 7..(idx - 1) * 7 + 6) {
            if (values.indices.contains(i)) {
                sendMessage("/${Formatting.aqua}${values[i].aliases[0]}${Formatting.reset} ${values[i].usage}")
            } else {
                break
            }
        }
    }
}