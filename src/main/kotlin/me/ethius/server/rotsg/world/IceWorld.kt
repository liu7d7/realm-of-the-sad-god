package me.ethius.server.rotsg.world

import me.ethius.server.rotsg.world.biome.BiomeType
import me.ethius.server.rotsg.world.biome.CustomBiome
import me.ethius.server.rotsg.world.biome.SimpleBiome
import me.ethius.shared.dvec2
import me.ethius.shared.opti.TexData
import me.ethius.shared.rotsg.tile.Bushery
import me.ethius.shared.rotsg.tile.Tile

class IceWorld(vararg entities:me.ethius.shared.rotsg.entity.AEntity):ReturnableWorld("Ice World", *entities) {

    private val playerPos = dvec2(0.0, 0.0)

    private fun setupWorld() {
        abiomes.add(SimpleBiome(0, 0, this, 15, BiomeType.ice).also { it.maxEntities = 2 })
        abiomes.add(CustomBiome(0, 0, this, 28) { Tile(it, TexData.snow_1, Bushery.empty_bb) })
    }

    init {
        setupWorld()
        this.texDataId = "ice_portal"
    }

}