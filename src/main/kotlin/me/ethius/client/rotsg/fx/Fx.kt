package me.ethius.client.rotsg.fx

import me.ethius.shared.*
import me.ethius.shared.ext.ZERO2
import me.ethius.shared.opti.TexData
import org.apache.commons.lang3.RandomUtils
import kotlin.math.min
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.jvmErasure

data class Fx(
    var x:double,
    var y:double,
    var velocity:fvec2,
    var texData:TexData,
    var u:double = -1.0,
    var v:double = -1.0,
) {
    val start = measuringTimeMS()
    var pX = x
    var pY = y
    val width = min(1.0, texData.width)
    val height = min(1.0, texData.height)
    val rTexData:TexData
    var id:string = "unnamed${hashCode()}"
    var zBase = 50.0
    var modulateZ:bool = true

    fun copy(x:double, y:double, velocity:fvec2):Fx {
        return Fx(x, y, velocity, texData).also { it.id = this.id }
    }

    init {
        if (u == -1.0) {
            u = RandomUtils.nextDouble(texData.u, texData.u + texData.width - width)
        }
        if (v == -1.0) {
            v = RandomUtils.nextDouble(texData.v, texData.v + texData.height - height)
        }
        rTexData = TexData(u, v, width, height, 0.0, 0.0)
    }

    companion object {
        val values = mutableListOf<Fx>()

        val purple = Fx(0.0, 0.0, ZERO2, TexData.atk_rock)

        operator fun get(fx:string):Fx? {
            return values.find { it.id == fx }
        }

        fun init() {
            for (i in this::class.declaredMemberProperties) {
                if (i.returnType.jvmErasure == Fx::class) {
                    values.add(((i as KProperty1<Any, *>).get(this) as Fx).also { it.id = i.name })
                }
            }
        }
    }

}