package me.ethius.client.renderer

import me.ethius.shared.bool
import me.ethius.shared.double
import me.ethius.shared.events.Listen
import me.ethius.shared.events.def.WindowResizedEvent
import me.ethius.shared.int
import org.lwjgl.opengl.GL11C
import org.lwjgl.opengl.GL30C
import org.lwjgl.opengl.GL42

abstract class AFramebuffer(val useRenBuf:bool) {

    var id:int = -1
    var colorAttatchment:int = -1
    open var colorFormat:int = -1
    var depthAttatchment:int = -1
    var width:double = -1.0
    var height:double = -1.0

    fun bind() {
        bindFrameBuffer(id)
    }

    fun unbind() {
        bindFrameBuffer(0)
    }

    private fun dispose() {
        GL30C.glDeleteTextures(this.colorAttatchment)
        this.colorAttatchment = -1
        GL30C.glDeleteTextures(this.depthAttatchment)
        this.depthAttatchment = -1
        bindFrameBuffer(0)
        GL30C.glDeleteFramebuffers(this.id)
        this.id = -1
    }

    @Listen
    fun window(event:WindowResizedEvent) {
        dispose()
        init(event.width.toDouble(), event.height.toDouble())
    }

    open fun init(w:double, h:double) {
        this.width = w
        this.height = h
        this.id = GL30C.glGenFramebuffers()
        GL30C.glBindFramebuffer(GL30C.GL_FRAMEBUFFER, this.id)
        if (this.useRenBuf) {
            this.depthAttatchment = GL11C.glGenTextures()
            GL30C.glBindTexture(GL30C.GL_TEXTURE_2D, this.depthAttatchment)
            GL42.glTexParameteri(GL30C.GL_TEXTURE_2D, GL30C.GL_TEXTURE_MIN_FILTER, GL30C.GL_NEAREST)
            GL42.glTexParameteri(GL30C.GL_TEXTURE_2D, GL30C.GL_TEXTURE_MAG_FILTER, GL30C.GL_NEAREST)
            GL42.glTexParameteri(GL30C.GL_TEXTURE_2D, GL30C.GL_TEXTURE_COMPARE_MODE, GL30C.GL_NONE)
            GL42.glTexParameteri(GL30C.GL_TEXTURE_2D, GL30C.GL_TEXTURE_WRAP_S, GL30C.GL_CLAMP_TO_EDGE)
            GL42.glTexParameteri(GL30C.GL_TEXTURE_2D, GL30C.GL_TEXTURE_WRAP_T, GL30C.GL_CLAMP_TO_EDGE)
            GL42.glTexStorage2D(GL30C.GL_TEXTURE_2D, 1, GL30C.GL_DEPTH_COMPONENT32F, this.width.toInt(), this.height.toInt())
            GL30C.glFramebufferTexture2D(GL30C.GL_FRAMEBUFFER, GL30C.GL_DEPTH_ATTACHMENT, GL30C.GL_TEXTURE_2D, depthAttatchment, 0)
        }
        this.colorAttatchment = GL11C.glGenTextures()
        GL30C.glBindTexture(GL30C.GL_TEXTURE_2D, this.colorAttatchment)
        GL42.glTexParameteri(GL30C.GL_TEXTURE_2D, GL30C.GL_TEXTURE_MIN_FILTER, GL30C.GL_NEAREST)
        GL42.glTexParameteri(GL30C.GL_TEXTURE_2D, GL30C.GL_TEXTURE_MAG_FILTER, GL30C.GL_NEAREST)
        GL42.glTexParameteri(GL30C.GL_TEXTURE_2D, GL30C.GL_TEXTURE_WRAP_S, GL30C.GL_CLAMP_TO_BORDER)
        GL42.glTexParameteri(GL30C.GL_TEXTURE_2D, GL30C.GL_TEXTURE_WRAP_T, GL30C.GL_CLAMP_TO_BORDER)
        GL42.glTexStorage2D(GL30C.GL_TEXTURE_2D, 1, if (colorFormat == -1) GL30C.GL_RGBA8 else colorFormat, this.width.toInt(), this.height.toInt())
        GL30C.glFramebufferTexture2D(GL30C.GL_FRAMEBUFFER, GL30C.GL_COLOR_ATTACHMENT0, GL30C.GL_TEXTURE_2D, colorAttatchment, 0)
        // now that we actually created the framebuffer and added all attachments we want to check if it is actually complete
        GL30C.glBindFramebuffer(GL30C.GL_FRAMEBUFFER, this.id)
        check(GL30C.glCheckFramebufferStatus(GL30C.GL_FRAMEBUFFER) == GL30C.GL_FRAMEBUFFER_COMPLETE) {
            "Framebuffer is not complete!"
        }
        GL30C.glBindFramebuffer(GL30C.GL_FRAMEBUFFER, 0)
    }

}