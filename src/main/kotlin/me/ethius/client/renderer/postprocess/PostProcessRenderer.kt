package me.ethius.client.renderer.postprocess

import me.ethius.client.Client
import me.ethius.client.renderer.Mesh
import me.ethius.shared.double

object PostProcessRenderer {
    lateinit var mesh:Mesh
    lateinit var meshFs:Mesh

    fun init() {
        mesh = Mesh(me.ethius.client.renderer.DrawMode.triangle, null, Mesh.Attrib.tex)
        mesh.begin()
        mesh.quad(
            mesh.tex(-1.0, -1.0).next(),
            mesh.tex(-1.0, 1.0).next(),
            mesh.tex(1.0, 1.0).next(),
            mesh.tex(1.0, -1.0).next()
        )
        mesh.end()
        meshFs = Mesh(me.ethius.client.renderer.DrawMode.triangle, null, Mesh.Attrib.tex)
    }

    fun renderFs(
        x0:double = 0.0,
        y0:double = 0.0,
        x1:double = Client.window.width.toDouble(),
        y1:double = Client.window.height.toDouble(),
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

    fun render() {
        mesh.render()
    }

}