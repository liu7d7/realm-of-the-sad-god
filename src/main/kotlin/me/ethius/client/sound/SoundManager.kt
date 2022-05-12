package me.ethius.client.sound

import me.ethius.shared.Tickable
import me.ethius.shared.bool
import me.ethius.shared.maths.Quintuple
import me.ethius.shared.void

object SoundManager:Tickable() {

    val sounds =
        ArrayList<Quintuple<SoundInstance, MutableList<() -> void>, MutableList<() -> void>, bool, bool>>()

    override fun clientTick() {
        for (it in sounds) {
            it.v1.source.tick()
            if (it.v1.source.isStopped) {
                for (it in it.v3) {
                    it()
                }
                it.v4 = false
            } else if (!it.v4) {
                for (it in it.v2) {
                    it()
                }
                it.v4 = true
            }
        }
        sounds.removeIf { it.v1.source.isStopped && !it.v5 }
    }

    init {
        init()
    }

}