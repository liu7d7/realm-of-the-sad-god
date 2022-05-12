package me.ethius.shared.maths

import me.ethius.shared.bool
import me.ethius.shared.double

class BoundingCircle(
    var cx:double,
    var cy:double,
    var radius:double,
) {

    constructor():this(0.0, 0.0, 0.0)

    fun collidesWith(box:BoundingCircle):bool {
        if (box.radius <= 0) {
            return false
        }
        val dst = (box.cx - cx) * (box.cx - cx) + (box.cy - cy) * (box.cy - cy)
        return dst <= (radius + box.radius) * (radius + box.radius)
    }

}