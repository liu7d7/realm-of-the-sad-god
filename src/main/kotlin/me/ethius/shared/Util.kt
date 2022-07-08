package me.ethius.shared

import com.moandjiezana.toml.Toml
import com.moandjiezana.toml.TomlWriter
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import me.ethius.server.rotsg.world.biome.ABiome
import me.ethius.shared.rotsg.entity.AEntity
import me.ethius.shared.rotsg.entity.StatEntity
import me.ethius.shared.rotsg.tile.Tile
import me.ethius.shared.rotsg.world.IWorld
import org.apache.commons.lang3.RandomUtils
import org.lwjgl.glfw.GLFW.glfwGetTime
import org.lwjgl.system.MemoryUtil
import java.io.*
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.util.*
import kotlin.math.*

const val PI = Math.PI
const val PI2 = PI * 2f
const val PId2 = PI / 2f
const val DegToRadMult = (PI / 180f).toFloat()
const val RadToDegMult = (180f / PI).toFloat()
const val Tau = PI * 2f
const val InvPi = 1f / PI
const val InvTau = InvPi * 0.5f

var time = 0f
var timeGetter = { time }
var start = System.currentTimeMillis().toDouble()

val lambda_v = { }

private val randomFloats = fv(500) { RandomUtils.nextFloat(0f, 5f) * 0.2f }

fun calcAngle(v:double, h:double):double {
    return wrapDegrees(Math.toDegrees(fastAtan2(v, h)))
}

fun calcAngle(eye:AEntity, target:AEntity, lead:bool = false, leadAmt:double = 4.0):double {
    val dx = (target.x + (if (lead) (target.velocity.x * (if (target is StatEntity) target.tps.toDouble() else 1.0) * leadAmt) else 0.0)) - eye.x
    val dy = (target.y + (if (lead) (target.velocity.y * (if (target is StatEntity) target.tps.toDouble() else 1.0) * leadAmt) else 0.0)) - eye.y
    return calcAngle(dy, dx)
}

fun fastInverseSqrt(x:double):Double {
    var x1 = x
    val d = 0.5 * x1
    var l = java.lang.Double.doubleToRawLongBits(x1)
    l = 6910469410427058090L - (l shr 1)
    x1 = java.lang.Double.longBitsToDouble(l)
    x1 *= 1.5 - d * x1 * x1
    return x1
}

fun Double.toRadians():double {
    return Math.toRadians(this)
}

fun Float.toDegrees():float {
    return this * RadToDegMult
}

fun Double.toDegrees():double {
    return Math.toDegrees(this)
}

fun updateTime() {
    ifclient {
        time = (glfwGetTime() * 1000.0).toFloat()
    }
    ifserver {
        time = (System.currentTimeMillis() - start).toFloat()
    }
}

fun measuringTimeMS():float {
    return timeGetter()
}

fun fract(num:double):double {
    return num - floor(num)
}

fun frand(seed:int):float {
    val vec2 = dvec2(randomFloats[abs(seed % 500)].toDouble(), randomFloats[abs(seed + 200) % 500].toDouble())
    return fract(sin(vec2.dot(dvec2(12.9898, 78.233))) * 43758.5453).toFloat()
}

fun wrapDegrees(degrees:double):double {
    var f = degrees % 360.0
    if (f >= 180.0) {
        f -= 360.0
    }
    if (f < -180.0) {
        f += 360.0
    }
    return f
}

fun wrapDegrees(degrees:float):float {
    var f = degrees % 360.0f
    if (f >= 180.0f) {
        f -= 360.0f
    }
    if (f < -180.0f) {
        f += 360.0f
    }
    return f
}

fun getKern(char:int):double {
    return when (char) {
        in 33..64 -> -0.465
        else -> -1.0
    }
}

fun colorToRGB(red:int, green:int, blue:int, alpha:int):int {
    var rgb = alpha shl 24
    rgb = rgb or (red shl 16)
    rgb = rgb or (green shl 8)
    rgb = rgb or blue
    return rgb
}

fun interpolateColor(rgba1:long, rgba2:long, percent:float):long {
    val r1 = rgba1 and 0xFF
    val g1 = rgba1 shr 8 and 0xFF
    val b1 = rgba1 shr 16 and 0xFF
    val a1 = rgba1 shr 24 and 0xFF
    val r2 = rgba2 and 0xFF
    val g2 = rgba2 shr 8 and 0xFF
    val b2 = rgba2 shr 16 and 0xFF
    val a2 = rgba2 shr 24 and 0xFF
    val r = (if (r1 < r2) r1 + (r2 - r1) * percent else r2 + (r1 - r2) * percent).toLong()
    val g = (if (g1 < g2) g1 + (g2 - g1) * percent else g2 + (g1 - g2) * percent).toLong()
    val b = (if (b1 < b2) b1 + (b2 - b1) * percent else b2 + (b1 - b2) * percent).toLong()
    val a = (if (a1 < a2) a1 + (a2 - a1) * percent else a2 + (a1 - a2) * percent).toLong()
    return r or (g shl 8) or (b shl 16) or (a shl 24)
}

