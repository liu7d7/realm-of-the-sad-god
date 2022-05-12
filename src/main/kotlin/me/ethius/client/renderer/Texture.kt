package me.ethius.client.renderer

import me.ethius.shared.int
import me.ethius.shared.readResource
import me.ethius.shared.string
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL42.glGenerateMipmap
import org.lwjgl.opengl.GL42.glTexStorage2D
import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryStack
import java.io.InputStream
import java.nio.Buffer
import java.nio.ByteBuffer

/**
 * This class represents a texture.
 *
 * @author Heiko Brumme
 */
class Texture {
    /**
     * Stores the handle of the texture.
     */
    val id:int = glGenTextures()
    var data:ByteBuffer? = null

    /**
     * Width of the texture.
     */
    var width = 0
        set(width) {
            if (width > 0) {
                field = width
            }
            this.invWidth = 1.0 / width.toDouble()
        }
    var invWidth = 0.0

    /**
     * Height of the texture.
     */
    var height = 0
        set(height) {
            if (height > 0) {
                field = height
            }
            this.invHeight = 1.0 / height.toDouble()
        }
    var invHeight = 0.0

    /**
     * Binds the texture.
     */
    fun bind() {
        bindTexture(id)
    }

    /**
     * Sets a parameter of the texture.
     *
     * @param name  Name of the parameter
     * @param value Value to set
     */
    fun setParameter(name:int, value:int) {
        textureParam(GL_TEXTURE_2D, name, value)
    }

    /**
     * Uploads image data with specified internal format, width, height and
     * image format.
     *
     * @param internalFormat Internal format of the image data
     * @param width          Width of the image
     * @param height         Height of the image
     * @param format         Format of the image data
     * @param data           Pixel data of the image
     */
    fun uploadData(
        internalFormat:int,
        width:int,
        height:int,
        format:int,
        data:ByteBuffer,
    ) {
        glTexStorage2D(GL_TEXTURE_2D, 1, internalFormat, width, height)
        glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width, height, format, GL_UNSIGNED_BYTE, data)
        this.data = data
    }

    /**
     * Delete the texture.
     */
    fun delete() {
        glDeleteTextures(id)
    }

    fun freeImageData() {
        data?.let {
            STBImage.stbi_image_free(it)
        }
        data = null
    }

    companion object {
        /**
         * Creates a texture with specified width, height and data.
         *
         * @param width  Width of the texture
         * @param height Height of the texture
         * @param data   Picture Data in RGBA format
         *
         * @return Texture from the specified data
         */
        private fun createTexture(width:int, height:int, data:ByteBuffer):Texture {
            val texture = Texture()
            texture.width = width
            texture.height = height
            glBindTexture(GL_TEXTURE_2D, texture.id)
            texture.setParameter(GL_TEXTURE_WRAP_S, GL13.GL_CLAMP_TO_BORDER)
            texture.setParameter(GL_TEXTURE_WRAP_T, GL13.GL_CLAMP_TO_BORDER)
            texture.setParameter(GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_NEAREST)
            texture.setParameter(GL_TEXTURE_MAG_FILTER, GL_NEAREST)
            texture.uploadData(GL_RGBA8, width, height, GL_RGBA, data)
            glGenerateMipmap(GL_TEXTURE_2D)
            return texture
        }

        /**
         * Creates a texture with specified width, height and data.
         *
         * @param width  Width of the texture
         * @param height Height of the texture
         * @param data   Picture Data in specified format
         *
         * @return Texture from the specified data
         */
        fun createTexture(
            width:int,
            height:int,
            data:ByteBuffer,
            filterMin:int,
            filterMag:int,
            format:int,
            internalFormat:int,
        ):Texture {
            val texture = Texture()
            texture.width = width
            texture.height = height
            glBindTexture(GL_TEXTURE_2D, texture.id)
            texture.setParameter(GL_TEXTURE_WRAP_S, GL13.GL_CLAMP_TO_BORDER)
            texture.setParameter(GL_TEXTURE_WRAP_T, GL13.GL_CLAMP_TO_BORDER)
            texture.setParameter(GL_TEXTURE_MIN_FILTER, filterMin)
            texture.setParameter(GL_TEXTURE_MAG_FILTER, filterMag)
            texture.uploadData(internalFormat, width, height, format, data)
            return texture
        }

        /**
         * Creates a texture with specified width, height and data.
         *
         * @param width  Width of the texture
         * @param height Height of the texture
         * @param data   Picture Data in specified format
         *
         * @return Texture from the specified data
         */
        fun createTextureMipped(
            width:int,
            height:int,
            data:ByteBuffer,
            filterMin:int,
            filterMag:int,
            format:int,
            internalFormat:int,
        ):Texture {
            val texture = Texture()
            texture.width = width
            texture.height = height
            glBindTexture(GL_TEXTURE_2D, texture.id)
            texture.setParameter(GL_TEXTURE_WRAP_S, GL13.GL_CLAMP_TO_BORDER)
            texture.setParameter(GL_TEXTURE_WRAP_T, GL13.GL_CLAMP_TO_BORDER)
            texture.setParameter(GL_TEXTURE_MIN_FILTER, filterMin)
            texture.setParameter(GL_TEXTURE_MAG_FILTER, filterMag)
            texture.uploadData(internalFormat, width, height, format, data)
            glGenerateMipmap(GL_TEXTURE_2D)
            return texture
        }

        /**
         * Load texture from file.
         *
         * @param path File path of the texture
         *
         * @return Texture from specified file
         */
        fun loadTexture(path:string):Texture {
            return loadTexture(Texture::class.java.getResourceAsStream(path)!!)
        }

        private fun loadTexture(inputStream:InputStream):Texture {
            var image:ByteBuffer
            var width:int
            var height:int
            val byteBuffer = readResource(inputStream)
            (byteBuffer as Buffer).rewind()
            MemoryStack.stackPush().use { stack ->
                /* Prepare image buffers */
                val w = stack.mallocInt(1)
                val h = stack.mallocInt(1)
                val comp = stack.mallocInt(1)

                /* Load image */STBImage.stbi_set_flip_vertically_on_load(false)
                image = STBImage.stbi_load_from_memory(byteBuffer, w, h, comp, 4)!!

                /* Get width and height of image */
                width = w.get()
                height = h.get()
            }
            return createTexture(width, height, image)
        }
    }

}