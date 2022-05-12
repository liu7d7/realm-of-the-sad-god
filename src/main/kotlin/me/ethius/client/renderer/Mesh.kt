package me.ethius.client.renderer

import me.ethius.client.Client
import me.ethius.client.ext.transform
import me.ethius.client.main_tex
import me.ethius.shared.*
import org.joml.Matrix4d
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11C
import org.lwjgl.opengl.GL15C
import org.lwjgl.opengl.GL30.*
import java.nio.ByteBuffer

open class Mesh(drawMode:DrawMode, val shader:Shader?, vararg attributes:Attrib) {

    enum class Attrib(val size:int) {
        float(1), tex(2), vec2(3), vec3(3), color(4);
    }

    var alpha = 1.0f
    private val drawMode:DrawMode
    private val primitiveVerticesSize:int
    private val vao:int
    private val vbo:int
    private val ibo:int
    var depthTest = false
    private var vertices:ByteBuffer
    private var indices:ByteBuffer
    private var vertexI = 0
    private var indicesCount = 0
    var building = false
    private var cameraX = 0.0f
    private var cameraZ = 0.0f
    private var beganRendering = false
    var uniformSetter:(() -> void)? = null

    fun begin() {
        check(!building) { "Mesh.begin() called while already building." }
        vertices.clear()
        indices.clear()
        vertexI = 0
        indicesCount = 0
        building = true
        cameraX = 0.0f
        cameraZ = 0.0f
    }

