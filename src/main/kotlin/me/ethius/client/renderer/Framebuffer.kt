package me.ethius.client.renderer

import me.ethius.client.Client
import me.ethius.client.iden_m4d
import me.ethius.shared.bool
import me.ethius.shared.double
import me.ethius.shared.events.Listen
import me.ethius.shared.events.def.WindowResizedEvent
import me.ethius.shared.int
import org.joml.Matrix4d
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11C
import org.lwjgl.opengl.GL30C.*
import org.lwjgl.opengl.GL42.glTexParameteri
import org.lwjgl.opengl.GL42.glTexStorage2D

class Framebuffer(w:double, h:double, val useRenBuf:bool) {

    constructor(useRenBuf:bool):this(Client.window.width.toDouble(), Client.window.height.toDouble(), useRenBuf)

    var id:int = -1
    var colorAttatchment:int = -1
    var depthAttatchment:int = -1
    var width:double = -1.0
    var height:double = -1.0
    lateinit var projMat:Matrix4d

    fun bind() {
        bindFrameBuffer(id)
    }

    fun unbind() {
        bindFrameBuffer(0)
    }

    private fun dispose() {
        glDeleteTextures(this.colorAttatchment)
        this.colorAttatchment = -1
        glDeleteTextures(this.depthAttatchment)
        this.depthAttatchment = -1
        bindFrameBuffer(0)
        glDeleteFramebuffers(this.id)
        this.id = -1
    }

    fun draw(setViewport:bool = true, x0:double = 0.0, y0:double = 0.0, x1:double = width, y1:double = height) {
        enableBlend()
        val prevMat = Client.projMat
        val prevLookAt = Client.renderTaskTracker.lookAt
        Client.renderTaskTracker.lookAt = iden_m4d
        if (setViewport) {
            Client.projMat = projMat
        }
        val rTex = Mesh.triangles
        if (setViewport) {
            glViewport(0, 0, width.toInt(), height.toInt())
        }
        rTex.begin()
        rTex.quad(
            rTex.addVertex(iden_m4d, x0, y0).tex(0.0, 0.0).color(0xffffffff).float(0).next(),
            rTex.addVertex(iden_m4d, x1, y0).tex(1.0, 0.0).color(0xffffffff).float(0).next(),
            rTex.addVertex(iden_m4d, x1, y1).tex(1.0, 1.0).color(0xffffffff).float(0).next(),
            rTex.addVertex(iden_m4d, x0, y1).tex(0.0, 1.0).color(0xffffffff).float(0).next()
        )
        rTex.end()
        bindTexture(colorAttatchment)
        rTex.render()
        disableBlend()
        Client.renderTaskTracker.lookAt = prevLookAt
        if (setViewport) {
            Client.projMat = prevMat
        }
    }

    @Listen
    fun window(event:WindowResizedEvent) {
        dispose()
        init(event.width.toDouble(), event.height.toDouble())
    }

    fun copyDepthFrom(p_83946_:Framebuffer) {
        glBindFramebuffer(GL_READ_FRAMEBUFFER, p_83946_.id)
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, id)
        glBlitFramebuffer(0,
                          0,
                          width.toInt(),
                          height.toInt(),
                          0,
                          0,
                          width.toInt(),
                          height.toInt(),
                          GL_DEPTH_BUFFER_BIT,
                          GL_NEAREST)
        unbind()
    }

    fun copyDepthFromToColor(p_83946_:Framebuffer) {
        val prevMat = Client.projMat
        val prevLookAt = Client.renderTaskTracker.lookAt
        Client.renderTaskTracker.lookAt = iden_m4d
        Client.projMat = projMat
        bind()
        val rTex = Mesh.triangles
        rTex.begin()
        rTex.quad(
            rTex.addVertex(iden_m4d, 0.0, 0.0).tex(0.0, 1.0).color(0xffffffff).float(0).next(),
            rTex.addVertex(iden_m4d, width, 0.0).tex(1.0, 1.0).color(0xffffffff).float(0).next(),
            rTex.addVertex(iden_m4d, width, height).tex(1.0, 0.0).color(0xffffffff).float(0).next(),
            rTex.addVertex(iden_m4d, 0.0, height).tex(0.0, 0.0).color(0xffffffff).float(0).next()
        )
        rTex.end()
        bindTexture(p_83946_.depthAttatchment)
        rTex.render()
        unbind()
        Client.renderTaskTracker.lookAt = prevLookAt
        Client.projMat = prevMat
    }

    fun clearColorAndDepth() {
        bind()
        GL11.glClearColor(0f, 0f, 0f, 0f)
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
        unbind()
    }

    fun copyColorFrom(p_83946_:Framebuffer) {
        glBindFramebuffer(GL_READ_FRAMEBUFFER, p_83946_.id)
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, id)
        glBlitFramebuffer(0,
                          0,
                          width.toInt(),
                          height.toInt(),
                          0,
                          0,
                          width.toInt(),
                          height.toInt(),
                          GL_COLOR_BUFFER_BIT,
                          GL_NEAREST)
        unbind()
    }

    fun copyColorToFbo0() {
        glBindFramebuffer(GL_READ_FRAMEBUFFER, id)
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0)
        glBlitFramebuffer(0,
                          0,
                          width.toInt(),
                          height.toInt(),
                          0,
                          height.toInt(),
                          width.toInt(),
                          0,
                          GL_COLOR_BUFFER_BIT,
                          GL_LINEAR)
        unbind()
    }

    fun init(w:double, h:double) {
        this.width = w
        this.height = h
        this.projMat = Matrix4d().ortho(0.0, this.width, this.height, 0.0, -1.0, 1.0)
        this.id = glGenFramebuffers()
        glBindFramebuffer(GL_FRAMEBUFFER, this.id)
        if (this.useRenBuf) {
            this.depthAttatchment = GL11C.glGenTextures()
            glBindTexture(GL_TEXTURE_2D, this.depthAttatchment)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
            glTexParameteri(GL_TEXTURE_2D, 34892, 0)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
            glTexStorage2D(GL_TEXTURE_2D, 1, GL_DEPTH_COMPONENT32F, this.width.toInt(), this.height.toInt())
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depthAttatchment, 0)
        }
        this.colorAttatchment = GL11C.glGenTextures()
        glBindTexture(GL_TEXTURE_2D, this.colorAttatchment)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER)
        glTexStorage2D(GL_TEXTURE_2D, 1, GL_RGBA8, this.width.toInt(), this.height.toInt())
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, colorAttatchment, 0)
        // now that we actually created the framebuffer and added all attachments we want to check if it is actually complete
        glBindFramebuffer(GL_FRAMEBUFFER, this.id)
        check(glCheckFramebufferStatus(GL_FRAMEBUFFER) == GL_FRAMEBUFFER_COMPLETE) {
            "Framebuffer is not complete!"
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }

    init {
        init(w, h)
        Client.events.register(this)
    }

}