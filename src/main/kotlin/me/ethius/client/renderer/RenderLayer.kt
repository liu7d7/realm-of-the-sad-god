package me.ethius.client.renderer

import me.ethius.client.Client
import me.ethius.client.main_tex
import me.ethius.client.renderer.postprocess.EntityOutline
import me.ethius.client.renderer.postprocess.Shadow
import me.ethius.shared.bool
import me.ethius.shared.lambda_v
import me.ethius.shared.void
import org.lwjgl.opengl.GL11

enum class RenderLayer(val begin:() -> void, val end:() -> void, val beEachTime:bool = false) {
    tile(
        {
            Mesh.triangles.begin()
        },
        {
            Mesh.drawTriangles()
        }),
    world_feature_3d(
        {
            Client.render.setRendering3d(true)
            Client.renderTaskTracker.threeD.clearColorAndDepth()
            Client.renderTaskTracker.threeD.bind()
            Mesh.model3dMesh.begin()
        },
        {
            bindTexture(main_tex.id)
            Mesh.model3dMesh.render()
            Client.frameBufferObj.bind()
            Client.renderTaskTracker.threeD.draw(true, 0.0, Client.renderTaskTracker.threeD.height, Client.renderTaskTracker.threeD.width, 0.0)
            Client.render.setRendering3d(false)
        }),
    world_feature(
        {
            Client.render.setRendering3d(true)
            Client.renderTaskTracker.outlines.bind()
            GL11.glClearColor(0f, 0f, 0f, 0f)
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
            if (Client.renderTaskTracker.hasWorld3d)
                Client.renderTaskTracker.outlines.copyDepthFrom(Client.renderTaskTracker.threeD)
            else
                Client.renderTaskTracker.outlines.copyDepthFrom(Client.frameBufferObj)
            Client.renderTaskTracker.outlines.bind()
            Mesh.triangles.begin()
            Client.font.begin(0.965)
        },
        {
            Mesh.drawTriangles()
            Client.renderTaskTracker.shadows.copyColorFrom(Client.renderTaskTracker.outlines)
            Client.renderTaskTracker.shadows.copyDepthFrom(Client.renderTaskTracker.outlines)
            Client.renderTaskTracker.shadows.bind()
            Shadow.render(4f, Client.renderTaskTracker.shadows)
            Client.renderTaskTracker.shadows.unbind()
            Client.renderTaskTracker.outlines.bind()
            EntityOutline.render(Client.renderTaskTracker.outlines)
            Client.renderTaskTracker.outlines.unbind()
            Client.render.setRendering3d(false)
            Client.frameBufferObj.bind()
            Client.renderTaskTracker.shadows.draw(false,
                                                  0.0,
                                                  0.0,
                                                  Client.window.scaledWidth.toDouble(),
                                                  Client.window.scaledHeight.toDouble())
            Client.renderTaskTracker.outlines.draw(false,
                                                   0.0,
                                                   Client.window.scaledHeight.toDouble(),
                                                   Client.window.scaledWidth.toDouble(),
                                                   0.0)
        }),
    world_feature_text(
        {
            Mesh.triangles.begin()
        },
        {
            Mesh.drawTriangles()
        }),
    hud(lambda_v, lambda_v),
    ignored(lambda_v, lambda_v);

    companion object {
        val notIgnoredWorld = values().filter { it != ignored && it != hud }
        val notIgnoredHud = arrayOf(hud)
    }
}