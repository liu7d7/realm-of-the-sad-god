package me.ethius.shared.ext

import com.moandjiezana.toml.Toml
import me.ethius.shared.*
import kotlin.math.absoluteValue
import kotlin.math.hypot
import kotlin.math.sqrt

val ZERO2 = fvec2(0f, 0f)
val ZERO2i = ivec2(0, 0)
val POSITIVE_Z = dvec3(0.0, 0.0, 1.0)
val POSITIVE_X = dvec3(1.0, 0.0, 0.0)
val NEGATIVE_X = dvec3(-1.0, 0.0, 0.0)
val NEGATIVE_Z = dvec3(0.0, 0.0, -1.0)
val POSITIVE_Y = dvec3(0.0, 1.0, 0.0)
val ZERO3 = fvec3(0f, 0f, 0f)
val ZERO3d = dvec3(0.0, 0.0, 0.0)

var fvec3.r:float
    get() = this.z
    set(value) {
        this.z = value
    }

var dvec3.r:double
    get() = this.z
    set(value) {
        this.z = value
    }

fun dvec2.toivec2():ivec2 {
    return ivec2(kotlin.math.floor(this.x).toInt(), kotlin.math.floor(this.y).toInt())
}

fun dvec3.isZero():bool {
    return this.x.absoluteValue <= 0.1 && this.y.absoluteValue <= 0.1 && this.z.absoluteValue <= 0.1
}

fun dvec2.isZero():bool {
    return this.x.absoluteValue <= 0.1 && this.y.absoluteValue <= 0.1
}

fun fvec3.distance(other:fvec2):float {
    return sqrt(this.distanceSquared(other.x, other.y))
}

fun fvec3.distanceSquared(other:fvec2):float {
    return this.distanceSquared(other.x, other.y)
}

fun dvec3.distanceSquared(other:dvec2):double {
    return this.distanceSquared(other.x, other.y)
}

fun fvec3.distanceSquared(x:float, y:float):float {
    return (this.x - x) * (this.x - x) + (this.y - y) * (this.y - y)
}

fun dvec3.distanceSquared(x:double, y:double):double {
    return (this.x - x) * (this.x - x) + (this.y - y) * (this.y - y)
}

fun fvec3.copy():fvec3 {
    return fvec3(this.x, this.y, this.z)
}

fun dvec3.distance2dSquared(other:dvec3):double {
    val dx = this.x - other.x
    val dy = this.y - other.y
    return dx * dx + dy * dy
}

fun dvec3.distance2d(other:dvec3):double {
    val dx = this.x - other.x
    val dy = this.y - other.y
    return hypot(dx, dy)
}

fun dvec3.distance2d(other:dvec2):double {
    val dx = this.x - other.x
    val dy = this.y - other.y
    return hypot(dx, dy)
}

val int.tickToMs:long
    get() {
        return this * 20L
    }

fun fvec3.distance2dSquared(other:fvec3):float {
    val dx = this.x - other.x
    val dy = this.y - other.y
    return dx * dx + dy * dy
}

operator fun dvec3.minus(other:dvec3):dvec3 {
    return this.copy().sub(other)
}

inline fun <T, K> Iterable<T>.distinctBy(selector:(T) -> K, ifGone:(T) -> void):List<T> {
    val set = HashSet<K>()
    val list = ArrayList<T>()
    for (e in this) {
        val key = selector(e)
        if (set.add(key))
            list.add(e)
        else
            ifGone(e)
    }
    return list
}

fun fvec3.toVec2f():fvec2 {
    return fvec2(this.x, this.y)
}

fun dvec3.todvec2():dvec2 {
    return dvec2(this.x, this.y)
}

fun ivec2.todvec2():dvec2 {
    return dvec2(this.x.toDouble(), this.y.toDouble())
}

fun fvec3.distance(x:float, y:float):float {
    return hypot(this.x - x, this.y - y)
}

fun Toml.getInt(name:string):int = this.getLong(name).toInt()

fun Toml.getInt(name:string, defVal:int) = this.getLong(name, defVal.toLong()).toInt()

fun Toml.getFloat(name:string):float = this.getDouble(name).toFloat()

fun Toml.getFloat(name:string, defVal:float) = this.getDouble(name, defVal.toDouble()).toFloat()