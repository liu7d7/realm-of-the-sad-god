package me.ethius.client.rotsg.command

import me.ethius.shared.string

class GcCommand:Command(arrayOf("gc"), "Make the jvm collect garbage; may help performance", "") {
    override fun execute(args:Array<string>) {
        try {
            System.gc()
        } catch (e:Exception) {
            sendMessage("Failed to run gc")
            return
        }
        sendMessage("Successfully ran gc")
    }
}