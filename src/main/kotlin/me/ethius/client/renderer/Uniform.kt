package me.ethius.client.renderer

import me.ethius.shared.*
import org.joml.Matrix4d
import org.lwjgl.opengl.GL11.GL_FALSE
import org.lwjgl.opengl.GL11.GL_TRUE
import java.util.*

class DummyUniform:Uniform("dummy", -1) {
    override fun set(v:bool) {

    }

    override fun set(v:int) {

    }

    override fun set(v:float) {

    }

    override fun set(v:double) {

    }

    override fun set(mat:Matrix4d) {

    }

    override fun set(v1:float, v2:float) {

    }

    override fun set(v1:double, v2:double) {

    }
}

open class Uniform(val name:string, val location:int) {

    private val id = UUID.randomUUID()

    open fun set(v:bool) {
        uniformInt(location, if (v) GL_TRUE else GL_FALSE)
    }

    open fun set(v:int) {
        uniformInt(location, v)
    }

    open fun set(v:float) {
        uniformFloat(location, v)
    }

    open fun set(v:double) {
        uniformFloat(location, v.toFloat())
    }

    open fun set(v1:float, v2:float) {
        uniformFloat2(location, v1, v2)
    }

    open fun set(v1:double, v2:double) {
        uniformFloat2(location, v1.toFloat(), v2.toFloat())
    }

    open fun set(mat:Matrix4d) {
        uniformMatrix(location, mat)
    }

    override fun equals(other:Any?):Boolean {
        return other is Uniform && other.id == id
    }

    override fun hashCode():Int {
        return id.hashCode()
    }

    companion object {
        val dummy_uniform = DummyUniform()
    }
}