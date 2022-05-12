package me.ethius.shared.events.def

import me.ethius.shared.events.Event
import me.ethius.shared.float
import me.ethius.shared.int

data class MouseClickedEvent(
    var x:float,
    var y:float,
    val button:int,
    val action:int,
    val mods:int,
):Event()
