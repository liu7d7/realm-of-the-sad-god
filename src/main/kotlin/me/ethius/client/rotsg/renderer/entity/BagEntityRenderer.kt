package me.ethius.client.rotsg.renderer.entity

import me.ethius.client.Client
import me.ethius.client.ext.push
import me.ethius.client.rotsg.entity.Bag
import me.ethius.shared.interpolateColor
import me.ethius.shared.measuringTimeMS
import me.ethius.shared.opti.TexData
import me.ethius.shared.sin
import me.ethius.shared.toRadians
import org.joml.Matrix4dStack
import kotlin.math.absoluteValue

class BagEntityRenderer:EntityRenderer<Bag>() {
    override fun renderInternal(matrix:Matrix4dStack, entity:Bag) {
        with(entity) {
            matrix.push {
                center(matrix, entity) {
                    matrix.scale(4.0, 1.0, 4.0)
                }
                Client.render.drawTexCenteredVerticalWithoutEnding(TexData[texDataId],
                                                                   matrix,
                                                                   x,
                                                                   y + depth * 0.2f,
                                                                   TexData[texDataId].width,
                                                                   TexData[texDataId].height - depth * 0.2f,
                                                                   if (measuringTimeMS() - timeSpawned > 15000) interpolateColor(
                                                                       0xffffffff, 0xff777777,
                                                                       ((sin(((measuringTimeMS() / 3f) % 360f).toRadians()).absoluteValue + 1f) / 2f)) else 0xffffffff)
            }
        }
    }
}