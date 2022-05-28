package me.ethius.client.renderer

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import me.ethius.client.Client
import me.ethius.shared.*
import org.apache.commons.io.IOUtils
import org.joml.Matrix4d
import org.lwjgl.opengl.GL20C
import java.io.IOException
import java.nio.charset.StandardCharsets

class Shader(vsh:string, fsh:string) {

    private val id:int
    private val uniformLocations = Object2ObjectOpenHashMap<string, Uniform>()


    private fun read(path:string):string {
        return try {
            IOUtils.toString(javaClass.getResource(path)!!, StandardCharsets.UTF_8)
        } catch (e:IOException) {
            e.printStackTrace()
            ""
        }
    }

    fun bind() {
        useProgram(id)
        activeShader = this
    }

    fun unbind() {
        useProgram(0)
        activeShader = null
    }

    private fun getUniform(name:string):Uniform {
        if (uniformLocations.containsKey(name)) return uniformLocations[name] ?: throw IllegalArgumentException("Uniform $name not found when containsKey returned true")
        val location = getUniformLocation(id, name)
        var uniform = Uniform(name, location)
        if (location == -1) uniform = Uniform.dummy_uniform
        uniformLocations[name] = uniform
        return uniform
    }

    operator fun set(name:string, v:bool) {
        getUniform(name).set(v)
    }

    operator fun set(name:string, v:int) {
        getUniform(name).set(v)
    }

    operator fun set(name:string, v:float) {
        getUniform(name).set(v)
    }

    operator fun set(name:string, v:double) {
        getUniform(name).set(v)
    }

    operator fun set(name:string, v1:float, v2:float) {
        getUniform(name).set(v1, v2)
    }

    operator fun set(name:string, v1:double, v2:double) {
        getUniform(name).set(v1, v2)
    }

    operator fun set(name:string, mat:Matrix4d) {
        getUniform(name).set(mat)
    }

    fun setDefaults() {
        set("layer", Client.renderTaskTracker.layer.ordinal)
        set("projMat", Client.projMat)
        set("lookAt", Client.renderTaskTracker.lookAt)
        set("camAngleX", Client.camAngleX)
    }

    companion object {
        var activeShader:Shader? = null

        private val spToPath = Object2IntOpenHashMap<string>()
        fun disposeOldShaders() {
            for (sp in spToPath) {
                GL20C.glDeleteShader(sp.value)
            }
            spToPath.clear()
        }
    }

    init {
        val vert:int
        if (spToPath.containsKey(vsh)) {
            vert = spToPath.getInt(vsh)
        } else {
            vert = createShader(GL20C.GL_VERTEX_SHADER)
            shaderSource(vert, read(vsh))
            val vertError = compileShader(vert)
            if (vertError != null) {
                throw RuntimeException("Failed to compile vertex shader ($vsh): $vertError")
            }
            spToPath[vsh] = vert
        }
        val frag:int
        if (spToPath.containsKey(fsh)) {
            frag = spToPath.getInt(fsh)
        } else {
            frag = createShader(GL20C.GL_FRAGMENT_SHADER)
            shaderSource(frag, read(fsh))
            val fragError = compileShader(frag)
            if (fragError != null) {
                throw RuntimeException("Failed to compile fragment shader ($fsh): $fragError")
            }
            spToPath[fsh] = frag
        }
        id = createProgram()
        val programError = linkProgram(id, vert, frag)
        if (programError != null) {
            throw RuntimeException("Failed to link program: $programError")
        }
    }

}