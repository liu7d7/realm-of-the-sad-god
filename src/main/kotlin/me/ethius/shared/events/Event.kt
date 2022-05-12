package me.ethius.shared.events

import me.ethius.shared.bool

open class Event(open var era:EventEra = EventEra.pre) {

    var isCancelled = false

    fun cancel():Event {
        isCancelled = true
        return this
    }

    fun restore():Event {
        isCancelled = false
        return this
    }

    fun <A:Event> era(era:EventEra):A {
        this.era = era
        return this as A
    }

    fun isPre():bool = era == EventEra.pre

}