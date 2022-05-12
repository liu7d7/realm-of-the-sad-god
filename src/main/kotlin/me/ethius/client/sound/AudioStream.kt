package me.ethius.client.sound

import me.ethius.shared.int
import java.io.Closeable
import java.nio.ByteBuffer
import javax.sound.sampled.AudioFormat

interface AudioStream:Closeable {
    val format:AudioFormat?

    fun getBuffer(size:int):ByteBuffer?
}