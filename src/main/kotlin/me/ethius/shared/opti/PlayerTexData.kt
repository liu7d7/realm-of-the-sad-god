package me.ethius.shared.opti

import me.ethius.shared.float
import me.ethius.shared.int
import me.ethius.shared.string

open class PlayerTexData(
    tl_x:int,
    tl_y:int,
    val shootingWidth:int = 11,
) {

    var id:string = "unknown"

    constructor(tl_x:float, tl_y:float):this(tl_x.toInt(), tl_y.toInt())

    // UP //
    open val up = TexData(tl_x, tl_y, 8, 8)
    open val up_w1 = TexData(tl_x, tl_y + 8, 8, 8)
    open val up_w2 = TexData(tl_x, tl_y + 16, 8, 8)
    open val up_s1 = TexData(tl_x + 40, tl_y, 8, 8)
    open val up_s2 = TexData(tl_x + 40, tl_y + 8, 8, 8)

    // RIGHT //
    open val right = TexData(tl_x + 8, tl_y, 8, 8)
    open val right_w1 = TexData(tl_x + 8, tl_y + 8, 8, 8)
    open val right_w2 = TexData(tl_x + 8, tl_y + 16, 8, 8)
    open val right_s1 = TexData(tl_x + 48, tl_y, shootingWidth, 8)
    open val right_s2 = TexData(tl_x + 48, tl_y + 8, shootingWidth, 8)

    // DOWN //
    open val down = TexData(tl_x + 16, tl_y, 8, 8)
    open val down_w1 = TexData(tl_x + 16, tl_y + 8, 8, 8)
    open val down_w2 = TexData(tl_x + 16, tl_y + 16, 8, 8)
    open val down_s1 = TexData(tl_x + 32, tl_y, 8, 8)
    open val down_s2 = TexData(tl_x + 32, tl_y + 8, 8, 8)

    // LEFT //
    open val left = TexData(tl_x + 24, tl_y, 8, 8)
    open val left_w1 = TexData(tl_x + 24, tl_y + 8, 8, 8)
    open val left_w2 = TexData(tl_x + 24, tl_y + 16, 8, 8)
    open val left_s1 = TexData(tl_x + 48 + shootingWidth, tl_y, shootingWidth, 8)
    open val left_s2 = TexData(tl_x + 48 + shootingWidth, tl_y + 8, shootingWidth, 8)

}