package me.ethius.server.rotsg.world

import me.ethius.shared.ivec2
import me.ethius.shared.rotsg.data.EntityInfo
import me.ethius.shared.rotsg.world.biome.BiomeFeature
import org.apache.commons.lang3.RandomUtils

class RottenWorld:ReturnableWorld("Rotten World") {
    override var spawnPosition:ivec2 = ivec2(43, 25)

    private fun setupWorld() {
        this.addFeature(BiomeFeature.rotten_world(ivec2(0, 0)))
        for (i in poses) {
            this.addEntity(if (RandomUtils.nextBoolean()) EntityInfo.rotten_dude1 else EntityInfo.rotten_dude2, i)
        }
        this.addEntity(EntityInfo.rotten_boss, bossPos)
    }

    init {
        setupWorld()
    }

    companion object {
        private val poses = listOf(
            ivec2(39, 34),
            ivec2(45, 37),
            ivec2(54, 28),
            ivec2(57, 21),
            ivec2(57, 40),
            ivec2(57, 51),
            ivec2(53, 49),
            ivec2(68, 37),
            ivec2(68, 49),
            ivec2(66, 53),
            ivec2(83, 34),
            ivec2(86, 37),
            ivec2(107, 49),
            ivec2(114, 49),
            ivec2(111, 59),
            ivec2(65, 62),
            ivec2(82, 61),
            ivec2(27, 62),
            ivec2(33, 66),
            ivec2(25, 75),
            ivec2(26, 79),
            ivec2(26, 79),
            ivec2(14, 78),
            ivec2(11, 73),
            ivec2(8, 77),
            ivec2(8, 77),
            ivec2(23, 52),
            ivec2(23, 52),
            ivec2(28, 53),
            ivec2(20, 43),
            ivec2(21, 29),
            ivec2(21, 29),
            ivec2(14, 28),
            ivec2(14, 19),
            ivec2(22, 18),
            ivec2(23, 14),
            ivec2(29, 14),
            ivec2(30, 4),
            ivec2(40, 2),
            ivec2(45, 8),
            ivec2(45, 3),
        )

        val bossPos = ivec2(8, 45)
    }

}