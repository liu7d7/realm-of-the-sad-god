package me.ethius.shared

import me.ethius.client.Client
import me.ethius.server.Server
import kotlin.math.roundToInt

abstract class Tickable(autoInit:bool = false, var priority:int = 3) {

    var ticksExisted = -1
    open val shouldTick:bool
        get() = true

    open fun clientTick() {

    }

    open fun serverTick() {

    }

    open fun init() {
        when (Side.currentSide) {
            Side.client -> {
                Client.ticker.add(this)
            }
            Side.server -> {
                Server.ticker.add(this)
            }
        }
        this.ticksExisted = 0
    }

    open fun release() {
        when (Side.currentSide) {
            Side.client -> {
                Client.ticker.rem(this)
            }
            Side.server -> {
                Server.ticker.rem(this)
            }
        }
        this.ticksExisted = -1
    }

    fun delayNumSeconds(num:double):bool {
        return ticksExisted % (num / (tickTime / 1000f)).roundToInt() == 0
    }

    init {
        if (autoInit) {
            this.init()
        }
    }

}