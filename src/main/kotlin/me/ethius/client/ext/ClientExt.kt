package me.ethius.client.ext

import me.ethius.shared.*
import org.joml.*
import java.lang.Math
import java.nio.FloatBuffer

fun Matrix4d.writeRowMajor(buf:FloatBuffer) {
    buf.put(0, this.a00.toFloat())
    buf.put(1, this.a01.toFloat())
    buf.put(2, this.a02.toFloat())
    buf.put(3, this.a03.toFloat())
    buf.put(4, this.a10.toFloat())
    buf.put(5, this.a11.toFloat())
    buf.put(6, this.a12.toFloat())
    buf.put(7, this.a13.toFloat())
    buf.put(8, this.a20.toFloat())
    buf.put(9, this.a21.toFloat())
    buf.put(10, this.a22.toFloat())
    buf.put(11, this.a23.toFloat())
    buf.put(12, this.a30.toFloat())
    buf.put(13, this.a31.toFloat())
    buf.put(14, this.a32.toFloat())
    buf.put(15, this.a33.toFloat())
}

inline fun Matrix4dStack.push(action:() -> Unit) {
    this.pushMatrix()
    action()
    this.popMatrix()
}

fun Matrix4d.multiply(quaternion:Quaterniond) {
    this.rotate(quaternion)
}

inline fun Matrix4d.multiply(quaternion:Quaterniond, action:() -> Unit) {
    this.rotate(quaternion)
    action()
    this.rotate(quaternion.invert())
}

inline fun Matrix4d.scale(x:double, y:double, z:double, action:() -> Unit) {
    this.scale(x, y, z)
    action()
    this.scale(1f / x, 1f / y, 1f / z)
}

inline fun Matrix4d.translate(x:double, y:double, z:double, action:() -> Unit) {
    this.multiplyByTranslation(x, y, z)
    action()
    this.multiplyByTranslation(-x, -y, -z)
}

fun Matrix4d.multiplyByTranslation(x:double, y:double, z:double) {
    this.a30 += Math.fma(this.a00, x, Math.fma(this.a10, y, this.a20 * z))
    this.a31 += Math.fma(this.a01, x, Math.fma(this.a11, y, this.a21 * z))
    this.a32 += Math.fma(this.a02, x, Math.fma(this.a12, y, this.a22 * z))
    this.a33 += Math.fma(this.a03, x, Math.fma(this.a13, y, this.a23 * z))
}

fun dvec4.transform(m4f:Matrix4d):dvec4 {
    val f = x
    val g = y
    val h = z
    val i = w
    this.x = Math.fma(m4f.a00, f, Math.fma(m4f.a10, g, Math.fma(m4f.a20, h, m4f.a30 * i)))
    this.y = Math.fma(m4f.a01, f, Math.fma(m4f.a11, g, Math.fma(m4f.a21, h, m4f.a31 * i)))
    this.z = Math.fma(m4f.a02, f, Math.fma(m4f.a12, g, Math.fma(m4f.a22, h, m4f.a32 * i)))
    this.w = Math.fma(m4f.a03, f, Math.fma(m4f.a13, g, Math.fma(m4f.a23, h, m4f.a33 * i)))
    return this
}

fun fvec3.getDegreesQuaternion(degrees:float):Quaternionf {
    return Quaternionf(AxisAngle4f(degrees.toRadians(), this))
}

fun dvec3.getDegreesQuaternion(degrees:double):Quaterniond {
    return Quaterniond(AxisAngle4d(degrees.toRadians(), this))
}