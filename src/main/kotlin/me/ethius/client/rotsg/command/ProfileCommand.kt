package me.ethius.client.rotsg.command

import me.ethius.client.Client
import me.ethius.client.rotsg.screen.MainMenuScreen
import me.ethius.shared.rotsg.entity.player.PlayerProfile
import me.ethius.shared.string

class ProfileCommand:Command(arrayOf("profile"), "Set your profile", "<name (str)>") {
    override fun execute(args:Array<string>) {
        if (args.size != 1) {
            sendUsage()
            return
        }
        if (Client.screen !is MainMenuScreen) {
            sendMessage("You must be in the main menu to set your profile")
            return
        }
        try {
            (Client.screen as MainMenuScreen).playerProfile = PlayerProfile.read("saves/${args[0]}.dat")
        } catch (e:Exception) {
            sendMessage("Failed to read profile named ${args[0]}")
            e.printStackTrace()
            return
        }
        sendMessage("Profile set to ${args[0]}")
    }
}