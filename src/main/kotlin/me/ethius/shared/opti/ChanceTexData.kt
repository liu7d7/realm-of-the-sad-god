package me.ethius.shared.opti

import me.ethius.shared.float
import me.ethius.shared.frand
import me.ethius.shared.int
import me.ethius.shared.interpolateColor
import me.ethius.shared.maths.WeightedCollection

class ChanceTexData(private val collection:WeightedCollection<TexData>):TexData(0, 0, 0, 0) {

    override fun texData(seed:int, wind:float):TexData {
        return collection.next(frand(seed).toDouble()) ?: empty
    }

    override fun getAvgColor() {
        var color = -1L
        for (i in collection.map.values) {
            i.getAvgColor()
            if (color == -1L) {
                color = i.avgColor
                continue
            }
            color = interpolateColor(color, i.avgColor, 0.5f)
        }
    }

}