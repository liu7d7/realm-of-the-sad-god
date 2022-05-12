package me.ethius.client.sound

import me.ethius.client.sound.AlUtil.checkErrors
import me.ethius.shared.bool
import me.ethius.shared.float
import me.ethius.shared.int
import org.lwjgl.openal.AL10
import org.lwjgl.openal.AL11
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import javax.sound.sampled.AudioFormat

class Source private constructor(private val pointer:int) {

    private val playing = AtomicBoolean(true)
    private var bufferSize = 16384
    private var stream:AudioStream? = null

    fun close() {
        if (playing.compareAndSet(true, false)) {
            AL10.alSourceStop(pointer)
            checkErrors("Stop")
            if (stream != null) {
                try {
                    stream!!.close()
                } catch (var2:IOException) {
                    var2.printStackTrace()
                }
                removeProcessedBuffers()
                stream = null
            }
            AL10.alDeleteSources(intArrayOf(pointer))
            checkErrors("Cleanup")
        }
    }

    fun play() {
        AL10.alSourcePlay(pointer)
    }

    private val sourceState:int
        private get() = if (!playing.get()) AL10.AL_STOPPED else AL10.alGetSourcei(pointer, AL10.AL_SOURCE_STATE)

    fun pause() {
        if (sourceState == AL10.AL_PLAYING) {
            AL10.alSourcePause(pointer)
        }
    }

    fun resume() {
        if (sourceState == AL10.AL_PAUSED) {
            AL10.alSourcePlay(pointer)
        }
    }

    fun stop() {
        if (playing.get()) {
            AL10.alSourceStop(pointer)
            checkErrors("Stop")
        }
    }

    fun isPlaying():bool {
        return sourceState == AL10.AL_PLAYING
    }

    val isStopped:bool
        get() = sourceState == AL10.AL_STOPPED

    fun setPosition() {
        AL10.alSourcefv(pointer, AL10.AL_POSITION, floatArrayOf(0f, 0f, 0f))
    }

    fun setPitch(pitch:float) {
        AL10.alSourcef(pointer, AL10.AL_PITCH, pitch)
    }

    fun setLooping(looping:bool) {
        AL10.alSourcei(pointer, AL10.AL_LOOPING, if (looping) 1 else 0)
    }

    fun setVolume(volume:float) {
        AL10.alSourcef(pointer, AL10.AL_SOURCE_STATE, volume)
    }

    fun disableAttenuation() {
        AL10.alSourcei(pointer, AL10.AL_DISTANCE_MODEL, 0)
    }

    fun setAttenuation(attenuation:float) {
        AL10.alSourcei(pointer, AL10.AL_DISTANCE_MODEL, AL11.AL_LINEAR_DISTANCE)
        AL10.alSourcef(pointer, AL10.AL_MAX_DISTANCE, attenuation)
        AL10.alSourcef(pointer, AL10.AL_ROLLOFF_FACTOR, 1.0f)
        AL10.alSourcef(pointer, AL10.AL_REFERENCE_DISTANCE, 0.0f)
    }

    fun setRelative(relative:bool) {
        AL10.alSourcei(pointer, AL10.AL_SOURCE_RELATIVE, if (relative) 1 else 0)
    }

    fun setBuffer(sound:StaticSound) {
        sound.streamBufferPointer_OI.ifPresent { pointer:int -> AL10.alSourcei(this.pointer, AL10.AL_BUFFER, pointer) }
    }

    fun setStream(stream:AudioStream) {
        this.stream = stream
        val audioFormat = stream.format
        bufferSize = getBufferSize(audioFormat, 1)
        method19640(4)
    }

    private fun method19640(i:int) {
        if (stream != null) {
            try {
                for (j in 0 until i) {
                    val byteBuffer = stream!!.getBuffer(bufferSize)
                    if (byteBuffer != null) {
                        StaticSound(byteBuffer, stream!!.format!!).takeStreamBufferPointer().ifPresent { ix:int ->
                            AL10.alSourceQueueBuffers(
                                pointer, intArrayOf(ix))
                        }
                    }
                }
            } catch (var4:IOException) {
                var4.printStackTrace()
            }
        }
    }

    fun tick() {
        if (stream != null) {
            val i = removeProcessedBuffers()
            method19640(i)
        }
    }

    private fun removeProcessedBuffers():int {
        val i = AL10.alGetSourcei(pointer, AL10.AL_BUFFERS_PROCESSED)
        if (i > 0) {
            val `is` = IntArray(i)
            AL10.alSourceUnqueueBuffers(pointer, `is`)
            checkErrors("Unqueue buffers")
            AL10.alDeleteBuffers(`is`)
            checkErrors("Remove processed buffers")
        }
        return i
    }

    companion object {
        fun create():Source? {
            val `is` = IntArray(1)
            AL10.alGenSources(`is`)
            return if (checkErrors("Allocate new source")) null else Source(`is`[0])
        }

        private fun getBufferSize(format:AudioFormat?, time:int):int {
            return ((time * format!!.sampleSizeInBits).toFloat() / 8.0f * format.channels.toFloat() * format.sampleRate).toInt()
        }
    }
}