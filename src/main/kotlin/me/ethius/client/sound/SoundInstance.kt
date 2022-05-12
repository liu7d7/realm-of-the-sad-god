package me.ethius.client.sound

import me.ethius.shared.bool
import me.ethius.shared.float
import me.ethius.shared.maths.Quintuple
import me.ethius.shared.void

data class SoundInstance(
    var source:Source,
    val pitch:float = 1.0f,
    val volume:float = 1.0f,
    var inputStream:OggAudioStream,
    val staticSound:StaticSound,
    val looping:bool,
) {

    fun play() {
        source.setLooping(looping)
        source.setPitch(pitch)
        source.setVolume(volume)
        source.setStream(inputStream)
        source.setBuffer(staticSound)
        source.play()
    }

    fun addPlayListener(listener:() -> void) {
        for (s in SoundManager.sounds) {
            if (s.v1 == this) {
                s.v2.add(listener)
                break
            }
        }
    }

    fun addStopListener(listener:() -> void) {
        for (s in SoundManager.sounds) {
            if (s.v1 == this) {
                s.v3.add(listener)
                break
            }
        }
    }

    init {
        SoundManager.sounds.add(Quintuple(this, mutableListOf(), mutableListOf(), false, looping))
    }

}