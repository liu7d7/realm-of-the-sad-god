package me.ethius.client.renderer

import me.ethius.client.main_tex
import me.ethius.shared.*
import me.ethius.shared.opti.TexData
import org.joml.Matrix4dStack

class VertexData {
    var position:dvec3 = dvec3()
    var texCoords:dvec2 = dvec2()
    var color:long = 0xffffffff
    var normal:dvec3 = dvec3()

    fun uploadWithNormal(mesh:Mesh, matrix:Matrix4dStack, texData:TexData):int {
        return mesh.addVertex(matrix, position.x, position.y, position.z)
            .addVertex(normal.x, normal.y, normal.z)
            .tex(lerp(0.0, texData.width, texCoords.x) + texData.u, lerp(0.0, texData.height, texCoords.y) + texData.v, main_tex)
            .color(color).next()
    }

    override fun toString():String {
        return "VertexData_ptci(position=$position, texCoords=$texCoords, color=$color)"
    }
}