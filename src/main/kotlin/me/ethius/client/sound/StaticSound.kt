package me.ethius.client.sound

import me.ethius.client.sound.AlUtil.checkErrors
import me.ethius.client.sound.AlUtil.getFormatId
import org.lwjgl.openal.AL10
import java.nio.ByteBuffer
import java.util.*
import javax.sound.sampled.AudioFormat

class StaticSound(private var sample:ByteBuffer?, private val format:AudioFormat) {
    private var hasBuffer = false
    var streamBufferPointer = 0
    val streamBufferPointer_OI:OptionalInt
        get() {
            if (!hasBuffer) {
                if (sample == null) {
                    return OptionalInt.empty()
                }
                val i = getFormatId(format)
                val `is` = IntArray(1)
                AL10.alGenBuffers(`is`)
                if (checkErrors("Creating buffer")) {
                    return OptionalInt.empty()
                }
                AL10.alBufferData(`is`[0], i, sample, format.sampleRate.toInt())
                if (checkErrors("Assigning buffer data")) {
                    return OptionalInt.empty()
                }
                streamBufferPointer = `is`[0]
                hasBuffer = true
                sample = null
            }
            return OptionalInt.of(streamBufferPointer)
        }

    fun close() {
        if (hasBuffer) {
            AL10.alDeleteBuffers(intArrayOf(streamBufferPointer))
            if (checkErrors("Deleting stream buffers")) {
                return
            }
        }
        hasBuffer = false
    }

    fun takeStreamBufferPointer():OptionalInt {
        val optionalInt = streamBufferPointer_OI
        hasBuffer = false
        return optionalInt
    }
}