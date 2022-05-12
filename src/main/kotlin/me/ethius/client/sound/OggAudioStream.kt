package me.ethius.client.sound

import com.google.common.collect.Lists
import me.ethius.shared.*
import org.lwjgl.BufferUtils
import org.lwjgl.PointerBuffer
import org.lwjgl.stb.STBVorbis
import org.lwjgl.stb.STBVorbisAlloc
import org.lwjgl.stb.STBVorbisInfo
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.util.*
import java.util.function.Consumer
import javax.sound.sampled.AudioFormat

class OggAudioStream(private val name:string):AudioStream {
    private val inputStream = OggAudioStream::class.java.getResourceAsStream(name)!!
    private var pointer:long = 0
    override var format:AudioFormat? = null
    private var buffer = MemoryUtil.memAlloc(8192)

    private fun readHeader():bool {
        val i = buffer.limit()
        val j = buffer.capacity() - i
        return if (j == 0) {
            true
        } else {
            val bs = ByteArray(j)
            val k = inputStream.read(bs)
            if (k == -1) {
                false
            } else {
                val l = buffer.position()
                buffer.limit(i + k)
                buffer.position(i)
                buffer.put(bs, 0, k)
                buffer.position(l)
                true
            }
        }
    }

    private fun increaseBufferSize() {
        val bl = buffer.position() == 0
        val bl2 = buffer.position() == buffer.limit()
        if (bl2 && !bl) {
            buffer.position(0)
            buffer.limit(0)
        } else {
            val byteBuffer = MemoryUtil.memAlloc(if (bl) 2 * buffer.capacity() else buffer.capacity())
            byteBuffer.put(buffer)
            MemoryUtil.memFree(buffer)
            byteBuffer.flip()
            buffer = byteBuffer
        }
    }

    private fun readOggFile(channelList:ChannelList):bool {
        if (pointer != 0L) {
            val memoryStack = MemoryStack.stackPush()
            run label80@{
                try {
                    val pointerBuffer:PointerBuffer = memoryStack.mallocPointer(1)
                    val intBuffer:IntBuffer = memoryStack.mallocInt(1)
                    val intBuffer2:IntBuffer = memoryStack.mallocInt(1)
                    while (true) {
                        val i = STBVorbis.stb_vorbis_decode_frame_pushdata(pointer,
                                                                           buffer,
                                                                           intBuffer,
                                                                           pointerBuffer,
                                                                           intBuffer2)
                        buffer.position(buffer.position() + i)
                        val j = STBVorbis.stb_vorbis_get_error(pointer)
                        if (j == 1) {
                            increaseBufferSize()
                            if (!readHeader()) {
                                memoryStack.close()
                                return false
                            }
                        } else {
                            if (j != 0) {
                                throw IOException("Failed to read Ogg file $j")
                            }
                            val k = intBuffer2[0]
                            if (k != 0) {
                                val l = intBuffer[0]
                                val pointerBuffer2 = pointerBuffer.getPointerBuffer(l)
                                if (l == 1) {
                                    this.readChannels(pointerBuffer2.getFloatBuffer(0, k), channelList)
                                    memoryStack.close()
                                    return true
                                }
                                check(l == 2) { "Invalid number of channels: $l" }
                                this.readChannels(pointerBuffer2.getFloatBuffer(0, k),
                                                  pointerBuffer2.getFloatBuffer(1, k),
                                                  channelList)
                                break
                            }
                        }
                    }
                } catch (var13:Throwable) {
                    try {
                        memoryStack.close()
                    } catch (var12:Throwable) {
                        var13.addSuppressed(var12)
                    }
                    throw var13
                }
                memoryStack.close()
                return true
            }
        }
        return false
    }

    private fun readChannels(FloatBuffer:FloatBuffer, channelList:ChannelList) {
        while (FloatBuffer.hasRemaining()) {
            channelList.addChannel(FloatBuffer.get())
        }
    }

    private fun readChannels(FloatBuffer:FloatBuffer, floatBuffer2:FloatBuffer, channelList:ChannelList) {
        while (FloatBuffer.hasRemaining() && floatBuffer2.hasRemaining()) {
            channelList.addChannel(FloatBuffer.get())
            channelList.addChannel(floatBuffer2.get())
        }
    }

    override fun close() {
        if (pointer != 0L) {
            STBVorbis.stb_vorbis_close(pointer)
            pointer = 0L
        }
        MemoryUtil.memFree(buffer)
        inputStream.close()
    }

    override fun getBuffer(size:int):ByteBuffer? {
        val channelList = ChannelList(size + 8192)
        while (readOggFile(channelList) && channelList.currentBufferSize < size);
        return channelList.getBuffer()
    }

    fun getBuffer():ByteBuffer? {
        val channelList = ChannelList(16384)
        while (readOggFile(channelList));
        return channelList.getBuffer()
    }

    private class ChannelList(size:int) {
        private val buffers:MutableList<ByteBuffer?> = Lists.newArrayList()
        private val size:int
        var currentBufferSize = 0
        private var buffer:ByteBuffer? = null
        private fun init() {
            buffer = BufferUtils.createByteBuffer(size)
        }

        fun addChannel(f:float) {
            if (buffer!!.remaining() == 0) {
                buffer!!.flip()
                buffers.add(buffer)
                init()
            }
            val i = clamp((f * 32767.5f - 0.5f).toInt(), -32768, 32767)
            buffer!!.putShort(i.toShort())
            currentBufferSize += 2
        }

        fun getBuffer():ByteBuffer? {
            buffer!!.flip()
            return if (buffers.isEmpty()) {
                buffer
            } else {
                val byteBuffer = BufferUtils.createByteBuffer(currentBufferSize)
                Objects.requireNonNull(byteBuffer)
                buffers.forEach(Consumer { src:ByteBuffer? -> byteBuffer.put(src) })
                byteBuffer.put(buffer)
                byteBuffer.flip()
                byteBuffer
            }
        }

        init {
            this.size = size + 1 and -2
            init()
        }
    }

    fun clone():OggAudioStream {
        return OggAudioStream(name)
    }

    init {
        buffer.limit(0)
        val memoryStack = MemoryStack.stackPush()
        try {
            val intBuffer = memoryStack.mallocInt(1)
            val intBuffer2 = memoryStack.mallocInt(1)
            while (true) {
                if (pointer != 0L) {
                    buffer.position(buffer.position() + intBuffer[0])
                    val sTBVorbisInfo = STBVorbisInfo.malloc(memoryStack)
                    STBVorbis.stb_vorbis_get_info(pointer, sTBVorbisInfo)
                    format =
                        AudioFormat(sTBVorbisInfo.sample_rate().toFloat(), 16, sTBVorbisInfo.channels(), true, false)
                    break
                }
                if (!readHeader()) {
                    throw IOException("Failed to find Ogg header")
                }
                val i = buffer.position()
                buffer.position(0)
                pointer = STBVorbis.stb_vorbis_open_pushdata(buffer, intBuffer, intBuffer2, null as STBVorbisAlloc?)
                buffer.position(i)
                val j = intBuffer2[0]
                if (j == 1) {
                    increaseBufferSize()
                } else if (j != 0) {
                    throw IOException("Failed to read Ogg file $j")
                }
            }
        } catch (var8:Throwable) {
            try {
                memoryStack.close()
            } catch (var7:Throwable) {
                var8.addSuppressed(var7)
            }
            throw var8
        }
        memoryStack.close()
    }
}