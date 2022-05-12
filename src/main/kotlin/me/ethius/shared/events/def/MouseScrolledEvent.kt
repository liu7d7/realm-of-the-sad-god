package me.ethius.shared.events.def

import me.ethius.shared.events.Event
import me.ethius.shared.float

data class MouseScrolledEvent(
    var x:float,
    var y:float,
    val modifier:float,
):Event()