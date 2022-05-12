package me.ethius.client.rotsg.fx

import me.ethius.client.Client
import me.ethius.client.ext.getDegreesQuaternion
import me.ethius.client.ext.multiply
import me.ethius.client.ext.push
import me.ethius.client.ext.translate
import me.ethius.client.renderer.RenderLayer
import me.ethius.shared.*
import me.ethius.shared.ext.NEGATIVE_Z
import me.ethius.shared.maths.Animations
import me.ethius.shared.opti.TexData
import me.ethius.shared.rotsg.entity.AEntity
import org.apache.commons.lang3.RandomUtils
import org.joml.Matrix4dStack
import java.util.concurrent.CopyOnWriteArrayList

class FxManager:Tickable() {

    override val shouldTick:bool
        get() = Client.worldInit

    private val fxList = CopyOnWriteArrayList<Fx>()

    fun createFx(entity:AEntity, x:double = entity.x, y:double = entity.y) {
        for (i in 5..RandomUtils.nextInt(5, 11)) {
            fxList.add(Fx(x,
                          y,
                          fvec2(1f - RandomUtils.nextFloat(0f, 2f),
                                1f - RandomUtils.nextFloat(0f, 2f)),
                          TexData[entity.texDataId]))
        }
    }

    fun createFx(entity:AEntity, x:double, y:double, fx:Fx, num:int = 5, zBase:double = 50.0) {
        for (i in num..RandomUtils.nextInt(num, num * 2 + 1)) {
            fxList.add(fx.copy(x, y, fvec2(1f - RandomUtils.nextFloat(0f, 2f), 1f - RandomUtils.nextFloat(0f, 2f)))
                           .also { it.zBase = zBase })
        }
    }

    fun createFx(texData:TexData, x:double, y:double, num:int = 5, zBase:double = 50.0, modulateZ:bool = true) {
        for (i in num..RandomUtils.nextInt(num, num * 2 + 1)) {
            fxList.add(Fx(x,
                          y,
                          fvec2(1f - RandomUtils.nextFloat(0f, 2f), 1f - RandomUtils.nextFloat(0f, 2f)),
                          texData).also { it.zBase = zBase; it.modulateZ = modulateZ })
        }
    }

    fun renderFxs(matrix:Matrix4dStack) {
        for (fx in fxList) {
            Client.renderTaskTracker.onLayer(RenderLayer.world_feature) {
                val x = lerp(fx.pX, fx.x, Client.ticker.tickDelta) + fx.width * 0.5
                val y = lerp(fx.pY, fx.y, Client.ticker.tickDelta) + fx.height * 0.5
                val time = measuringTimeMS() - fx.start
                val z = if (fx.modulateZ) (if (time < 250.0) time / 12.5 + 50.0 else ((150.0 - (time - 250.0)) / 150.0) * 70) + fx.zBase else 0.0
                val scale = if (time > 150.0) (1.0 - Animations.getDecelerateAnimation(250.0, time - 150.0)) * 5.0 else 5.0
                matrix.push {
                    matrix.translate(x, y, 0.0) {
                        matrix.multiply(NEGATIVE_Z.getDegreesQuaternion(Client.player.r))
                        matrix.scale(scale, 1.0, scale)
                    }
                    matrix.translate(0.0, 0.0, z / scale * 5.0 / 28.0)
                    Client.render.drawTexCenteredVerticalWithoutEnding(fx.rTexData, matrix, x, y)
                }
            }
        }
    }

    private fun updateFx(fx:Fx) {
        fx.pX = fx.x
        fx.pY = fx.y
        fx.x += fx.velocity.x
        fx.y += fx.velocity.y
    }

    override fun clientTick() {
        for (fx in fxList) {
            updateFx(fx)
        }
        val time = measuringTimeMS()
        fxList.removeIf { time - it.start > 400 }
    }

    init {
        init()
    }

}