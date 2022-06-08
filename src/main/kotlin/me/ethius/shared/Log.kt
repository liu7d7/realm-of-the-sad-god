package me.ethius.shared

object Log {

    // all 16 foreground colors
    const val fg_black = "\u001B[30m"
    const val fg_red = "\u001B[31m"
    const val fg_green = "\u001B[32m"
    const val fg_yellow = "\u001B[33m"
    const val fg_blue = "\u001B[34m"
    const val fg_magenta = "\u001B[35m"
    const val fg_cyan = "\u001B[36m"
    const val fg_white = "\u001B[37m"
    const val fg_gray = "\u001B[90m"
    const val fg_light_red = "\u001B[91m"
    const val fg_light_green = "\u001B[92m"
    const val fg_light_yellow = "\u001B[93m"
    const val fg_light_blue = "\u001B[94m"
    const val fg_light_magenta = "\u001B[95m"
    const val fg_light_cyan = "\u001B[96m"
    const val fg_light_white = "\u001B[97m"

    // all 16 background colors
    const val bg_black = "\u001B[40m"
    const val bg_red = "\u001B[41m"
    const val bg_green = "\u001B[42m"
    const val bg_yellow = "\u001B[43m"
    const val bg_blue = "\u001B[44m"
    const val bg_magenta = "\u001B[45m"
    const val bg_cyan = "\u001B[46m"
    const val bg_white = "\u001B[47m"
    const val bg_gray = "\u001B[100m"
    const val bg_light_red = "\u001B[101m"
    const val bg_light_green = "\u001B[102m"
    const val bg_light_yellow = "\u001B[103m"
    const val bg_light_blue = "\u001B[104m"
    const val bg_light_magenta = "\u001B[105m"
    const val bg_light_cyan = "\u001B[106m"
    const val bg_light_white = "\u001B[107m"

    // all 8 styles
    const val style_bold = "\u001B[1m"
    const val style_dim = "\u001B[2m"
    const val style_italic = "\u001B[3m"
    const val style_underline = "\u001B[4m"
    const val style_blink = "\u001B[5m"
    const val style_reverse = "\u001B[7m"
    const val style_hidden = "\u001B[8m"
    const val reset = "\u001B[0m"

    const val endl = "\n"

    val side:string
        get() {
            return if (Side._client) "_LOCAL_" else "_SERVER_"
        }

    private var sb = StringBuffer()
    var firstRun = true

    object info {
        operator fun plus(message:Any?):info {
            if (firstRun) {
                sb.append("[$side/${fg_cyan}_INFO_$reset] ")
                firstRun = false
            }
            default(message)
            return this
        }
    }

    object warn {
        operator fun plus(message:Any?):warn {
            if (firstRun) {
                sb.append("[$side/${fg_yellow}_WARN_$reset] ")
                firstRun = false
            }
            default(message)
            return this
        }
    }

    object error {
        operator fun plus(message:Any?):error {
            if (firstRun) {
                sb.append("[$side/${fg_red}_ERROR_$reset] ")
                firstRun = false
            }
            default(message)
            return this
        }
    }

    fun default(message:Any?) {
        when (message) {
            endl -> {
                println(sb.append(reset))
                sb = StringBuffer()
                firstRun = true
            }
            else -> {
                sb.append(message)
            }
        }
    }

}