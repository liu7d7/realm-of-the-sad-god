package me.ethius.client.renderer

import me.ethius.client.Client
import me.ethius.client.main_tex
import me.ethius.client.renderer.postprocess.EntityOutline
import me.ethius.client.renderer.postprocess.Shadow
import me.ethius.shared.bool
import me.ethius.shared.void

enum class RenderLayer(val begin:(RenderTaskTracker) -> void, val end:(RenderTaskTracker) -> void, val beEachTime:bool = false) {
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
            it.threeD.clearColorAndDepth()
            it.threeD.bind()
            Mesh.model3dMesh.begin()
        },
        {
            bindTexture(main_tex.id)
            Mesh.model3dMesh.render()
            Client.frameBufferObj.bind()
            it.threeD.draw(true, 0.0, it.threeD.height, it.threeD.width, 0.0)
            Client.render.setRendering3d(false)
        }),
    world_feature(
        {
            Client.render.setRendering3d(true)
            it.outlines.clearColorAndDepth()
            if (it.hasWorld3d)
                it.outlines.copyDepthFrom(it.threeD)
            else
                it.outlines.copyDepthFrom(Client.frameBufferObj)
            it.outlines.bind()
            Mesh.triangles.begin()
            Client.font.begin(0.965)
        },
        {
            Mesh.drawTriangles()
            it.outlines.bind()
            it.shadows.copyColorFrom(it.outlines)
            it.shadows.copyDepthFrom(it.outlines)
            it.shadows.bind()
            Shadow.render(it.shadows, 4f)
            it.shadows.unbind()
            it.outlines.bind()
            EntityOutline.render(it.outlines, 1)
            it.outlines.unbind()
            Client.render.setRendering3d(false)
            Client.frameBufferObj.bind()
            it.shadows.draw(false,
                            0.0,
                            0.0,
                            Client.window.scaledWidth,
                            Client.window.scaledHeight)
            it.outlines.draw(false,
                             0.0,
                             Client.window.scaledHeight,
                             Client.window.scaledWidth,
                             0.0)
        }),
    world_feature_text(
        {
            Mesh.triangles.begin()
        },
        {
            Mesh.drawTriangles()
        }),
    hud({  }, {  }),
    ignored({  }, {  });

    companion object {
        val notIgnoredWorld = values().filter { it != ignored && it != hud }
        val notIgnoredHud = arrayOf(hud)
    }
}