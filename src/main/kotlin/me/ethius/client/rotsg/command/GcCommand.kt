package me.ethius.client.rotsg.command

import me.ethius.shared.Log
import me.ethius.shared.string

class GcCommand:Command(arrayOf("gc"), "Make the jvm collect garbage; may help performance", "") {
    override fun execute(args:Array<string>) {
        try {
            System.gc()
        } catch (e:Exception) {
            sendMessage("Failed to run gc")
            Log.error + "Failed to run gc using command" + Log.endl
            return
        }
        sendMessage("Successfully ran gc")
        Log.info + "Ran gc using command" + Log.endl
    }
}