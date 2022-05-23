package me.ethius.client

import me.ethius.shared.calcAngle
import me.ethius.shared.double
import me.ethius.shared.wrapDegrees
import kotlin.math.hypot

fun calcAngleMPToMouse():double {
    return wrapDegrees(calcAngle(-((Client.cameraPos.y + Client.player.lerpedY) - Client.mouse.y),
                                 -((Client.cameraPos.x + Client.player.lerpedX) - Client.mouse.x)) - Client.player.r)
}

fun mouseDstToPlayer():double {
    return hypot(((Client.cameraPos.x + Client.player.lerpedX) - Client.mouse.x),
                 ((Client.cameraPos.y + Client.player.lerpedY) - Client.mouse.y))
}