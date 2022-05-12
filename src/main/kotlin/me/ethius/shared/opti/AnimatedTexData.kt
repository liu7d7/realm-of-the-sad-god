package me.ethius.shared.opti

import me.ethius.shared.*
import org.apache.commons.lang3.RandomUtils
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong

data class AnimatedTexData(
    val u1:double,
    val v1:double,
    val w1:double,
    val h1:double,
    val time:long,
    val facing:me.ethius.client.renderer.Axis = me.ethius.client.renderer.Axis.horizontal,
):TexData(0f, 0f, 0f, 0f) {
    private val bl1 = facing == me.ethius.client.renderer.Axis.horizontal
    private val steps =
        if (bl1) {
            (w1 / h1).toInt()
        } else {
            (h1 / w1).toInt()
        }
    private val step:long
        get() = (current_time % time) / (time / steps)
    private val rand = RandomUtils.nextInt(0, 600)
    private val current_time:long
        get() = measuringTimeMS().roundToLong() + rand
    private val arrTexData = Array(steps) {
        val widthHeight = min(w1, h1)
        val mx = max(w1, h1)
        val addH = if (bl1) (it * widthHeight).coerceAtMost(mx - widthHeight) else 0.0
        val addV = if (!bl1) (it * widthHeight).coerceAtMost(mx - widthHeight) else 0.0
        TexData(u1 + addH, v1 + addV, widthHeight, widthHeight, 0.0, 0.0, "", Type.other)
    }

    override fun texData(seed:int, wind:float):TexData {
        return arrTexData[step.toInt().coerceIn(arrTexData.indices)]
    }
}