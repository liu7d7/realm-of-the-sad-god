package me.ethius.client.sound

import me.ethius.shared.float
import me.ethius.shared.string

object SoundHelper {

    fun loadSound(
        location:string,
        pitch:float = 1.0f,
        volume:float = 1.0f,
    ):SoundInstance {
        val oggAudioStream = OggAudioStream(location)
        val staticSound = StaticSound(oggAudioStream.getBuffer(), oggAudioStream.format!!)
        oggAudioStream.close()
        val source = Source.create() ?: throw IllegalStateException("null source")
        source.setPosition()
        return SoundInstance(source, pitch, volume, oggAudioStream, staticSound, false)
    }

}