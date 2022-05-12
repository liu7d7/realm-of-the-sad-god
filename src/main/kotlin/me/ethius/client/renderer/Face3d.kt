package me.ethius.client.renderer

import me.ethius.shared.dvec3
import me.ethius.shared.ext.minus
import me.ethius.shared.opti.TexData
import org.joml.Matrix4dStack

class Face3d(val a:VertexData, val b:VertexData, val c:VertexData) {

    private val normal:dvec3

    fun upload(mesh:Mesh, matrix:Matrix4dStack, texData:TexData) {
        mesh.triangle(
            a.uploadWithNormal(mesh, matrix, texData),
            b.uploadWithNormal(mesh, matrix, texData),
            c.uploadWithNormal(mesh, matrix, texData))
    }

    override fun toString():String {
        return "Face3d(p0=$a, p1=$b, p2=$c)"
    }

    init {
        val ab = b.position - a.position
        val ac = c.position - a.position
        normal = ab.cross(ac).normalize()
        a.normal.set(normal)
        b.normal.set(normal)
        c.normal.set(normal)
    }
}