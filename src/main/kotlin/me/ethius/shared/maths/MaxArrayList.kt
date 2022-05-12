package me.ethius.shared.maths

import me.ethius.shared.bool
import me.ethius.shared.int

class MaxArrayList<T>(private val maxSize:int):ArrayList<T>() {

    override fun add(element:T):bool {
        while (size >= maxSize) {
            this.removeAt(0)
        }
        return super.add(element)
    }

}