fun readBytes(`in`:InputStream):ByteArray {
    try {
        val out = ByteArrayOutputStream()
        val buffer = ByteArray(256)
        var read:int
        while (`in`.read(buffer).also { read = it } > 0) out.write(buffer, 0, read)
        `in`.close()
        return out.toByteArray()
    } catch (e:IOException) {
        e.printStackTrace()
    }
    return ByteArray(0)
}

fun withAlpha(color:long, alpha:long):long {
    return (alpha shl 24) or (color and 0xffffff.toLong())
}

inline fun after(ms:long, crossinline action:() -> void) {
    Thread {
        Thread.sleep(ms)
        action()
    }.start()
}

enum class OS {
    windows, mac, linux, other
}

private lateinit var _os: OS

fun getOS(): OS {
    if (!::_os.isInitialized) {
        val os = System.getProperty("os.name", "generic").lowercase(Locale.ENGLISH)
        if (os.indexOf("mac") >= 0 || os.indexOf("darwin") >= 0) {
            _os = OS.mac
        } else if (os.indexOf("win") >= 0) {
            _os = OS.windows
        } else if (os.indexOf("nux") >= 0) {
            _os = OS.linux
        } else {
            _os = OS.other
        }
    }
    return _os
}

private lateinit var _ip: string

fun getLocalIp(): string {
    if (!::_ip.isInitialized) {
        when (getOS()) {
            OS.windows -> {
                DatagramSocket().use { socket ->
                    socket.connect(InetAddress.getByName("8.8.8.8"), 10002)
                    _ip = socket.localAddress.hostAddress
                }
            }
            else -> {
                val socket = Socket()
                socket.connect(InetSocketAddress("google.com", 80))
                _ip = socket.localAddress.toString()
            }
        }
    }
    return _ip
}

fun withAlpha(color:long, alpha:float):long {
    return ((alpha * 255.0).toLong() shl 24) or (color and 0xffffff.toLong())
}

fun sinD(degrees:double):double {
    return sin(degrees.toRadians())
}

fun sinD(degrees:float):float {
    return sin(degrees.toRadians())
}

fun sin(radians:float):float {
    return sin(radians.toDouble()).toFloat()
}

fun sin(radians:double):double {
    var x = radians - floor((radians + PI) * InvTau) * Tau

    x *= 4 * InvPi * (Math.fma(-x.absoluteValue, InvPi, 1.0))
    return x * Math.fma(0.224, x.absoluteValue, 0.776)
}

fun cosD(degrees:double):double {
    return cos(degrees.toRadians())
}

fun cosD(degrees:float):float {
    return cos(degrees.toRadians())
}

fun cos(radians:double):double {
    return sin(radians + PI * 0.5f)
}

fun cos(radians:float):float {
    return cos(radians.toDouble()).toFloat()
}

fun fastAtan2(y:double, x:double):double {
    var t3 = x.absoluteValue
    var t1 = y.absoluteValue
    var t0 = max(t3, t1)
    t1 = min(t3, t1)
    t3 = 1f / t0
    t3 *= t1

    val t4 = t3 * t3
    t0 = -0.013480470
    t0 = Math.fma(t0, t4, 0.057477314)
    t0 = Math.fma(t0, t4, -0.121239071)
    t0 = Math.fma(t0, t4, 0.195635925)
    t0 = Math.fma(t0, t4, -0.332994597)
    t0 = Math.fma(t0, t4, 0.999995630)
    t3 *= t0

    if (y.absoluteValue > x.absoluteValue) {
        t3 = Math.fma(PI, 0.5, -t3)
    }
    if (x < 0) {
        t3 = PI - t3
    }

    if (y < 0) {
        return -t3
    }
    return t3
}

fun readResource(inputStream:InputStream):ByteBuffer {
    var byteBuffer2:ByteBuffer
    if (inputStream is FileInputStream) {
        val fileChannel = inputStream.channel
        byteBuffer2 = MemoryUtil.memAlloc(fileChannel.size().toInt() + 1)
        while (true) {
            if (fileChannel.read(byteBuffer2) != -1) {
                continue
            }
        }
    } else {
        byteBuffer2 = MemoryUtil.memAlloc(8192)
        val readableByteChannel = Channels.newChannel(inputStream)
        while (readableByteChannel.read(byteBuffer2) != -1) {
            if (byteBuffer2.remaining() == 0) {
                byteBuffer2 = MemoryUtil.memRealloc(byteBuffer2, byteBuffer2.capacity() * 2)
            }
        }
    }
    return byteBuffer2
}

