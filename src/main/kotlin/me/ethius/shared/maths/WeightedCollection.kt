package me.ethius.shared.maths

import me.ethius.shared.bool
import me.ethius.shared.double
import java.util.*

open class WeightedCollection<E> constructor(private val random:Random = Random()) {

    val map:NavigableMap<double, E> = TreeMap()
    private var total = 0.0

    open fun add(weight:double, result:E):WeightedCollection<E> {
        if (weight <= 0) return this
        total += weight
        map[total] = result
        return this
    }

    open fun addAll(e:MutableMap<double, E>) {
        for (entry in e) {
            add(entry.key, entry.value)
        }
    }

    open fun isEmpty():bool {
        return map.isEmpty()
    }

    operator fun next():E? {
        val value = random.nextDouble() * total
        val h = map.higherEntry(value) ?: return null
        return h.value
    }

    open fun clear() {
        map.clear()
        total = 0.0
    }

}