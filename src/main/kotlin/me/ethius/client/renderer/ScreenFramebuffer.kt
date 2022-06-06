package me.ethius.client.renderer

import me.ethius.client.Client
import me.ethius.client.iden_m4d
import me.ethius.shared.bool
import me.ethius.shared.double
import me.ethius.shared.events.Listen
import me.ethius.shared.events.def.WindowResizedEvent
import org.joml.Matrix4d
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30C.*

class ScreenFramebuffer(w:double, h:double, useRenBuf:bool):AFramebuffer(useRenBuf) {

    constructor(useRenBuf:bool):this(Client.window.width, Client.window.height, useRenBuf)

    lateinit var projMat:Matrix4d

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

    fun copyDepthFrom(p_83946_:ScreenFramebuffer) {
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

    fun copyDepthFromToColor(p_83946_:ScreenFramebuffer) {
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

    fun copyColorFrom(p_83946_:ScreenFramebuffer) {
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
                          GL_NEAREST)
        unbind()
    }

    override fun init(w:double, h:double) {
        super.init(w, h)
        this.projMat = Matrix4d().ortho(0.0, this.width, this.height, 0.0, -1.0, 1.0)
    }

    @Listen
    fun onResize(e:WindowResizedEvent) {
        dispose()
        init(e.width, e.height)
    }

    init {
        init(w, h)
        Client.events.register(this)
    }

}