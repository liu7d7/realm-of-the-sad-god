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
import me.ethius.shared.rotsg.data.ProjectileData
import me.ethius.shared.rotsg.tile.Bushery
import me.ethius.shared.rotsg.world.biome.BiomeFeature

object Server {

    // public vars
    lateinit var ticker:Ticker
    lateinit var network:SNetworkHandler

    fun main(args:RunArgs) {
        Side.currentSide = Side.server
        System.setProperty("joml.format", "false")
        BiomeFeature.init()
        EntityInfo.init()
        EffectInfo.init()
        TexData.init()
        BiomeType.init()
        ProjectileData.init()
        Bushery.init()
        WorldEvent.init()
        LootTableEntry.init()
        this.ticker = Ticker()
        this.network = SNetworkHandler()
        this.network.start(args.addr)
        timeGetter = {
            (System.currentTimeMillis() - start).toFloat()
        }
        Realm.worldId = WorldTracker.newWorld { Realm() }
        Nexus.worldId = WorldTracker.newWorld { Nexus() }
        mainloop()
        shutdown()
    }

    class RunArgs(val addr:string)

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
            val timeSleep = (20L - (end - begin)).coerceAtLeast(0)
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