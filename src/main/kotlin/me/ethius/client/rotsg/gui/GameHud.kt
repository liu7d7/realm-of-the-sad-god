package me.ethius.client.rotsg.gui

import me.ethius.client.Client
import me.ethius.client.renderer.Mesh
import me.ethius.client.renderer.RenderLayer
import me.ethius.client.rotsg.entity.Portal
import me.ethius.shared.double
import me.ethius.shared.events.Listen
import me.ethius.shared.events.def.MouseClickedEvent
import me.ethius.shared.maths.MaxArrayList
import me.ethius.shared.measuringTimeMS
import me.ethius.shared.string
import me.ethius.shared.tickTime
import org.joml.Matrix4dStack
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.min
import kotlin.math.roundToInt

const val dul = 0xff1c1b22
const val ldu = 0xff282c34
const val lldu = 0xff3d424d

private const val expDark = 0xff9a5cb3
private const val hpDark = 0xffeb5447
private const val mpDark = 0xff32a1e5

class GameHud {

    private val statView = StatView()
    val portals = CopyOnWriteArrayList<Portal>()
    val chatHud = ChatHud()
    private val mspts = MaxArrayList<double>(300)
    var ms = 1f
    var lastUpdate = 0f
    var debug = false

    fun render(matrix:Matrix4dStack) {
        Client.renderTaskTracker.layer(RenderLayer.hud)
        Client.renderTaskTracker.track {
            Mesh.triangles.begin()
            Client.font.begin(0.965)
            chatHud.render(matrix)

            if (Client.playerInit && Client.screen == null) {
                statView.render(matrix)

                var offset = 0f
                if (portals.isNotEmpty()) {
                    for (v in portals) {
                        v.offset = offset
                        v.renderInHud(matrix)
                        offset += v.renderHeight + 10
                    }
                }

                // hp/mp //
                Client.font.drawCenteredStringWithoutEnding(matrix,
                                                            getCoordsStr(),
                                                            Client.window.midX.toDouble(),
                                                            15.0,
                                                            0xffffffff,
                                                            true)
                drawHpBar(matrix)
                drawMpBar(matrix)
                drawExpBar(matrix)
            }
            mspts += (Client.ticker.lastFrameDuration * tickTime)
            if (measuringTimeMS() - lastUpdate >= 250f) {
                ms = mspts.average().toFloat()
                lastUpdate = measuringTimeMS()
            }
            val stringMs = ms.toString()
            val stringFps = "${(1000f / ms).roundToInt()}"
            Client.font.drawWithoutEnding(matrix,
                                   "MSPF: ${
                                       stringMs.substring(0..min(stringMs.length - 1,
                                                                 4))
                                   } | FPS: $stringFps | Draw Calls: ${Mesh.numDrawCalls + 1}",
                                   2.0,
                                   if (this@GameHud.chatHud.isTyping) {
                                       Client.window.scaledHeight - Client.font.getHeight(true) - 24.0
                                   } else {
                                       Client.window.scaledHeight - Client.font.getHeight(true) - 2.0
                                   },
                                   0xffffffff,
                                   true)
            Mesh.drawTriangles()

            if (Client.playerInit && Client.screen == null) {
                // inv //
                Client.player.inventory.render(matrix)
            }
        }

    }

    private fun drawHpBar(matrix:Matrix4dStack) {
        Client.render.drawRectWithoutEnding(matrix,
                                       3.0,
                                       3.0,
                                       3.0 + 2.0 + (Client.player.hp / Client.player.life.toDouble()) * 148.0,
                                       3.0 + 19.0,
                                       hpDark)
        Client.font.drawWithoutEnding(matrix, "HP", 5.0, 4.5, 0xffffffff, true, 0.85)
        Client.font.drawLeftWithoutEnding(matrix,
                                          "${Client.player.hp.roundToInt()}/${Client.player.life}",
                                          148.0,
                                          5.5,
                                          0xffffffff,
                                          true,
                                          0.8)
    }

    private fun drawMpBar(matrix:Matrix4dStack) {
        Client.render.drawRectWithoutEnding(matrix,
                                       3.0,
                                       26.5,
                                       3.0 + 2.0 + (Client.player.mp / Client.player.maxMp.toDouble()) * 148.0,
                                       26.5 + 19.0,
                                       mpDark)
        Client.font.drawWithoutEnding(matrix, "MP", 5.0, 29.0, 0xffffffff, true, 0.85)
        Client.font.drawLeftWithoutEnding(matrix,
                                          "${Client.player.mp.roundToInt()}/${Client.player.maxMp}",
                                          148.0,
                                          30.0,
                                          0xffffffff,
                                          true,
                                          0.8)
    }

    private fun drawExpBar(matrix:Matrix4dStack) {
        val xpToNext = (Client.player.nextLevel() - Client.player.prevLevel()).toFloat()
        val xpProgress = (Client.player.exp - Client.player.prevLevel()).toFloat()
        Client.render.drawRectWithoutEnding(matrix,
                                       3.0,
                                       49.5,
                                       3.0 + 2.0 + (xpProgress / xpToNext) * 148.0,
                                       49.5 + 19.0,
                                       expDark)
        Client.font.drawWithoutEnding(matrix,
                                      "LVL ${Client.player.level}",
                                      5.0,
                                      52.0,
                                      0xffffffff,
                                      true,
                                      0.85)
        Client.font.drawLeftWithoutEnding(matrix,
                                          "${xpProgress.toInt()}/${xpToNext.toInt()}",
                                          148.0,
                                          53.0,
                                          0xffffffff,
                                          true,
                                          0.8)
    }

    private fun getCoordsStr():string {
        return "XY: ${Client.player.tilePos.x}, ${Client.player.tilePos.y}"
    }

    @Listen
    fun click(event:MouseClickedEvent) {
        for (it in portals) {
            it.click(event)
        }
    }

    init {
        Client.events.register(this)
    }

}

