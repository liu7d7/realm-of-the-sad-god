package me.ethius.client.rotsg.renderer.entity

import me.ethius.client.Client
import me.ethius.client.ext.getDegreesQuaternion
import me.ethius.client.ext.multiply
import me.ethius.client.ext.push
import me.ethius.client.ext.translate
import me.ethius.shared.ext.NEGATIVE_Z
import me.ethius.shared.ext.POSITIVE_Z
import me.ethius.shared.maths.Facing
import me.ethius.shared.opti.TexData
import me.ethius.shared.rotsg.entity.enemy.Enemy
import org.joml.Matrix4dStack

class EnemyEntityRenderer:EntityRenderer<Enemy>() {
    override fun renderInternal(matrix:Matrix4dStack, entity:Enemy) {
        with(entity) {
            matrix.push {
                matrix.translate(lerpedX, lerpedY, 0.0) {
                    matrix.multiply(POSITIVE_Z.getDegreesQuaternion(when (facing) {
                                                                        Facing.left -> 0.0
                                                                        Facing.right -> -180.0
                                                                        else -> -180.0
                                                                    }))
                    matrix.multiply(NEGATIVE_Z.getDegreesQuaternion(Client.player.lerpedR))
                    matrix.scale(scale, 1.0, scale)
                }
                Client.render.drawTexCenteredVerticalWithoutEnding(TexData[texDataId],
                                                                   matrix,
                                                                   lerpedX,
                                                                   lerpedY + depth * 0.2f,
                                                                   TexData[texDataId].width,
                                                                   TexData[texDataId].height - depth * 0.2f,
                                                                   if (hasEffect("curse")) 0xffff0000 else 0xffffffff)
            }
        }
    }
}