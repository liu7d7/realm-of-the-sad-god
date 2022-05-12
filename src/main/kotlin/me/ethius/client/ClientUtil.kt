package me.ethius.client

import me.ethius.shared.calcAngle
import me.ethius.shared.double
import me.ethius.shared.wrapDegrees
import kotlin.math.pow
import kotlin.math.sqrt

fun calcAngleMPToMouse():double {
    return wrapDegrees(calcAngle(-((Client.cameraPos.y + Client.player.lerpedY) - Client.mouse.y),
                                 -((Client.cameraPos.x + Client.player.lerpedX) - Client.mouse.x)) - Client.player.r)
}

fun mouseDstToPlayer():double {
    return sqrt(((Client.cameraPos.x + Client.player.lerpedX) - Client.mouse.x).pow(2) +
                ((Client.cameraPos.y + Client.player.lerpedY) - Client.mouse.y).pow(2))
}