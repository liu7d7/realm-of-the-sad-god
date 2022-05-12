package me.ethius.client.rotsg.renderer.entity

import me.ethius.client.Client
import me.ethius.client.ext.getDegreesQuaternion
import me.ethius.client.ext.multiply
import me.ethius.client.ext.push
import me.ethius.client.ext.translate
import me.ethius.shared.ext.POSITIVE_Z
import me.ethius.shared.opti.TexData
import me.ethius.shared.rotsg.entity.other.Projectile
import me.ethius.shared.wrapDegrees
import org.joml.Matrix4dStack

class ProjectileEntityRenderer:EntityRenderer<Projectile>() {
    override fun renderInternal(matrix:Matrix4dStack, entity:Projectile) {
        with(entity) {
            if (ticksExisted == 0)
                return
            matrix.push {
                matrix.translate(lerpedX, lerpedY, 0.0) {
                    matrix.scale(scale, scale, 1.0)
                    matrix.multiply(POSITIVE_Z.getDegreesQuaternion(wrapDegrees(this.lerpedDirection + this.lraa + 180f)))
                }
                matrix.translate(0.0, 0.0, z)
                Client.render.drawTexCenteredWithoutEnding(TexData[texDataId], matrix, lerpedX, lerpedY)
            }
        }
    }
}