package me.ethius.shared.opti

import me.ethius.shared.float
import me.ethius.shared.frand
import me.ethius.shared.int
import me.ethius.shared.maths.WeightedCollection

class ChanceTexData(private val collection:WeightedCollection<TexData>):TexData(0, 0, 0, 0) {

    override fun texData(seed:int, wind:float):TexData {
        return collection.next(frand(seed).toDouble()) ?: empty
    }

}