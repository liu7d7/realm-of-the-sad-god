package me.ethius.shared.rotsg.data

import me.ethius.shared.char
import me.ethius.shared.float
import me.ethius.shared.long
import me.ethius.shared.string

enum class Formatting(
    val chr:char,
    val color:(long) -> long,
) {
    black('0', { 0xff000000 }),
    dark_blue('1', { 0xff0000aa }),
    dark_green('2', { 0xff00aa00 }),
    dark_aqua('3', { 0xff00aaaa }),
    dark_red('4', { 0xffaa0000 }),
    dark_purple('5', { 0xffaa00aa }),
    gold('6', { 0xffffaa00 }),
    gray('7', { 0xffaaaaaa }),
    dark_gray('8', { 0xff555555 }),
    blue('9', { 0xff5555ff }),
    green('a', { 0xff55ff55 }),
    aqua('b', { 0xff55ffff }),
    red('c', { 0xffff5555 }),
    light_purple('d', { 0xffff55ff }),
    yellow('e', { 0xffffff55 }),
    white('f', { 0xffffffff }),
    reset('r', { it });

    override fun toString():string {
        return "§${this.chr}"
    }

    companion object {
        fun byChar(chr:char):Formatting? {
            return values().find { it.chr == chr }
        }

        fun getColorFor0Through1(value:float, mid:float):Formatting {
            val firstQuartile = mid / 2f
            val thirdQuartile = if (mid / 2f * 3f > 1f) 1 - mid / 2f else mid / 2f * 3f
            return when {
                value < firstQuartile -> red
                value < mid -> yellow
                value < thirdQuartile -> green
                else -> dark_green
            }
        }

    }
}