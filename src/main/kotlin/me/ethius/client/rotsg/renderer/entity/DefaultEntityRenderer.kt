package me.ethius.client.rotsg.renderer.entity

import me.ethius.client.Client
import me.ethius.client.ext.getDegreesQuaternion
import me.ethius.client.ext.multiply
import me.ethius.client.ext.push
import me.ethius.shared.ext.NEGATIVE_Z
import me.ethius.shared.opti.TexData
import me.ethius.shared.rotsg.entity.AEntity
import org.joml.Matrix4dStack

class DefaultEntityRenderer:EntityRenderer<AEntity>() {
    override fun renderInternal(matrix:Matrix4dStack, entity:AEntity) {
        matrix.push {
            center(matrix, entity) {
                matrix.multiply(NEGATIVE_Z.getDegreesQuaternion(Client.player.r))
                matrix.scale(5.0 * 1.05, 1.0, 5.0 * 1.05)
            }
            Client.render.drawTexCenteredVerticalWithoutEnding(TexData[entity.texDataId],
                                                               matrix,
                                                               entity.lerpedX,
                                                               entity.lerpedY + entity.depth * 0.2f,
                                                               TexData[entity.texDataId].width,
                                                               TexData[entity.texDataId].height - entity.depth * 0.2f)
        }
    }
}