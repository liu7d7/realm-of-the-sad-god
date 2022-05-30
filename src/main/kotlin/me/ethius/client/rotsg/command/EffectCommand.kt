package me.ethius.client.rotsg.command

import me.ethius.client.Client
import me.ethius.shared.rotsg.data.EffectInfo
import me.ethius.shared.string

class EffectCommand:Command(arrayOf("effect"), "Adds an effect to you", "<id (str)> <duration (ticks) (int)> <amplifier (int)>") {

    override fun execute(args:Array<string>) {
        val id = args[0]
        val effect = EffectInfo[id]
        if (effect == null) {
            sendMessage("Effect $id does not exist!")
            return
        }
        if (args.size < 3) {
            sendMessage("Usage: /effect <id> <duration> <amplifier>")
            return
        }
        val duration = args[1].toInt()
        val amplifier = args[2].toInt()
        Client.player.addEffect(effect(duration * 20L, amplifier).also {
            it.pushData("sourceId", Client.player.entityId)
        })
        sendMessage("Applied effect $id for $duration ticks with amplifier $amplifier")
    }

}