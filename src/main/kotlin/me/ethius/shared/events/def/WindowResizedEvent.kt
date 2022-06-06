package me.ethius.shared.events.def

import me.ethius.shared.double
import me.ethius.shared.events.Event

data class WindowResizedEvent(val width:double, val height:double):Event()