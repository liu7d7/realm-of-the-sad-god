package me.ethius.server.rotsg.world

import me.ethius.server.rotsg.world.biome.CustomBiome
import me.ethius.shared.ivec2
import me.ethius.shared.opti.TexData
import me.ethius.shared.rotsg.world.biome.BiomeFeature
import me.ethius.shared.rotsg.data.EntityInfo
import me.ethius.shared.rotsg.tile.Tile
import me.ethius.shared.rotsg.tile.tile_size
import org.apache.commons.lang3.RandomUtils

class CrystalCaveWorld:ReturnableWorld("Crystal Cave") {
    private fun setupWorld() {
        this.addFeature(BiomeFeature.crystal_room_1(ivec2(-16, 12)))
        this.addEntity(EntityInfo.fb_clone_1().also {
            it.x = -13 * tile_size
            it.prevX = -13 * tile_size
            it.y = 15 * tile_size
            it.prevY = 15 * tile_size

        })
        this.addFeature(BiomeFeature.crystal_room_2(ivec2(-12, -12)))
        this.addEntity(EntityInfo.flame().also {
            it.hp = 40000.0
        }.also {
            it.x = -8 * tile_size
            it.prevX = -8 * tile_size
            it.y = -8 * tile_size
            it.prevY = -8 * tile_size
        })
        this.addFeature(BiomeFeature.crystal_room_1(ivec2(6, -12)))
        this.addEntity(EntityInfo.fb_clone_2().also {
            it.x = 10 * tile_size
            it.prevX = 10 * tile_size
            it.y = -9 * tile_size
            it.prevY = -9 * tile_size
        })
        this.addEntity(EntityInfo.fb_clone_3().also {
            it.x = 11 * tile_size
            it.prevX = 11 * tile_size
            it.y = -9 * tile_size
            it.prevY = -9 * tile_size
        })
        this.addEntity(EntityInfo.fb_clone_1().also {
            it.x = 11 * tile_size
            it.prevX = 11 * tile_size
            it.y = -8 * tile_size
            it.prevY = -8 * tile_size
        })
        this.abiomes.add(CustomBiome(0, 0, this, 24) {
            Tile(it,
                 if (RandomUtils.nextFloat(0f, 1f) < 0.75f) TexData.tropical_grass_1 else TexData.grass_1)
        })
    }

    init {
        setupWorld()
        texDataId = "crystal_portal"
    }
}