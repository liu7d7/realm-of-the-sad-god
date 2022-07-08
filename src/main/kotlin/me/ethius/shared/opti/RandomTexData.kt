package me.ethius.shared.opti

import me.ethius.shared.float
import me.ethius.shared.frand
import me.ethius.shared.int
import me.ethius.shared.interpolateColor

class RandomTexData(private vararg val datas:TexData):TexData(0f, 0f, 0f, 0f) {
    override val width = datas.first().width
    override val height = datas.first().height

    override fun texData(seed:int, wind:float):TexData {
        return datas[(frand(seed) * (datas.size - 1)).toInt().coerceAtLeast(0).coerceAtMost(datas.size - 1)]
    }

    override fun getAvgColor() {
        for (data in datas) {
            data.getAvgColor()
            avgColor = interpolateColor(avgColor, data.avgColor, 0.5f)
        }
    }
}