    fun end():Mesh {
        check(building) { "Mesh.end() called while not building." }
        if (indicesCount > 0) {
            vertices.flip()
            glBindBuffer(GL_ARRAY_BUFFER, vbo)
            glBufferData(GL_ARRAY_BUFFER, vertices, GL15C.GL_DYNAMIC_DRAW)
            glBindBuffer(GL_ARRAY_BUFFER, 0)
            indices.flip()
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo)
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL15C.GL_DYNAMIC_DRAW)
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
        }
        building = false
        return this
    }

    private fun beginRender() {
        saveState()
        if (!depthTest) disableDepth() else enableDepth()
        enableBlend()
        beganRendering = true
    }

    fun render() {
        if (building) end()
        if (indicesCount > 0) {
            /* Setup opengl state and matrix stack */
            val wasBeganRendering = beganRendering
            if (!wasBeganRendering) {
                beginRender()
            }

            /* Render */
            beforeRender()
            Shader.activeShader?.setDefaults()
            uniformSetter?.invoke()
            bindVertexArray(vao)
            drawElements(drawMode.asGl, indicesCount, GL11C.GL_UNSIGNED_INT)

            /* Cleanup opengl state and matrix stack */
            bindVertexArray(0)
            if (!wasBeganRendering) {
                endRender()
            }
        }
        numDrawCalls++
    }

    private fun endRender() {
        restoreState()
        beganRendering = false
    }

    fun addVertex(x:float, y:float, z:float):Mesh {
        vertices.putFloat(x)
        vertices.putFloat(y)
        vertices.putFloat(z)
        return this
    }

    fun addVertex(x:double, y:double, z:double):Mesh {
        vertices.putFloat(x.toFloat())
        vertices.putFloat(y.toFloat())
        vertices.putFloat(z.toFloat())
        return this
    }

    fun addVertex(m4f:Matrix4d, x:double, y:double, z:double):Mesh {
        val vec4f = dvec4(x, y, z, 1.0)
        vec4f.transform(m4f)
        vertices.putFloat(vec4f.x.toFloat())
        vertices.putFloat(vec4f.y.toFloat())
        vertices.putFloat(vec4f.z.toFloat())
        return this
    }

    fun tex(u:double, v:double):Mesh {
        vertices.putFloat(u.toFloat())
        vertices.putFloat(v.toFloat())
        return this
    }

    fun tex(x:double, y:double, tex:Texture):Mesh {
        vertices.putFloat((x * tex.invWidth).toFloat())
        vertices.putFloat((y * tex.invHeight).toFloat())
        return this
    }

    fun addVertex(m4f:Matrix4d, x:double, y:double):Mesh {
        val vec4f = dvec4(x, y, 0.0, 1.0)
        vec4f.transform(m4f)
        vertices.putFloat(vec4f.x.toFloat())
        vertices.putFloat(vec4f.y.toFloat())
        vertices.putFloat(vec4f.z.toFloat())
        return this
    }

    fun color(color:long):Mesh {
        vertices.putFloat((color shr 16 and 0xff) * 0.003921569f)
        vertices.putFloat((color shr 8 and 0xff) * 0.003921569f)
        vertices.putFloat((color and 0xff) * 0.003921569f)
        vertices.putFloat((color shr 24 and 0xff) * 0.003921569f * alpha)
        return this
    }

    fun color(a:float, r:float, g:float, b:float):Mesh {
        vertices.putFloat(r)
        vertices.putFloat(g)
        vertices.putFloat(b)
        vertices.putFloat(a * alpha)
        return this
    }

    fun float(f:float):Mesh {
        vertices.putFloat(f)
        return this
    }

    fun float(f:int):Mesh {
        vertices.putFloat(f.toFloat())
        return this
    }

    operator fun next():int {
        return vertexI++
    }

    fun single(i1:int) {
        indices.putInt(i1)
        indicesCount++
        grow()
    }

    fun line(i1:int, i2:int) {
        indices.putInt(i1)
        indices.putInt(i2)
        indicesCount += 2
        grow()
    }

    fun triangle(i1:int, i2:int, i3:int) {
        indices.putInt(i1)
        indices.putInt(i2)
        indices.putInt(i3)
        indicesCount += 3
        grow()
    }

    fun quad(i1:int, i2:int, i3:int, i4:int) {
        indices.putInt(i1)
        indices.putInt(i2)
        indices.putInt(i3)
        indices.putInt(i3)
        indices.putInt(i4)
        indices.putInt(i1)
        indicesCount += 6
        grow()
    }

    private fun grow() {
        /* Vertices */
        if ((vertexI + 1) * primitiveVerticesSize >= vertices.capacity()) {
            var newSize = vertices.capacity() * 2
            if (newSize % primitiveVerticesSize != 0) newSize += newSize % primitiveVerticesSize
            val newVertices = BufferUtils.createByteBuffer(newSize)
            vertices.flip()
            newVertices.put(vertices)
            vertices = newVertices
        }

        /* Indices */
        if (indicesCount * 4 >= indices.capacity()) {
            var newSize = indices.capacity() * 2
            if (newSize % drawMode.indices != 0) newSize += newSize % (drawMode.indices * 4)
            val newIndices = BufferUtils.createByteBuffer(newSize)
            indices.flip()
            newIndices.put(indices)
            indices = newIndices
        }
    }

    protected open fun beforeRender() {
        shader?.bind()
    }

    fun dispose() {
        glDeleteVertexArrays(vao)
        glDeleteBuffers(vbo)
        glDeleteBuffers(ibo)
    }

    init {
        var stride = 0
        for (attribute in attributes) stride += attribute.size * 4
        this.drawMode = drawMode
        primitiveVerticesSize = stride * drawMode.indices
        vertices = BufferUtils.createByteBuffer(primitiveVerticesSize * 256 * 4)
        indices = BufferUtils.createByteBuffer(drawMode.indices * 512 * 4)
        vao = glGenVertexArrays()
        glBindVertexArray(vao)
        vbo = glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        ibo = glGenBuffers()
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo)
        var offset = 0
        for (i in attributes.indices) {
            val size = attributes[i].size
            glEnableVertexAttribArray(i)
            glVertexAttribPointer(i, size, GL11C.GL_FLOAT, false, stride, offset.toLong())
            offset += size * 4
        }
        glBindVertexArray(0)
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
    }

    companion object {
        var numDrawCalls = 0

        lateinit var triangles:Mesh private set
        lateinit var lines:Mesh private set
        lateinit var triangleFans:Mesh private set
        lateinit var model3dMesh:Mesh private set

        fun init() {
            triangles = Mesh(DrawMode.triangle, Shaders.pos_tex_color, Attrib.vec3, Attrib.tex, Attrib.color, Attrib.float)
            triangleFans = Mesh(DrawMode.triangle_fan, Shaders.pos_tex_color, Attrib.vec3, Attrib.tex, Attrib.color, Attrib.float)
            lines = Mesh(DrawMode.line, Shaders.pos_color, Attrib.vec2, Attrib.color)
            model3dMesh = Mesh(DrawMode.triangle, Shaders.pos_tex_color_lighting, Attrib.vec3, Attrib.vec3, Attrib.tex, Attrib.color)
            model3dMesh.depthTest = true
        }

        fun drawTriangles(mtId:int = main_tex.id) {
            triangles.end()
            bindTexture(mtId, 0) {
                triangles.shader?.set("u_Texture0", 0)
            }
            if (Client.font.font != null) {
                bindTexture(Client.font.font!!.texture.id, 1) {
                    triangles.shader?.set("u_Texture1", 1)
                }
                Client.font.end()
            }
            triangles.render()
        }
    }

}