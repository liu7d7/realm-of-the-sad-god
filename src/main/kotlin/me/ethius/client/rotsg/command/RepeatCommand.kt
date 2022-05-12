package me.ethius.client.rotsg.command

import me.ethius.client.Client
import me.ethius.shared.string

class RepeatCommand:Command(arrayOf("repeat"),
                            "Repeat a command x times",
                            "<x (int, required)>, \"<command (str, any command in / form)>\"") {
    override fun execute(args:Array<string>) {
        if (args.size < 2) {
            sendUsage()
            return
        }
        val x = args[0].toInt()
        val args = args.drop(1)
        val command = args.joinToString(" ").drop(1).dropLast(1)
        sendMessage("\"$command\" will be repeated $x times")
        Client.inGameHud.chatHud.runWithDisabledMessages {
            for (i in 0 until x) {
                tryExec(command, true)
            }
        }
    }
}