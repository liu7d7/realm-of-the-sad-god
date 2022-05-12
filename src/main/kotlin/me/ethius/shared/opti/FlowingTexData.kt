package me.ethius.shared.opti

import me.ethius.client.main_tex
import me.ethius.shared.*

class FlowingTexData(
    val data:TexData,
    override val width:double,
    override val height:double,
):
    TexData(0f, 0f, 0f, 0f) {
    override var u:double = 0.0
    override var v:double = 0.0

    override fun texData(seed:int, wind:float):TexData {
        this.u = data.u + (data.width - width) * (wind / 23f + 0.5f)
        this.v = data.v + (data.height - height) * (wind / 23f + 0.5f)
        return this
    }

    override fun getAvgColor() {
        if (this.type == Type.tile) {
            val start = (main_tex.width * data.v + data.u).toInt()
            val end = (main_tex.width * (data.v + data.height) + data.u + data.width).toInt()
            for (i in start * 4..end * 4 step 4) {
                val red = main_tex.data!![i]
                val grn = main_tex.data!![i + 1]
                val blu = main_tex.data!![i + 2]
                val alp = main_tex.data!![i + 3]
                avgColor = interpolateColor(avgColor,
                                            (colorToRGB((red.toUByte().toInt() * (alp.toUByte()
                                                                                      .toDouble() / 255.0)).toInt(),
                                                        (grn.toUByte().toInt() * (alp.toUByte()
                                                                                      .toDouble() / 255.0)).toInt(),
                                                        (blu.toUByte().toInt() * (alp.toUByte()
                                                                                      .toDouble() / 255.0)).toInt(),
                                                        255)).toLong(),
                                            0.5f)
            }
        }
    }
}