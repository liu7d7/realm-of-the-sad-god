package me.ethius.client.renderer

import me.ethius.client.Client
import me.ethius.shared.*
import me.ethius.shared.opti.TexData
import me.ethius.shared.rotsg.tile.tile_size
import org.apache.commons.io.IOUtils
import org.joml.Matrix4dStack
import java.nio.charset.Charset
import kotlin.math.absoluteValue
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.jvmErasure

class Model3d {

    var id:string = "unknown"

    private val vertices = ArrayList<dvec3>()
    private val uvs = ArrayList<dvec2>()
    private val faces = ArrayList<Face3d>()

    var size:double = 0.0
        private set

    fun upload(mesh:Mesh, transform:Matrix4dStack, texData:TexData) {
        for (i in faces) {
            i.upload(mesh, transform, texData)
        }
    }

    fun read(lines:List<string>, color:long, size:double):Model3d {
        vertices.clear()
        uvs.clear()
        faces.clear()
        this.size = size
        for (line in lines) {
            if (line.startsWith("#")) {
                continue
            }
            val split = line.split(" ")
            if (split[0] == "v") {
                val vec = dvec3()
                vec.x = split[1].toDouble() * size * 0.5
                vec.y = split[2].toDouble() * size * 0.5
                vec.z = split[3].toDouble() * size * 0.5
                vertices.add(vec)
            } else if (split[0] == "vt") {
                val vec = dvec2()
                vec.x = split[1].toDouble()
                vec.y = split[2].toDouble()
                uvs.add(vec)
            } else if (split[0] == "f") {
                val vertex1 = split[1].split("/")
                val vertex2 = split[2].split("/")
                val vertex3 = split[3].split("/")
                val face = Face3d(
                    VertexData().also {
                        it.position = vertices[vertex1[0].toInt() - 1]
                        it.texCoords = uvs[vertex1[1].toInt() - 1]
                        it.color = color
                    },
                    VertexData().also {
                        it.position = vertices[vertex2[0].toInt() - 1]
                        it.texCoords = uvs[vertex2[1].toInt() - 1]
                        it.color = color
                    },
                    VertexData().also {
                        it.position = vertices[vertex3[0].toInt() - 1]
                        it.texCoords = uvs[vertex3[1].toInt() - 1]
                        it.color = color
                    }
                )
                faces.add(face)
            }
        }
        normalizeTextureVectors()
        return this
    }

    private fun normalizeTextureVectors() {
        val maxTexX = faces.maxOf { arrayOf(it.a, it.b, it.c).maxOf { it.texCoords.x.absoluteValue } }
        val maxTexY = faces.maxOf { arrayOf(it.a, it.b, it.c).maxOf { it.texCoords.y.absoluteValue } }
        val invMaxX = 1.0 / maxTexX
        val invMaxY = 1.0 / maxTexY
        for (i in faces) {
            i.a.texCoords.x *= invMaxX
            i.a.texCoords.y *= invMaxY
            i.b.texCoords.x *= invMaxX
            i.b.texCoords.y *= invMaxY
            i.c.texCoords.x *= invMaxX
            i.c.texCoords.y *= invMaxY
        }
    }

    companion object {
        val values:HashMap<string, Model3d> = HashMap()

        val pyramid = Model3d().read(readLines("/assets/obj/pyramid.obj"), 0xffffffff, tile_size)
        val fence_post_thing = Model3d().read(readLines("/assets/obj/weirdfencepostthing.obj"), 0xffffffff, tile_size)

        private fun readLines(path:string):List<string> {
            return IOUtils.readLines(Client::class.java.getResourceAsStream(path)!!, Charset.defaultCharset())
        }

        operator fun get(name:string):Model3d {
            return values[name]!!
        }

        fun init() {
            for (i in this::class.declaredMemberProperties) {
                if (i.returnType.jvmErasure == Model3d::class) {
                    values[i.name] = (i as KProperty1<Companion, Model3d>).get(this)
                    values[i.name]!!.id = i.name
                }
            }
        }
    }

}