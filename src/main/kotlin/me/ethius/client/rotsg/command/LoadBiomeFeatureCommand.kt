package me.ethius.client.rotsg.command

import me.ethius.client.Client
import me.ethius.client.rotsg.screen.worldbuilder.WorldBuilderScreen
import me.ethius.shared.ivec2
import me.ethius.shared.rotsg.world.biome.BiomeFeature
import me.ethius.shared.string

class LoadBiomeFeatureCommand:Command(arrayOf("lbf", "loadbiomefeature"), "Loads a biome feature", "<name (str)>") {
    override fun execute(args:Array<string>) {
        if (Client.screen !is WorldBuilderScreen) {
            Client.inGameHud.chatHud.addChat("You must be in the world builder screen to use this command!")
            return
        }
        if (args.size != 1) {
            sendUsage()
            return
        }
        val name = args[0]
        val biomeFeature = BiomeFeature.values.find { it.id == name }
        if (biomeFeature == null) {
            sendMessage("No biome feature with the name $name exists!")
            return
        }
        (Client.screen as WorldBuilderScreen).loadFeature(biomeFeature(ivec2(4, 4)))
    }
}
