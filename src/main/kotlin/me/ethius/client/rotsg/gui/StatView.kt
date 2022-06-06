package me.ethius.client.rotsg.gui

import me.ethius.client.Client
import me.ethius.client.ext.getDegreesQuaternion
import me.ethius.client.ext.multiply
import me.ethius.client.ext.push
import me.ethius.client.ext.translate
import me.ethius.shared.bool
import me.ethius.shared.events.Listen
import me.ethius.shared.events.def.MouseClickedEvent
import me.ethius.shared.ext.POSITIVE_Z
import me.ethius.shared.int
import me.ethius.shared.rotsg.data.Formatting
import me.ethius.shared.rotsg.entity.Stat
import me.ethius.shared.string
import org.joml.Matrix4dStack
import org.lwjgl.glfw.GLFW
import kotlin.math.ceil

class StatView {

    private var width = 0.0
    var height = 0.0
    private var visible = false
    private var locked = false

    fun render(matrix:Matrix4dStack) {
        width = Client.font.getHeight(true) + 8.5
        height = Client.font.getHeight(true) * 4.0 + 5.0
        visible = isInMain() || (visible && isInOther()) || locked
        val top = 0.0
        // render main //
        Client.render.drawRectAlphaWithoutEnding(matrix,
                                                 Client.window.scaledWidth - width - 0.1,
                                                 top,
                                                 Client.window.scaledWidth,
                                                 top + height,
                                                 if (isInMain()) lldu else ldu, 0.3f)
        if (!visible)
            Client.render.drawShadowOutlineRectWithoutEnding(matrix,
                                                             Client.window.scaledWidth - width - 0.1,
                                                             top,
                                                             width,
                                                             height,
                                                             0x60000000,
                                                             4.0,
                                                             false,
                                                             true,
                                                             false,
                                                             true)
        matrix.push {
            matrix.translate(Client.window.scaledWidth - width / 2f, top + height / 2f, 1.0) {
                matrix.multiply(POSITIVE_Z.getDegreesQuaternion(90.0))
            }
            Client.font.drawCenteredStringWithoutEnding(matrix,
                                                        "Stats",
                                                        Client.window.scaledWidth - width / 2f,
                                                        top + height / 2f,
                                                        0xffffffff,
                                                        true,
                                                        1.0)
        }
        if (visible) {
            Client.render.drawRectAlphaWithoutEnding(matrix,
                                                     Client.window.scaledWidth - 210.0,
                                                     top,
                                                     Client.window.scaledWidth - width,
                                                     top + height,
                                                     ldu, 0.3f)
            Client.font.drawWithoutEnding(matrix,
                                          "LIFE: ${Client.player.life} [${
                                              getFormattedPlayerStatAdd(Stat.life)
                                          }]",
                                          Client.window.scaledWidth - 207.5,
                                          top + 2.5f,
                                          0xffffffff,
                                          true, 0.7)
            Client.font.drawWithoutEnding(matrix,
                                          "ATK: ${Client.player.atk} [${
                                              getFormattedPlayerStatAdd(Stat.atk)
                                          }]",
                                          Client.window.scaledWidth - 207.5,
                                          top + 2.5f + (Client.font.getHeight(true)),
                                          0xffffffff,
                                          true, 0.7)
            Client.font.drawWithoutEnding(matrix,
                                          "SPD: ${Client.player.spd} [${
                                              getFormattedPlayerStatAdd(Stat.spd)
                                          }]",
                                          Client.window.scaledWidth - 207.5,
                                          top + 2.5f + (Client.font.getHeight(true)) * 2,
                                          0xffffffff,
                                          true, 0.7)
            Client.font.drawWithoutEnding(matrix,
                                          "VIT: ${Client.player.vit} [${
                                              getFormattedPlayerStatAdd(Stat.vit)
                                          }]",
                                          Client.window.scaledWidth - 207.5,
                                          top + 2.5f + (Client.font.getHeight(true)) * 3,
                                          0xffffffff,
                                          true, 0.7)
            Client.font.drawWithoutEnding(matrix,
                                          "MANA: ${Client.player.mana} [${
                                              getFormattedPlayerStatAdd(Stat.mana)
                                          }]",
                                          Client.window.scaledWidth - 105.5,
                                          top + 2.5f,
                                          0xffffffff,
                                          true, 0.7)
            Client.font.drawWithoutEnding(matrix,
                                          "DEF: ${Client.player.def} [${
                                              getFormattedPlayerStatAdd(Stat.def)
                                          }]",
                                          Client.window.scaledWidth - 105.5,
                                          top + 2.5f + (Client.font.getHeight(true)),
                                          0xffffffff,
                                          true, 0.7)
            Client.font.drawWithoutEnding(matrix,
                                          "DEX: ${Client.player.dex} [${
                                              getFormattedPlayerStatAdd(Stat.dex)
                                          }]",
                                          Client.window.scaledWidth - 105.5,
                                          top + 2.5f + (Client.font.getHeight(true)) * 2,
                                          0xffffffff,
                                          true, 0.7)
            Client.font.drawWithoutEnding(matrix,
                                          "WIS: ${Client.player.wis} [${
                                              getFormattedPlayerStatAdd(Stat.wis)
                                          }]",
                                          Client.window.scaledWidth - 105.5,
                                          top + 2.5f + (Client.font.getHeight(true)) * 3,
                                          0xffffffff,
                                          true, 0.7)
            Client.render.drawShadowOutlineRectWithoutEnding(matrix,
                                                             Client.window.scaledWidth - 210.0,
                                                             top,
                                                             210.0,
                                                             height,
                                                             0x60000000,
                                                             4.0,
                                                             false,
                                                             true,
                                                             false,
                                                             true)
        }
    }

    private fun isInOther():bool {
        val top = 0f
        val mx = Client.mouse.x
        val my = Client.mouse.y
        return mx > Client.window.scaledWidth - 210f && mx < Client.window.scaledWidth - width && my > top && my < top + height
    }

    private fun isInMain():bool {
        val top = 0f
        val mx = Client.mouse.x
        val my = Client.mouse.y
        return (mx > Client.window.scaledWidth - width && mx < Client.window.scaledWidth && my > top && my < top + height)
    }

    @Listen
    fun click(event:MouseClickedEvent) {
        if (isInMain() && event.action == GLFW.GLFW_PRESS) {
            locked = !locked
        }
    }

    private fun getFormattedPlayerStatAdd(stat:Stat):string {
        val add:int
        val toMax:int
        when (stat) {
            Stat.life -> {
                add = Client.player.lifeAdd
                toMax =
                    ceil((Client.player.pClass.maxStats[stat.ordinal] - Client.player.lifeMaxed) / 5.0).toInt()
            }
            Stat.atk -> {
                add = Client.player.atkAdd
                toMax = Client.player.pClass.maxStats[stat.ordinal] - Client.player.atkMaxed
            }
            Stat.dex -> {
                add = Client.player.dexAdd
                toMax = Client.player.pClass.maxStats[stat.ordinal] - Client.player.dexMaxed
            }
            Stat.def -> {
                add = Client.player.defAdd
                toMax = Client.player.pClass.maxStats[stat.ordinal] - Client.player.defMaxed
            }
            Stat.mana -> {
                add = Client.player.manaAdd
                toMax =
                    ceil((Client.player.pClass.maxStats[stat.ordinal] - Client.player.manaMaxed) / 5.0).toInt()
            }
            Stat.spd -> {
                add = Client.player.spdAdd
                toMax = Client.player.pClass.maxStats[stat.ordinal] - Client.player.spdMaxed
            }
            Stat.vit -> {
                add = Client.player.vitAdd
                toMax = Client.player.pClass.maxStats[stat.ordinal] - Client.player.vitMaxed
            }
            Stat.wis -> {
                add = Client.player.wisAdd
                toMax = Client.player.pClass.maxStats[stat.ordinal] - Client.player.wisMaxed
            }
        }
        val string = if (add > 0) {
            "+$add"
        } else {
            "$add"
        }
        return "${if (toMax == 0) Formatting.gold else ""}$string${if (toMax == 0) "" else " | $toMax"}${Formatting.reset}"
    }

    init {
        Client.events.register(this)
    }

}