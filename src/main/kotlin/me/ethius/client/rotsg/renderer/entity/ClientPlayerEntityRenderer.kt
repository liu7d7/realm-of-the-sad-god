package me.ethius.client.rotsg.renderer.entity

import me.ethius.client.Client
import me.ethius.client.ext.push
import me.ethius.client.rotsg.entity.ClientPlayer
import org.joml.Matrix4dStack

private const val scale = 1.05 * 5

class ClientPlayerEntityRenderer:EntityRenderer<ClientPlayer>() {
    override fun renderInternal(matrix:Matrix4dStack, entity:ClientPlayer) {
        with(entity) {
            matrix.push {
                center(matrix, this) {
                    matrix.scale(scale, 1.0, scale)
                }
                Client.render.drawTexCenteredVerticalWithoutEnding(texData,
                                                                   matrix,
                                                                   this.lerpedX + this.texXOffset,
                                                                   this.lerpedY + depth * 0.2f,
                                                                   texData.width,
                                                                   texData.height - depth * 0.2f)
            }
        }
    }
}