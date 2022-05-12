package me.ethius.server.rotsg.world

import me.ethius.client.rotsg.entity.Portal
import me.ethius.shared.ivec2
import me.ethius.shared.rotsg.world.biome.BiomeFeature
import me.ethius.shared.rotsg.tile.tile_size
import me.ethius.shared.string

class Nexus:ServerWorld("Nexus") {

    private fun setupWorld() {
        this.addEntity(Portal("realm_portal", Realm.worldId, "Realm").also { it.y = -23 * tile_size; it.invulnerable = true })
        this.addFeature(BiomeFeature.nexus(ivec2(-30, -45)))
    }

    init {
        setupWorld()
        this.texDataId = "ice_world"
        this.spawnPosition.x = 0
        this.spawnPosition.y = 0
    }

    companion object {
        lateinit var worldId:string
    }
}