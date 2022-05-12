package me.ethius.client.rotsg.gui

import me.ethius.client.Client
import me.ethius.shared.float
import me.ethius.shared.int
import me.ethius.shared.measuringTimeMS
import me.ethius.shared.string

data class ChatHudLine(
    val contents:string,
    var id:int,
    val timeIn:float = measuringTimeMS(),
    val timeOut:float = timeIn + 7500f,
) {
    val lines = Client.font.wrapWords(contents, 350.0)
    val height = lines.size * 22.0
    var yModulate = 0.0
    var xModulate = 0.0
    val length = timeOut - timeIn

    init {
        lines.reverse()
    }
}
