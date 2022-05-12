package me.ethius.shared.rotsg.world

import me.ethius.shared.rotsg.world.biome.BiomeFeature
import me.ethius.shared.rotsg.tile.Tile
import me.ethius.shared.void

private val ttv = { _:Tile -> }

fun addFeatureTiles(
    add:(Tile) -> void,
    feature:BiomeFeature,
    tileMutator:(Tile) -> void = ttv,
):List<Tile> {
    val tiles = mutableListOf<Tile>()
    for (i in feature.feature) {
        i.pos.add(feature.tlPos)
        tileMutator(i)
        add(i)
        tiles.add(i)
    }
    return tiles
}