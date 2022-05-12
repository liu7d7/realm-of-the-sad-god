package me.ethius.shared.events.def

import me.ethius.shared.events.Event
import me.ethius.shared.float

data class WindowResizedEvent(val width:float, val height:float):Event()