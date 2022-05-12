package me.ethius.shared.maths

import me.ethius.shared.int

enum class Facing {

    up, right, down, left;

    companion object {
        val horizontalValues = arrayOf(right, left)
        val verticalValues = arrayOf(up, down)

        operator fun get(id:int):Facing {
            return values()[id]
        }
    }

}