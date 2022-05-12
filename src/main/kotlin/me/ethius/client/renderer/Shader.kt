package me.ethius.client.renderer

import it.unimi.dsi.fastutil.objects.Object2IntMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import me.ethius.client.Client
import me.ethius.shared.*
import org.apache.commons.io.IOUtils
import org.joml.Matrix4d
import org.lwjgl.opengl.GL11C
import org.lwjgl.opengl.GL20C
import java.io.IOException
import java.nio.charset.StandardCharsets

class Shader(vsh:string, fsh:string) {

    private val id:int
    private val uniformLocations:Object2IntMap<string> = Object2IntOpenHashMap()


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

    private fun getLocation(name:string):int {
        if (uniformLocations.containsKey(name)) return uniformLocations.getInt(name)
        val location = getUniformLocation(id, name)
        uniformLocations[name] = location
        return location
    }

    operator fun set(name:string, v:bool) {
        val loc = getLocation(name)
        if (loc == -1) return
        uniformInt(loc, if (v) GL11C.GL_TRUE else GL11C.GL_FALSE)
    }

    operator fun set(name:string, v:int) {
        val loc = getLocation(name)
        if (loc == -1) return
        uniformInt(loc, v)
    }

    operator fun set(name:string, v:float) {
        val loc = getLocation(name)
        if (loc == -1) return
        uniformfloat(loc, v)
    }

    operator fun set(name:string, v:double) {
        val loc = getLocation(name)
        if (loc == -1) return
        uniformfloat(loc, v.toFloat())
    }

    operator fun set(name:string, v1:float, v2:float) {
        val loc = getLocation(name)
        if (loc == -1) return
        uniformfloat2(loc, v1, v2)
    }

    operator fun set(name:string, v1:double, v2:double) {
        val loc = getLocation(name)
        if (loc == -1) return
        uniformfloat2(loc, v1.toFloat(), v2.toFloat())
    }

    operator fun set(name:string, mat:Matrix4d) {
        val loc = getLocation(name)
        if (loc == -1) return
        uniformMatrix(loc, mat)
    }

    fun setDefaults() {
//        set("debug", Main.options.debug)
        set("layer", Client.renderTaskTracker.layer.ordinal)
        set("projMat", Client.projMat)
        set("lookAt", Client.renderTaskTracker.lookAt)
        set("camAngleX", Client.camAngleX)
        set("layer", Client.renderTaskTracker.layer.ordinal)

    }

    companion object {
        var activeShader:Shader? = null
    }

    init {
        val vert = createShader(GL20C.GL_VERTEX_SHADER)
        shaderSource(vert, read(vsh))
        val vertError = compileShader(vert)
        if (vertError != null) {
            throw RuntimeException("Failed to compile vertex shader ($vsh): $vertError")
        }
        val frag = createShader(GL20C.GL_FRAGMENT_SHADER)
        shaderSource(frag, read(fsh))
        val fragError = compileShader(frag)
        if (fragError != null) {
            throw RuntimeException("Failed to compile fragment shader ($fsh): $fragError")
        }
        id = createProgram()
        val programError = linkProgram(id, vert, frag)
        if (programError != null) {
            throw RuntimeException("Failed to link program: $programError")
        }
        deleteShader(vert)
        deleteShader(frag)
    }

}