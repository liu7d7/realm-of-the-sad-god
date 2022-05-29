package me.ethius.client.rotsg.gui

import me.ethius.client.Client
import me.ethius.client.ext.getDegreesQuaternion
import me.ethius.client.ext.multiply
import me.ethius.client.ext.push
import me.ethius.client.ext.translate
import me.ethius.shared.*
import me.ethius.shared.ext.NEGATIVE_Z
import me.ethius.shared.ext.POSITIVE_Z
import me.ethius.shared.maths.Animations
import me.ethius.shared.rotsg.entity.AEntity
import org.joml.Matrix4dStack
import java.awt.Color

open class EntityNotification(
    private val entityIn:AEntity,
    private val display:string,
    val color:long,
) {

    private val yOffset:float = -45f
    private val start:float = measuringTimeMS()
    val time:float
        get() = measuringTimeMS() - start
    private val entityX:double
        get() = entityIn.lerpedX
    private val entityY:double
        get() = entityIn.lerpedY
    val rX:double
        get() = entityX + entityIn.pivotX
    val rY:double
        get() = entityY + entityIn.pivotY + yOffset * Animations.getDecelerateAnimation(1225f, time)
    val isDone:bool
        get() = time > 1225L

    fun render(matrix:Matrix4dStack, yCompY:double) {
        matrix.push {
            val scale = (if (time < 150f) 1.125f * Animations.getAccelerateAnimation(150f,
                                                                                     time)
            else if (time > 1115f) 1.125f * (1 - Animations.getAccelerateAnimation(220f,
                                                                                   time - 1115f))
            else 1.125f).toDouble()
            matrix.translate(entityX, entityY, 1.0) {
                matrix.multiply(NEGATIVE_Z.getDegreesQuaternion(Client.player.lerpedR))
            }
            matrix.translate(rX, rY + yCompY, 1.0) {
                matrix.scale(scale, scale, 1.0)
                if (time < 150f) {
                    matrix.multiply(POSITIVE_Z.getDegreesQuaternion(Animations.getAccelerateAnimation(150f,
                                                                                                      time) * 15.0))
                } else if (time < 300f) {
                    matrix.multiply(POSITIVE_Z.getDegreesQuaternion(Animations.getAccelerateAnimation(150f,
                                                                                                      150f - (time - 150f)) * 15.0))
                }
            }
            Client.font.drawCenteredStringWithoutEnding(matrix, display, rX, rY + yCompY, color, true)
        }
    }

}

class Damage(entityIn:AEntity, damage:int, throughDef:bool):
    EntityNotification(entityIn, "-${damage}", if (throughDef) 0xff7f00ff
    else withAlpha(
        Color.HSBtoRGB(
            lerp(0f, 100f, entityIn.hp.toFloat() / entityIn.life.toFloat()) / 360f,
            1f, 1f
        ).toLong(), 255L
    )
    )

class Exp(entityIn:AEntity, exp:int):
    EntityNotification(entityIn, "+${exp} EXP", withAlpha(Color.GREEN.rgb.toLong(), 255L))

class LevelUp(entityIn:AEntity):
    EntityNotification(entityIn, "Level Up!", withAlpha(Color.GREEN.rgb.toLong(), 255L))