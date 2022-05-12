package me.ethius.client.renderer.postprocess

import me.ethius.client.Client
import me.ethius.client.renderer.Framebuffer
import me.ethius.client.renderer.Shader
import me.ethius.client.renderer.Shaders
import me.ethius.client.renderer.bindTexture
import me.ethius.shared.double

object Bokeh {

    private lateinit var fbo0:Framebuffer
    private lateinit var fbo1:Framebuffer

    private lateinit var blobs2:Shader
    private lateinit var blit:Shader
    private lateinit var outline_watercolor:Shader
    private lateinit var blur2:Shader
    private lateinit var outline_combine:Shader

    fun init() {
        fbo0 = me.ethius.client.renderer.Framebuffer(true)
        fbo1 = me.ethius.client.renderer.Framebuffer(true)

        blobs2 = Shaders.blobs2
        blit = Shaders.blit
        outline_watercolor = Shaders.outline_watercolor
        blur2 = Shaders.blur2
        outline_combine = Shaders.outline_combine
    }

    // render the bokeh effect
    fun render(
        progress:double,
        x0:double = 0.0,
        y0:double = 0.0,
        x1:double = Client.frameBufferObj.width,
        y1:double = Client.frameBufferObj.height,
    ) {
        // pass #0
        blobs2.bind()
        blobs2["ProjMat"] = Client.frameBufferObj.projMat
        blobs2["InSize", Client.frameBufferObj.width] = Client.frameBufferObj.height
        blobs2["Radius"] = (progress * 7f)
        fbo0.bind()
        bindTexture(Client.frameBufferObj.colorAttatchment)
        PostProcessRenderer.renderFs(x0, y0, x1, y1)


        // pass #1
        outline_watercolor.bind()
        outline_watercolor["ProjMat"] = fbo0.projMat
        outline_watercolor["InSize", fbo0.width] = fbo0.height
        outline_watercolor["OutSize", Client.frameBufferObj.width] = Client.frameBufferObj.height
        outline_watercolor["LumaRamp"] = 16.0f
        Client.frameBufferObj.bind()
        bindTexture(fbo0.colorAttatchment)
        PostProcessRenderer.renderFs(x0, y0, x1, y1)


        // pass #2
        blur2.bind()
        blur2["ProjMat"] = Client.frameBufferObj.projMat
        blur2["InSize", Client.frameBufferObj.width] = Client.frameBufferObj.height
        blur2["OutSize", fbo1.width] = fbo1.height
        blur2["BlurDir", 0f] = 0.8f
        blur2["Radius"] = (progress * 15f)
        fbo1.bind()
        bindTexture(Client.frameBufferObj.colorAttatchment)
        PostProcessRenderer.renderFs(x0, y0, x1, y1)


        // pass #3
        blur2["ProjMat"] = fbo1.projMat
        blur2["InSize", fbo1.width] = fbo1.height
        blur2["OutSize", Client.frameBufferObj.width] = Client.frameBufferObj.height
        blur2["BlurDir", 0.8f] = 0f
        blur2["Radius"] = (progress * 15f)
        Client.frameBufferObj.bind()
        bindTexture(fbo1.colorAttatchment)
        PostProcessRenderer.renderFs(x0, y0, x1, y1)


        // pass #4
        outline_combine.bind()
        outline_combine["ProjMat"] = fbo0.projMat
        outline_combine["InSize", fbo0.width] = fbo0.height
        outline_combine["OutSize", fbo1.width] = fbo1.height
        fbo1.bind()
        bindTexture(fbo0.colorAttatchment)
        PostProcessRenderer.renderFs(x0, y0, x1, y1)


        // pass #5
        blit.bind()
        blit["ProjMat"] = fbo1.projMat
        blit["OutSize", Client.frameBufferObj.width] = Client.frameBufferObj.height
        Client.frameBufferObj.bind()
        bindTexture(fbo1.colorAttatchment)
        PostProcessRenderer.renderFs(x0, y0, x1, y1)
    }
}