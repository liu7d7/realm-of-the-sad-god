package me.ethius.client.rotsg.screen.worldbuilder

import me.ethius.client.Client
import me.ethius.client.renderer.disableScissor
import me.ethius.client.renderer.enableScissor
import me.ethius.client.rotsg.gui.Button
import me.ethius.client.rotsg.gui.ldu
import me.ethius.shared.*
import me.ethius.shared.events.def.MouseClickedEvent
import me.ethius.shared.events.def.MouseScrolledEvent
import me.ethius.shared.opti.TexData
import me.ethius.shared.rotsg.tile.Bushery
import me.ethius.shared.rotsg.tile.Tile
import me.ethius.shared.rotsg.tile.tile_size
import org.joml.Matrix4dStack
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sign

class WorldBuilderDropDownMenu(
    private val worldBuilder:WorldBuilderScreen,
    x:double,
    y:double,
) {

    val width:double = 300.0
    val height:double = 300.0

    val x = if (x + this.width >= Client.window.scaledWidth) {
        x - this.width
    } else {
        x
    }
    val y = if (y + this.height >= Client.window.scaledHeight) {
        y - this.height
    } else {
        y
    }

    private val bx = floor(x / tile_size).toInt()
    private val by = floor(y / tile_size).toInt()

    private lateinit var tile:Tile

    private var scrollYTiles = 0f
    private var scrollYEnv = 0f
    private val buttonsTiles = TexData.values.values.filter { it.type == TexData.Type.tile }.map { tex ->
        Button().setOnLeft {
            this.tile = Tile(ivec2(bx, by), tex, null)
        }.setText {
            "${tex.id}  "
        }.setTextAlignment(
            Button.TextAlignment.right
        ).setAdditionalRendering { matrixStack, button ->
            Client.render.drawTex(tex,
                                  matrixStack,
                                  button.centerX - button.width / 2f + 5f,
                                  button.centerY - button.height / 2f + 5f,
                                  30.0,
                                  30.0)
        }
    }
    private val buttonsEnvs = Bushery.values.map { bush ->
        Button().setOnLeft {
            if (this::tile.isInitialized) {
                this.tile.env = Bushery.copy(bush)
            }
        }.setText {
            "${bush.id}  "
        }.setTextAlignment(
            Button.TextAlignment.right
        ).setAdditionalRendering { matrixStack, button ->
            Client.render.drawTex(TexData[bush.texDataId],
                                  matrixStack,
                                  button.centerX - button.width / 2f + 20f - TexData[bush.texDataId].width / 2f,
                                  button.centerY - button.height / 2f + 20f - TexData[bush.texDataId].height / 2f)
        }
    }

    private val close =
        Button().setCenterX(this.x + this.width - 25.0).setCenterY(this.y + 12.0).setWidth(50.0).setHeight(20.0)
            .setText { "Done" }.setOnLeft {
                when (worldBuilder.mode) {
                    WorldBuilderScreen.Mode.selection -> {
                        endSelection()
                    }
                    WorldBuilderScreen.Mode.draw -> {
                        endDraw()
                    }
                    else -> { }
                }
                this.worldBuilder.tileDDM = null
            }

    private fun endDraw() {
        worldBuilder.tddmCountdown = measuringTimeMS()
        worldBuilder.tileToDraw = this.tile
    }

    private fun endSelection() {
        if (this::tile.isInitialized) {
            if (this.worldBuilder.sel2ed) {
                for (i in safeRange(this.worldBuilder.sel1.x, this.worldBuilder.sel2.x)) {
                    for (j in safeRange(this.worldBuilder.sel1.y, this.worldBuilder.sel2.y)) {
                        this.tile.pos.x = i
                        this.tile.pos.y = j
                        this.worldBuilder.world[ivec2(i, j)] =
                            Tile(ivec2(i, j), this.tile.texDataId, Bushery.copy(this.tile.env))
                    }
                }
            } else {
                this.worldBuilder.world[this.tile.pos] = this.tile
            }
        }
    }

    fun render(matrix:Matrix4dStack, xo:double, yo:double) {
        Client.render.drawRectAlpha(matrix, x, y, x + width, y + height, ldu, 0.9f)
        Client.font.draw(matrix,
                         (if (worldBuilder.mode == WorldBuilderScreen.Mode.selection) "Tile: $bx, $by -> " else "") + (if (this::tile.isInitialized) tile.toString() else "null"),
                         x + 5f,
                         y + 5f,
                         0xffffffff,
                         true)
        run {
            val scStart =
                (scrollYTiles.roundToInt() / 42).coerceAtMost(this.buttonsTiles.indices.last - 5).coerceAtLeast(0)
            for ((index, button) in buttonsTiles.withIndex()) {
                button.centerY = y + 53f - scrollYTiles + index * 42f
                button.centerX = x + 75f
                button.width = 140.0
                button.height = 40.0
            }
            enableScissor(x + 5f + xo, y + 32f + yo, 140.0, height - 37f)
            for (i in scStart..(scStart + 7).coerceIn(this.buttonsTiles.indices)) {
                buttonsTiles[i].render(matrix)
            }
            disableScissor()
        }
        run {
            val scStart =
                (scrollYEnv.roundToInt() / 42).coerceAtMost(this.buttonsEnvs.indices.last - 5).coerceAtLeast(0)
            for ((index, button) in buttonsEnvs.withIndex()) {
                button.centerY = y + 53f - scrollYEnv + index * 42f
                button.centerX = x + 150f + 75f
                button.width = 140.0
                button.height = 40.0
            }
            enableScissor(x + 155f + xo, y + 32f + yo, width - 10f, height - 37f)
            for (i in scStart..(scStart + 7).coerceIn(this.buttonsEnvs.indices)) {
                buttonsEnvs[i].render(matrix)
            }
            disableScissor()
        }
        close.render(matrix)
    }

    fun onMouse(event:MouseClickedEvent) {
        if (contains(event.x.toDouble(), event.y.toDouble())) {
            close.onMouse(event)
            if (isInTileButtonArea(event.x.toDouble(), event.y.toDouble())) {
                for (it in buttonsTiles) {
                    it.onMouse(event)
                }
            } else if (isInEnvButtonArea(event.x.toDouble(), event.y.toDouble())) {
                for (it in buttonsEnvs) {
                    it.onMouse(event)
                }
            }
        }
    }

    fun contains(x:double, y:double):bool {
        return x > this.x && x < this.x + this.width && y > this.y && y < this.y + this.height
    }

    private fun isInTileButtonArea(
        x:double,
        y:double,
    ):bool {
        return x > this.x + 5f && x < this.x + 150f - 5f && y > this.y + 32f && y < this.y + this.height - 5f
    }

    private fun isInEnvButtonArea(
        x:double,
        y:double,
    ):bool {
        return x > this.x + 155f && x < this.x + this.width - 5f && y > this.y + 32f && y < this.y + height
    }

    fun onScroll(event:MouseScrolledEvent) {
        if (isInTileButtonArea(event.x.toDouble(), event.y.toDouble())) {
            scrollYTiles += event.modifier.sign * 30f
            scrollYTiles = scrollYTiles.coerceAtLeast(0f).coerceAtMost((buttonsTiles.size - 6) * 42f)
        } else if (isInEnvButtonArea(event.x.toDouble(), event.y.toDouble())) {
            scrollYEnv += event.modifier.sign * 30f
            scrollYEnv = scrollYEnv.coerceAtLeast(0f).coerceAtMost((buttonsEnvs.size - 6) * 42f)
        }
    }

}