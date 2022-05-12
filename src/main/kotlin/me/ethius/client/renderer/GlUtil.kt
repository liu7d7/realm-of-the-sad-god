package me.ethius.client.renderer

import me.ethius.client.Client
import me.ethius.client.ext.writeRowMajor
import me.ethius.shared.*
import org.joml.Matrix4d
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL30.*
import org.lwjgl.system.APIUtil
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import kotlin.math.ceil
import kotlin.math.floor

private val matrix = BufferUtils.createFloatBuffer(4 * 4)

private var depthEnabled = false
private var blendEnabled = false

private var depthSaved = false
private var blendSaved = false

/* Binding */
fun bindVertexArray(vao:int) {
    glBindVertexArray(vao)
}

fun bindFrameBuffer(fbo:int) {
    glBindFramebuffer(GL_FRAMEBUFFER, fbo)
}

fun drawElements(mode:int, first:int, type:int) {
    glDrawElements(mode, first, type, 0L)
}

/* Shaders */
fun createShader(type:int):int {
    return glCreateShader(type)
}

fun shaderSource(shader:int, source:string?) {
    val stack = MemoryStack.stackGet()
    val stackPointer = stack.pointer
    try {
        val sourceBuffer = MemoryUtil.memUTF8(source as CharSequence, true)
        val pointers = stack.mallocPointer(1)
        pointers.put(sourceBuffer)
        nglShaderSource(shader, 1, pointers.address0(), 0)
        APIUtil.apiArrayFree(pointers.address0(), 1)
    } finally {
        stack.pointer = stackPointer
    }
}

fun compileShader(shader:int):string? {
    glCompileShader(shader)
    return if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE)
        glGetShaderInfoLog(shader, 512)
    else null
}

fun createProgram():int {
    return glCreateProgram()
}

fun linkProgram(program:int, vertShader:int, fragShader:int):string? {
    glAttachShader(program, vertShader)
    glAttachShader(program, fragShader)
    glLinkProgram(program)
    return if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
        glGetProgramInfoLog(program, 512)
    } else null
}

fun deleteShader(shader:int) {
    glDeleteShader(shader)
}

fun useProgram(program:int) {
    glUseProgram(program)
}

/* Uniforms */
fun getUniformLocation(program:int, name:string):int {
    return glGetUniformLocation(program, name)
}

fun uniformInt(location:int, v:int) {
    glUniform1i(location, v)
}

fun uniformfloat(location:int, v:float) {
    glUniform1f(location, v)
}

fun uniformfloat2(location:int, v1:float, v2:float) {
    glUniform2f(location, v1, v2)
}

fun uniformMatrix(location:int, v:Matrix4d) {
    v.writeRowMajor(matrix)
    glUniformMatrix4fv(location, false, matrix)
}

fun textureParam(target:int, name:int, param:int) {
    glTexParameteri(target, name, param)
}

inline fun bindTexture(texId:int, layer:int = 0, after:() -> void = { }) {
    glActiveTexture(GL_TEXTURE0 + layer)
    glBindTexture(GL_TEXTURE_2D, texId)
    after()
}

/* State */
fun saveState() {
    depthSaved = depthEnabled
    blendSaved = blendEnabled
}

fun restoreState() {
    if (depthSaved)
        enableDepth()
    else
        disableDepth()
    if (blendSaved)
        enableBlend()
    else
        disableBlend()
}

fun enableDepth() {
    if (depthEnabled) {
        return
    }
    glEnable(GL_DEPTH_TEST)
    depthEnabled = true
}

fun disableDepth() {
    if (!depthEnabled) {
        return
    }
    glDisable(GL_DEPTH_TEST)
    depthEnabled = false
}

fun enableBlend() {
    if (blendEnabled) {
        return
    }
    glEnable(GL_BLEND)
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
    blendEnabled = true
}

fun disableBlend() {
    if (!blendEnabled) {
        return
    }
    glDisable(GL_BLEND)
    blendEnabled = false
}

fun enableScissor(x:double, y:double, width:double, height:double) {
    val factor:double = getScale()
    glEnable(GL_SCISSOR_TEST)
    glScissor(
        floor(x * factor).toInt(),
        floor(y * factor).toInt(),
        ceil(width * factor).toInt(),
        ceil(height * factor).toInt()
    )
}

fun disableScissor() {
    glDisable(GL_SCISSOR_TEST)
}

private fun getScale():double = Client.window.width.toDouble() / Client.window.scaledWidth.toDouble()
