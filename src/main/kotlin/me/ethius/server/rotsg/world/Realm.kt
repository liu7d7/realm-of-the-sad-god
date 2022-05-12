package me.ethius.server.rotsg.world

import me.ethius.server.Server
import me.ethius.server.rotsg.entity.ServerPlayer
import me.ethius.server.rotsg.world.biome.BiomeType
import me.ethius.server.rotsg.world.biome.CustomBiome
import me.ethius.server.rotsg.world.biome.SimpleBiome
import me.ethius.shared.ivec2
import me.ethius.shared.network.Packet
import me.ethius.shared.opti.TexData
import me.ethius.shared.rotsg.data.Formatting
import me.ethius.shared.rotsg.tile.Bushery
import me.ethius.shared.rotsg.tile.Tile
import me.ethius.shared.string
import org.apache.commons.lang3.RandomUtils

class Realm(vararg entities:me.ethius.shared.rotsg.entity.AEntity):ServerWorld("Realm", *entities) {

    override lateinit var spawnPosition:ivec2
    var worldEvent = null as WorldEvent?

    private fun setupWorld() {
        abiomes.add(SimpleBiome(2 - RandomUtils.nextInt(0, 5),
                                2 - RandomUtils.nextInt(0, 5),
                                this,
                                20,
                                BiomeType.mountains))
        for (_i in -12..12) {
            val i = _i * 24
            val sign = if (_i % 2 == 0) 1 else -1
            for (_j in -288..288 step 24) {
                val j = _j * sign
                abiomes.add(SimpleBiome(i + 2 - RandomUtils.nextInt(0, 5),
                                        j + 2 - RandomUtils.nextInt(0, 5),
                                        this).also {
                    if (it.type == BiomeType.desert && RandomUtils.nextBoolean() && RandomUtils.nextBoolean()) {
                        spawnPosition = ivec2(it.x, it.y)
                    }
                })
            }
        }
        abiomes.add(SimpleBiome(0, 0, this, (328 * 1.4142135f).toInt(), BiomeType.lake))
        abiomes.add(CustomBiome(0, 0, this, (328 * 1.4142135f).toInt() + 15) {
            Tile(it, TexData.border_water_1, Bushery.empty_bb)
        })
    }

    override fun serverTickInternal() {
        if (ticksExisted % 20 == 0) {
            if (ticksExisted >= 100 && (worldEvent == null || worldEvent?.isFinished(this) == true)) {
                val e =
                    this.tiles.values.filter { it.texDataId != "water_1" && it.texDataId != "border_water_1" }
                        .random()
                worldEvent = WorldEvent.values.random()
                worldEvent!!.executeAtPos(this, e.pos.x, e.pos.y)
                Server.network.broadcast(Packet._id_chat, "<${Formatting.gold}WorldEvent${Formatting.reset}> ${Formatting.gold}${worldEvent?.id}" +
                                                          "${Formatting.reset} has happened at " + "${Formatting.green}[${e.pos.x}, ${e.pos.y}]")
            }
        }
    }

    override fun onPlayerAdd(entity:ServerPlayer) {
        super.onPlayerAdd(entity)
        Server.network.send(entity.client, Packet._id_chat, "<${Formatting.gold}WorldEvent${Formatting.reset}> ${Formatting.gold}${worldEvent?.id}" + "${Formatting.reset} has happened at " + "${Formatting.green}[${worldEvent?.pos?.x}, ${worldEvent?.pos?.y}]")
    }

    init {
        setupWorld()
    }

    companion object {
        lateinit var worldId:string
    }

}