package me.ethius.server

import me.ethius.server.network.SNetworkHandler
import me.ethius.server.rotsg.world.Nexus
import me.ethius.server.rotsg.world.Realm
import me.ethius.server.rotsg.world.WorldEvent
import me.ethius.server.rotsg.world.WorldTracker
import me.ethius.server.rotsg.world.biome.BiomeType
import me.ethius.shared.*
import me.ethius.shared.loottable.LootTableEntry
import me.ethius.shared.opti.TexData
import me.ethius.shared.rotsg.data.EffectInfo
import me.ethius.shared.rotsg.data.EntityInfo
import me.ethius.shared.rotsg.data.ProjectileProperties
import me.ethius.shared.rotsg.tile.Bushery
import me.ethius.shared.rotsg.world.biome.BiomeFeature

object Server {

    // public vars
    lateinit var ticker:Ticker
    lateinit var network:SNetworkHandler

    fun main(args:RunArgs) {
        Side.currentSide = Side.server
        System.setProperty("joml.format", "false")
        Log.info + "JOML format set to false" + Log.endl

        BiomeFeature.init()
        EntityInfo.init()
        EffectInfo.init()
        TexData.init()
        BiomeType.init()
        ProjectileProperties.init()
        Bushery.init()
        WorldEvent.init()
        LootTableEntry.init()
        Log.info + "Initialized various data collections" + Log.endl

        this.ticker = Ticker()
        Log.info + "Initialized Ticker" + Log.endl

        this.network = SNetworkHandler()
        Log.info + "Initialized NetworkHandler" + Log.endl

        this.network.start(args.addr, if (args.testing) 9928 else 9927)

        timeGetter = {
            (System.currentTimeMillis() - start).toFloat()
        }

        Realm.worldId = WorldTracker.newWorld { Realm() }
        Nexus.worldId = WorldTracker.newWorld { Nexus() }
        Log.info + "Initialized worlds" + Log.endl

        mainloop()
        shutdown()
    }

    class RunArgs(val addr:string, val testing:bool)

    private fun shutdown() {
        this.network.shutdown()
        this.ticker.shutdown()
    }

    private fun mainloop() {
        var lastTime = System.currentTimeMillis()
        var ticks = 0
        while (true) {
            val begin = System.currentTimeMillis()
            network.reset()
            ticker.tickMain()
            ticks++
            network.actOnQueuedPackets()
            network.flush()
            val end = System.currentTimeMillis()
            val timeSleep = (20 - (end - begin)).coerceAtLeast(0)
            if (timeSleep > 0) {
                Thread.sleep(timeSleep)
            }
            if (System.currentTimeMillis() - lastTime >= 1000) {
                ticks = 0
                lastTime = System.currentTimeMillis()
            }
        }
    }

}