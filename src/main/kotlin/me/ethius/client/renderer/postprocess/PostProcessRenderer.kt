package me.ethius.client.renderer.postprocess

import me.ethius.client.Client
import me.ethius.client.renderer.DrawMode
import me.ethius.client.renderer.Mesh
import me.ethius.shared.double

object PostProcessRenderer {
    lateinit var mesh:Mesh
    lateinit var meshFs:Mesh

    fun init() {
        mesh = Mesh(DrawMode.triangle, null, Mesh.Attrib.tex)
        mesh.begin()
        mesh.quad(
            mesh.tex(-1.0, -1.0).next(),
            mesh.tex(-1.0, 1.0).next(),
            mesh.tex(1.0, 1.0).next(),
            mesh.tex(1.0, -1.0).next()
        )
        mesh.end()
        meshFs = Mesh(DrawMode.triangle, null, Mesh.Attrib.tex)
    }

    fun renderFs(
        x0:double = 0.0,
        y0:double = 0.0,
        x1:double = Client.window.width,
        y1:double = Client.window.height,
    ) {
        val y1 = Client.window.height - y1
        val y0 = Client.window.height - y0
        meshFs.begin()
        meshFs.quad(
            meshFs.tex(x0, y0).next(),
            meshFs.tex(x1, y0).next(),
            meshFs.tex(x1, y1).next(),
            meshFs.tex(x0, y1).next()
        )
        meshFs.end().render()
    }

    fun renderNormalized(
        x0:double = 0.0,
        y0:double = 0.0,
        x1:double = Client.window.width,
        y1:double = Client.window.height,
        fboWidth:double = Client.window.width,
        fboHeight:double = Client.window.height
    ) {
        var y0 = fboHeight - y0
        y0 = (y0 / fboHeight) * 2.0 - 1.0
        var y1 = fboHeight - y1
        y1 = (y1 / fboHeight) * 2.0 - 1.0
        var x0 = x0
        x0 = (x0 / fboWidth) * 2.0 - 1.0
        var x1 = x1
        x1 = (x1 / fboWidth) * 2.0 - 1.0

        mesh.begin()
        mesh.quad(
            mesh.tex(x0, y0).next(),
            mesh.tex(x1, y0).next(),
            mesh.tex(x1, y1).next(),
            mesh.tex(x0, y1).next()
        )
        mesh.end().render()
    }

    fun render() {
        mesh.render()
    }

}