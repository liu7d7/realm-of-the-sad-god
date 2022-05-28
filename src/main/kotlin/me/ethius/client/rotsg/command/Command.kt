package me.ethius.client.rotsg.command

import me.ethius.client.Client
import me.ethius.shared.bool
import me.ethius.shared.rotsg.data.Formatting
import me.ethius.shared.string

abstract class Command(
    val aliases:Array<string>,
    val desc:string,
    val usage:string,
) {
    abstract fun execute(args:Array<string>)

    // send the specified message to the chatHud, with slight formatting
    fun sendMessage(message:string) {
        Client.inGameHud.chatHud.addChat("        <${Formatting.light_purple}${aliases[0]}${Formatting.reset}> $message")
    }

    // send the usage format to the chatHud
    fun sendUsage() {
        sendMessage("Usage: $usage")
    }

    companion object {
        // the prefix for all commands
        const val prefix = "/"

        // the command list
        val values = ArrayList<Command>()

        // try to find a command with the specified name and execute it
        fun tryExec(chat:string, autoExec:bool):bool {
            // if the chat doesn't start with the prefix, it's not a command; return false
            if (!chat.startsWith(prefix)) return false
            val split = chat.split(" ")
            for (v in values) {
                // if the chat starts with the prefix and the command's name or any of its aliases is the same as the chat, execute the command
                if (v.aliases.any { split[0] == prefix + it }) {
                    // remove the prefix and the command's name from the chat, and split the chat into arguments
                    val args = split.drop(1).toTypedArray()
                    // send the that the command has been executed to the chats
                    Client.inGameHud.chatHud.addChat("${Formatting.light_purple}${if (autoExec) "Autoe" else "E"}xecuted Command${Formatting.reset}: $chat")
                    // execute the command
                    v.execute(args)
                    // we've successfully executed the command; return true
                    return true
                }
            }
            // no command was found or an error happened; return false
            return false
        }

        init {
            values.add(HelpCommand())
            values.add(FindMobCommand())
            values.add(GcCommand())
            values.add(DebugCommand())
            values.add(GiveItemCommand())
            values.add(TpCommand())
            values.add(SpawnCommand())
            values.add(ProfileCommand())
            values.add(NameCommand())
            values.add(LoadBiomeFeatureCommand())
            values.add(RepeatCommand())
        }
    }
}