fun isWithin(x:double, y:double, vec1:ivec2, vec2:ivec2):bool {
    return x >= min(vec1.x, vec2.x) && x <= max(vec1.x + 1, vec2.x + 1) && y >= min(vec1.y,
                                                                                    vec2.y) && y <= max(vec1.y + 1,
                                                                                                        vec2.y + 1)
}

fun lerp(start:double, end:double, delta:float):double {
    return start + (end - start) * delta
}

fun lerp(start:double, end:double, delta:double):double {
    return start + (end - start) * delta
}

fun lerp(start:float, end:float, delta:float):float {
    return start + (end - start) * delta
}

fun darknessOf(color1:long):float {
    val c1a = (color1 shr 24) and 0xff
    val c1r = (color1 shr 16) and 0xff
    val c1g = (color1 shr 8) and 0xff
    val c1b = color1 and 0xff
    return (c1r + c1g + c1b).toFloat() / 3f * c1a.toFloat() / 255f
}

fun isColor1Darker(color1:long, color2:long):bool {
    val c1a = (color1 shr 24) and 0xff
    val c1r = (color1 shr 16) and 0xff
    val c1g = (color1 shr 8) and 0xff
    val c1b = color1 and 0xff
    val c2a = (color2 shr 24) and 0xff
    val c2r = (color2 shr 16) and 0xff
    val c2g = (color2 shr 8) and 0xff
    val c2b = color2 and 0xff
    val c1 = (c1r + c1g + c1b).toFloat() / 3f * c1a.toFloat() / 255f
    val c2 = (c2r + c2g + c2b).toFloat() / 3f * c2a.toFloat() / 255f
    return c1 < c2
}

fun getCircle(loc:ivec2, r:int, hollow:bool):List<ivec2> {
    val circleTiles:MutableList<ivec2> = ArrayList()
    val cx = loc.x + 1
    val cy = loc.y
    var x = cx - r
    while (x <= cx + r) {
        var y = cy - r
        while (y < cy + r) {
            val dist = ((cx - x) * (cx - x) + (cy - y) * (cy - y))
            if (dist < r * r && !(hollow && dist < (r - 1) * (r - 1))) {
                circleTiles.add(ivec2(x, y))
            }
            y++
        }
        x++
    }
    return circleTiles
}

fun fillCircle(world:IWorld, ABiome:ABiome, tile:(ivec2) -> Tile, loc:ivec2, r:int, hollow:bool) {
    val cx = loc.x + 1
    val cy = loc.y
    var x = cx - r
    while (x <= cx + r) {
        var y = cy - r
        while (y < cy + r) {
            val dist = ((cx - x) * (cx - x) + (cy - y) * (cy - y))
            if (dist < r * r && !(hollow && dist < (r - 1) * (r - 1))) {
                val tile = tile(ivec2(x, y))
                if (world.addTile(tile, false)) {
                    ABiome.addTile(tile, false)
                }
            }
            y++
        }
        x++
    }
}

fun clamp(value:int, min:int, max:int):int {
    return if (value < min)
        min
    else
        if (value > max) max else value
}

fun clamp(value:long, min:long, max:long):long {
    return if (value < min)
        min
    else
        if (value > max) max else value
}

fun clamp(value:float, min:float, max:float):float {
    return if (value < min)
        min
    else
        if (value > max) max else value
}

fun clamp(value:double, min:double, max:double):double {
    return if (value < min)
        min
    else
        if (value > max) max else value
}

fun getRandomInRange(min:float, max:float):float {
    return min + (max - min) * RandomUtils.nextFloat()
}

fun safeRange(min:Int, max:Int):IntRange = min(min, max)..max(min, max)

val pathToToml = Object2ObjectOpenHashMap<string, Toml>()

fun Toml.readCached(path:string):Toml {
    return if (pathToToml.containsKey(path)) {
        pathToToml[path]!!
    } else {
        val toml = read(this.javaClass.getResourceAsStream(path))
        pathToToml[path] = toml
        toml
    }
}

fun Toml.readCached(path:File):Toml {
    val ts = path.toString()
    return if (pathToToml.containsKey(ts)) {
        pathToToml[ts]!!
    } else {
        val toml = read(path)
        pathToToml[ts] = toml
        toml
    }
}

val toml = TomlWriter()

fun Float.toRadians():float {
    return this * DegToRadMult
}

fun ivec2.copy():ivec2 {
    return ivec2(this.x, this.y)
}

fun fvec2.copy():fvec2 {
    return fvec2(this.x, this.y)
}

fun dvec3.copy():dvec3 {
    return dvec3(this.x, this.y, this.z)
}

fun dvec2.copy():dvec2 {
    return dvec2(this.x, this.y)
}

inline fun ifserver(block:() -> Unit) {
    if (Side._server) {
        block()
    }
}

inline fun ifclient(also:bool = true, block:() -> Unit) {
    if (Side._client && also) {
        block()
    }
}