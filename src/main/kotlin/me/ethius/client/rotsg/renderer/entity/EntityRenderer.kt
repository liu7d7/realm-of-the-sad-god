package me.ethius.client.rotsg.renderer.entity

import me.ethius.client.Client
import me.ethius.client.ext.getDegreesQuaternion
import me.ethius.client.ext.multiply
import me.ethius.client.ext.push
import me.ethius.client.ext.translate
import me.ethius.client.rotsg.entity.OtherPlayer
import me.ethius.client.rotsg.gui.ldu
import me.ethius.shared.ext.NEGATIVE_X
import me.ethius.shared.ext.NEGATIVE_Z
import me.ethius.shared.lambda_v
import me.ethius.shared.rotsg.entity.AEntity
import me.ethius.shared.rotsg.entity.PassableEntity
import org.joml.Matrix4dStack
import java.awt.Color

abstract class EntityRenderer<T:AEntity> {

    fun render(matrix:Matrix4dStack, entity:AEntity) {
        renderInternal(matrix, entity as T)
    }

    protected abstract fun renderInternal(matrix:Matrix4dStack, entity:T)

    companion object {
        // render the effects //
        fun renderEffects(matrix:Matrix4dStack, entity:AEntity) {
            with(entity) {
                val effects = effects.distinctBy { it.id }
                // get x and y //
                val x = lerpedX + pivotX
                val y = lerpedY + pivotY
                // start of effects //
                val fortnite = x - effects.sumOf { it.texData.width * 0.6 + 3.5f } + 3.5f
                // push the matrix //
                matrix.push {
                    matrix.translate(0.0, 0.0, 120.0)
                    // translate to the entity's position //
                    center(matrix, entity) { }
                    // render each effect using Main.render //wd
                    var xOffset = 1.5
                    for (i in effects.indices) {
                        matrix.push {
                            matrix.translate(fortnite + xOffset, y + yCompY - 16f + 88, 0.0) {
                                matrix.scale(1.2, 1.3, 1.0)
                            }
                            Client.render.drawTexWithoutEnding(effects[i].texData, matrix, fortnite + xOffset, y + yCompY - 16f + 110)
                        }
                        xOffset += effects[i].texData.width * 1.2 + 3.5 * 1.2
                    }
                }
            }
        }

        // render this entity's health //
        fun renderBars(matrix:Matrix4dStack, entity:AEntity) {
            // if this entity is a PassableEntity, return //
            with(entity) {
                if (this !is PassableEntity) {
                    // get x and y //
                    val x = lerpedX + pivotX
                    val y = lerpedY + pivotY
                    // push the matrix //
                    matrix.push {
                        // translate to the entity's position //
                        center(matrix, entity, lambda_v)
                        // render the health bar //
                        matrix.translate(0.0, 0.0, -0.1) {
                            Client.render.drawRectWithoutEnding(matrix,
                                                                x - 20 + 1.25f,
                                                                y - yCompY + 3.25f,
                                                                x + 20f - 1.25f,
                                                                y - yCompY + 6.75f,
                                                                0xff0c0000)
                        }
                        Client.render.drawRectWithoutEnding(matrix,
                                                            x - 20 + 1.25f,
                                                            y - yCompY + 3.25f,
                                                            x - 20 + 40f * (this.hp / this.life.toDouble()) - 1.25f,
                                                            y - yCompY + 6.75f,
                                                            Color.GREEN.rgb.toLong())
                    }
                }
            }
        }

        fun renderNotifications(matrix:Matrix4dStack, entity:AEntity) {
            with(entity) {
                for (it in entityNotifications) {
                    it.render(matrix, yCompY)
                }
                if (this is OtherPlayer) {
                    matrix.push {
                        center(matrix, entity) {
                            matrix.multiply(NEGATIVE_X.getDegreesQuaternion(Client.camAngleX))
                        }
                        val width = Client.font.getWidth(this.name, 0.65) * 1.1
                        val height = Client.font.getHeight(true, 0.65) * 1.6
                        Client.render.drawRectAlphaWithoutEnding(matrix, this.lerpedX - width / 2f, this.lerpedY - this.texData.height * 5.25 - height / 2f - 5, this.lerpedX + width / 2f, this.lerpedY - this.texData.height * 5.25, ldu, 0.4f)
                        Client.font.drawCenteredStringWithoutEnding(matrix, this.name, this.lerpedX, this.lerpedY - this.texData.height * 5.25 - 4, 0xffffffff, true, 0.65)
                    }
                }
                entityNotifications.removeIf { it.isDone }
            }
        }

        inline fun center(matrix:Matrix4dStack, entity:AEntity, action:() -> Unit) {
            with(entity) {
                matrix.translate(lerpedX, lerpedY, 0.0) {
                    matrix.multiply(NEGATIVE_Z.getDegreesQuaternion(Client.player.lerpedR))
                    action()
                }
            }
        }
    }
}