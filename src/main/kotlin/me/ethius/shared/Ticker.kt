package me.ethius.shared

import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Ticker {

    val tickables = CopyOnWriteArrayList<Tickable>()

    var tickDelta = 0f
    var lastFrameDuration = 0f
    private var prevTimeMillis = 0f
    val tickTime = 20f
    var tps = 0L
    private var tpsi = 0L
    private var lastMetricSec = 0f
    private var thread_exec:ExecutorService = Executors.newWorkStealingPool(2)

    fun beginRenderTick():int {
        updateTime()
        val timeMillis = measuringTimeMS()
        lastFrameDuration = (timeMillis - prevTimeMillis) / tickTime
        prevTimeMillis = timeMillis
        tickDelta += lastFrameDuration
        val i = tickDelta.toInt()
        tickDelta -= i.toFloat()
        return i
    }

    fun submitTask(sync:Any = this, task:() -> void) {
        thread_exec.execute {
            synchronized(sync) {
                task()
            }
        }
    }

    fun tickMain() {
        val time = measuringTimeMS()
        when (Side.currentSide) {
            Side.client -> {
                for (i in tickables) {
                    if (i.shouldTick) {
                        try {
                            i.clientTick()
                            i.ticksExisted++
                        } catch (e:Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            Side.server -> {
                for (i in tickables) {
                    if (i.shouldTick) {
                        try {
                            i.serverTick()
                            i.ticksExisted++
                        } catch (e:Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
        tpsi++
        if (time - lastMetricSec >= 1000) {
            tps = tpsi
            tpsi = 0
            lastMetricSec = time
        }
    }

    fun contains(tickable:Tickable):bool {
        return tickable.ticksExisted != -1
    }

    fun add(tickable:Tickable):Tickable {
        if (contains(tickable)) {
            return tickable
        }
        tickables.add(tickable)
        tickables.sortBy { it.priority }
        return tickable
    }

    fun rem(tickable:Tickable):Tickable {
        if (contains(tickable)) {
            tickables.remove(tickable)
        }
        return tickable
    }

    fun shutdown() {
        thread_exec.shutdownNow()
    }

    init {
        this.prevTimeMillis = measuringTimeMS()
    }

}