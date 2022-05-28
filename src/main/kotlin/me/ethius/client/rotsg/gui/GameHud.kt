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
import me.ethius.shared.tickTime
import org.joml.Matrix4dStack
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.min
import kotlin.math.roundToInt

const val dul = 0xff1c1b22
const val ldu = 0xff282c34
const val lldu = 0xff3d424d

private const val expColor = 0xff58ff77
private const val hpColor = 0xffff5a5a
private const val mpColor = 0xffffbf00

const val barHeight = 26.0
const val barSpacing = 6.5
const val charHeight = barHeight * 2 + barSpacing
const val padding = 15.0
const val expBarWidth = 200.0 + charHeight
const val totalBarsHeight = barHeight * 3 + barSpacing * 2

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
                                                            "XY: ${Client.player.tilePos.x}, ${Client.player.tilePos.y}",
                                                            Client.window.midX.toDouble(),
                                                            15.0,
                                                            0xffffffff,
                                                            true)
                Client.font.drawWithoutEnding(matrix, Client.player.hp.roundToInt().toString(), padding + barSpacing + expBarWidth, Client.window.scaledHeight - padding - barSpacing - barHeight * 1.5 - Client.font.getHeight(true) / 2, hpColor, true)
                Client.font.drawWithoutEnding(matrix, Client.player.mp.roundToInt().toString(), padding + barSpacing + expBarWidth, Client.window.scaledHeight - padding - barHeight * 0.5 - Client.font.getHeight(true) / 2, mpColor, true)
                Client.font.drawWithoutEnding(matrix, Client.player.level.toString(), padding + barSpacing + expBarWidth, Client.window.scaledHeight - padding - charHeight - barSpacing - barHeight * 0.5 - Client.font.getHeight(true) / 2.0, expColor, true)
            }
            mspts += (Client.ticker.lastFrameDuration * tickTime)
            if (measuringTimeMS() - lastUpdate >= 250f) {
                ms = mspts.average().toFloat()
                lastUpdate = measuringTimeMS()
            }
            val stringMs = ms.toString()
            val stringFps = "${(1000f / ms).roundToInt()}"
            Client.font.drawLeftWithoutEnding(matrix,
                                   "MSPF: ${stringMs.substring(0..min(stringMs.length - 1, 4))} | FPS: $stringFps | Draw Calls: ${Mesh.numDrawCalls + 1}",
                                   Client.window.scaledWidth - 2.0,
                                   Client.window.scaledHeight - Client.font.getHeight(true) - 2.0,
                                   0xffffffff,
                                   true)
            Mesh.drawTriangles()

            if (Client.playerInit && Client.screen == null) {
                // inv //
                Client.player.inventory.render(matrix)
            }
        }

    }

    fun renderOutlined(matrix:Matrix4dStack) {
        drawHpBar(matrix)
        drawMpBar(matrix)
        drawExpBar(matrix)
        drawChar(matrix)
    }

    private fun drawHpBar(matrix:Matrix4dStack) {
        Client.render.drawRectWithoutEnding_WH(matrix, padding + barSpacing + charHeight, Client.window.scaledHeight - padding - barSpacing - barHeight * 2, expBarWidth - barSpacing - charHeight, barHeight, ldu)
        Client.render.drawRectWithoutEnding_WH(matrix, padding + barSpacing + charHeight, Client.window.scaledHeight - padding - barSpacing - barHeight * 2, (expBarWidth - barSpacing - charHeight) * Client.player.hp / Client.player.life.toDouble(), barHeight, hpColor)
    }

    private fun drawMpBar(matrix:Matrix4dStack) {
        Client.render.drawRectWithoutEnding_WH(matrix, padding + barSpacing + charHeight, Client.window.scaledHeight - padding - barHeight, expBarWidth - barSpacing - charHeight, barHeight, ldu)
        Client.render.drawRectWithoutEnding_WH(matrix, padding + barSpacing + charHeight, Client.window.scaledHeight - padding - barHeight, (expBarWidth - barSpacing - charHeight) * Client.player.mp / Client.player.maxMp.toDouble(), barHeight, mpColor)
    }

    private fun drawExpBar(matrix:Matrix4dStack) {
        val xpToNext = (Client.player.nextLevel() - Client.player.prevLevel()).toFloat()
        val xpProgress = (Client.player.exp - Client.player.prevLevel()).toFloat()
        Client.render.drawRectWithoutEnding_WH(matrix, padding, Client.window.scaledHeight - padding - charHeight - barSpacing - barHeight, expBarWidth, barHeight, ldu)
        Client.render.drawRectWithoutEnding_WH(matrix, padding, Client.window.scaledHeight - padding - charHeight - barSpacing - barHeight, expBarWidth * xpProgress / xpToNext, barHeight, expColor)
    }

    private fun drawChar(matrix:Matrix4dStack) {
        Client.render.drawTexWithoutEnding(Client.player.pTexData.right, matrix, padding, Client.window.scaledHeight - padding - charHeight, 0.0, 0xffffffff, charHeight, charHeight)
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

