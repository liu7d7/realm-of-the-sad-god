package me.ethius.client.renderer.font

import me.ethius.shared.double

data class CharData(
    val x0:double,
    val y0:double,
    val x1:double,
    val y1:double,
    val u0:double,
    val v0:double,
    val u1:double,
    val v1:double,
    val xAdvance:double,
)