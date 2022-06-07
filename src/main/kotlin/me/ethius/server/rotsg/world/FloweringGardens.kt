package me.ethius.server.rotsg.world

import me.ethius.shared.ivec2
import me.ethius.shared.rotsg.world.biome.BiomeFeature

class FloweringGardens:ServerWorld("Flowering Gardens") {

    init {
        this.addFeature(BiomeFeature(ivec2(-15, -15), "FloweringGardens"))
        this.texDataId = "flowering_portal"
    }

}