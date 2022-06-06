package me.ethius.client.rotsg.screen.worldbuilder

import me.ethius.client.Client
import me.ethius.client.ext.push
import me.ethius.client.ext.scale
import me.ethius.client.ext.translate
import me.ethius.client.renderer.Mesh
import me.ethius.client.rotsg.gui.Button
import me.ethius.client.rotsg.renderer.world.WorldRenderer.Companion.renderAtTile
import me.ethius.client.rotsg.renderer.world.WorldRenderer.Companion.renderAtTile2D
import me.ethius.client.rotsg.renderer.world.WorldRenderer.Companion.renderMesh
import me.ethius.client.rotsg.screen.Screen
import me.ethius.shared.*
import me.ethius.shared.events.def.KeyPressedEvent
import me.ethius.shared.events.def.MouseClickedEvent
import me.ethius.shared.events.def.MouseScrolledEvent
import me.ethius.shared.rotsg.data.Formatting
import me.ethius.shared.rotsg.data.WorldBuilderData
import me.ethius.shared.rotsg.tile.Bushery
import me.ethius.shared.rotsg.tile.Tile
import me.ethius.shared.rotsg.tile.TileData
import me.ethius.shared.rotsg.tile.tile_size
import me.ethius.shared.rotsg.world.addFeatureTiles
import me.ethius.shared.rotsg.world.biome.BiomeFeature
import org.joml.Matrix4dStack
import org.lwjgl.glfw.GLFW.*
import java.io.File
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.floor
import kotlin.math.min

class WorldBuilderScreen(private val prevScreen:Screen):Screen() {
    val world = HashMap<ivec2, Tile?>()
    override val doesGuiPauseGame:bool
        get() = true
    override val shouldCloseOnEsc:bool
        get() = false
    private var x = 0
    private var y = 0
    var sel1 = ivec2(0, 0)
    var sel2 = ivec2(0, 0)
    var sel1ed = false
    var sel2ed = false
    var worldMinX = 0
    var worldMinY = 0

    var tileDDM = null as WorldBuilderDropDownMenu?
    var tddmCountdown = 0f
    var name:string = "unnamed"
    var ltp = ivec2(-5000000, 50000000)

    var tileToDraw:Tile? = null

    enum class Mode {
        selection, draw, erase
    }

    var mode:Mode = Mode.selection

    private val modes:Array<Button> = Array(Mode.values().size) {
        Button()
            .setText {
                "${if (mode == Mode.values()[it]) Formatting.aqua else Formatting.white}${Mode.values()[it].name}"
            }.setOnLeft { _ ->
                mode = Mode.values()[it]
                tileToDraw = null
                sel1 = ivec2(0, 0)
                sel2 = ivec2(0, 0)
                tileDDM = null
            }.setWidth(100.0).setHeight(25.0)
    }

    fun loadFeature(biomeFeature:BiomeFeature) {
        addFeatureTiles({ world[it.pos] = it }, biomeFeature)
        worldMinX = world.keys.minByOrNull { it.x }!!.x
        worldMinY = world.keys.minByOrNull { it.y }!!.y
    }

    override fun onClose() {
        super.onClose()
        glfwSwapInterval(1)
        Client.screen = prevScreen
    }

    override fun onEnter() {
        super.onEnter()
        glfwSwapInterval(0)
    }

    override fun render(matrix:Matrix4dStack) {
        val mx = Client.mouse.x - x * tile_size
        val my = Client.mouse.y - y * tile_size
        val tilePos = ivec2(floor(mx / tile_size).toInt(), floor(my / tile_size).toInt())
        matrix.push {
            Mesh.triangles.begin()
            matrix.translate(x * tile_size, y * tile_size, 0.0) {
                for (tile in world.values) {
                    if (tile != null) {
                        tile.renderMesh(matrix)
                        if (tile.texDataId == "empty") {
                            Client.font.drawCenteredStringWithoutEnding(matrix,
                                                                        "Air",
                                                                        tile.pos.x * tile_size + tile_size / 2f,
                                                                        tile.pos.y * tile_size + tile_size / 2f,
                                                                        0xffffffff,
                                                                        true,
                                                                        0.5)
                        }
                    }
                }
                for (tile in world.values.filter { it?.env != null }) {
                    if (tile == null) {
                        continue
                    }
                    if (tile.env!!.is3d) {
                        tile.env!!.renderAtTile(tile, matrix, frand(tile.pos.hashCode()))
                        matrix.translate(1.0, 1.0, 3999.0) {
                            Client.font.drawCenteredStringWithoutEnding(matrix,
                                                                        "3d",
                                                                        tile.pos.x * tile_size + tile_size / 2f,
                                                                        tile.pos.y * tile_size + tile_size / 2f,
                                                                        0xffffffff,
                                                                        true,
                                                                        0.6)
                        }
                    } else {
                        tile.env!!.renderAtTile2D(tile, matrix, frand(tile.pos.hashCode()))
                    }
                }
                if (!(Client.mouse.x > Client.window.scaledWidth - 306 && Client.mouse.y < 27) && (tileDDM == null || !tileDDM!!.contains(
                        Client.mouse.x.toDouble(),
                        Client.mouse.y.toDouble()))) {
                    if (sel2ed && mode == Mode.selection) {
                        Client.render.drawOutlineRectWithoutEnding(matrix,
                                                                   min(sel1.x, sel2.x) * tile_size,
                                                                   min(sel1.y, sel2.y) * tile_size,
                                                                   abs(sel1.x * tile_size - sel2.x * tile_size) + tile_size,
                                                                   abs(sel1.y * tile_size - sel2.y * tile_size) + tile_size,
                                                                   withAlpha(0xffffff,
                                                                             (sin(((measuringTimeMS() * 0.2f) % 360f).toRadians()).absoluteValue * 255).toLong()),
                                                                   5.0)
                    } else {
                        Client.render.drawOutlineRectWithoutEnding(matrix,
                                                                   tilePos.x * tile_size,
                                                                   tilePos.y * tile_size,
                                                                   tile_size,
                                                                   tile_size,
                                                                   withAlpha(0xffffff,
                                                                             (sin(((measuringTimeMS() * 0.2f) % 360f).toRadians()).absoluteValue * 255).toLong()),
                                                                   5.0)
                    }
                }
            }
            matrix.push {
                matrix.scale(1.0 / Client.window.scale, 1.0 / Client.window.scale, 1.0) {
                    Client.font.drawWithoutEnding(matrix,
                                                  "XY: ${this.x}, ${this.y} | MXY: ${tilePos.x}, ${tilePos.y} | CXY: ${(Client.mouse.x / tile_size - x - worldMinX).toInt()}, ${(Client.mouse.y / tile_size - y - worldMinY).toInt()}${
                                                      if (sel2ed) {
                                                          " | WH: ${abs(sel1.x - sel2.x) + 1}, ${abs(sel1.y - sel2.y) + 1}"
                                                      } else if (mode == Mode.draw) {
                                                          " | Tile: ${tileToDraw?.toString()}"
                                                      } else ""
                                                  }",
                                                  2.0,
                                                  2.0,
                                                  0xffffffff,
                                                  true)
                }
            }
            var xPos = Client.window.scaledWidth
            for (i in modes) {
                xPos -= (i.width + 2.0)
                i.setX(xPos).setY(2.0).renderWithoutEnding(matrix)
            }
            Mesh.drawTriangles()
            tileDDM?.render(matrix, 0.0, 0.0)
        }
        if (Client.mouse.isKeyDown(GLFW_MOUSE_BUTTON_LEFT)) {
            if (mode == Mode.draw && tileToDraw != null && ltp != tilePos && tileDDM == null && measuringTimeMS() - tddmCountdown >= 1000f) {
                if (tileToDraw != world[tilePos]) {
                    world[tilePos] = Tile(tilePos, tileToDraw!!.texDataId, tileToDraw!!.env)
                    ltp = tilePos
                }
            } else if (mode == Mode.erase) {
                world[tilePos] = null
            }
        }
    }

    override fun onMouse(event:MouseClickedEvent) {
        super.onMouse(event)
        for (i in modes) {
            if (i.onMouse(event)) {
                return
            }
        }
        when (mode) {
            Mode.selection -> {
                clickSelection(event)
            }
            Mode.draw -> {
                clickDraw(event)
            }
            Mode.erase -> {
                val mx = Client.mouse.x - x * tile_size
                val my = Client.mouse.y - y * tile_size
                val tilePos = ivec2(floor(mx / tile_size).toInt(), floor(my / tile_size).toInt())
                if (tilePos in world) {
                    world[tilePos] = null
                }
            }
        }
    }

    private fun clickDraw(event:MouseClickedEvent) {
        val mx = Client.mouse.x - x * tile_size
        val my = Client.mouse.y - y * tile_size
        val tilePos = ivec2(floor(mx / tile_size).toInt(), floor(my / tile_size).toInt())
        if (event.button == GLFW_MOUSE_BUTTON_MIDDLE) {
            tileToDraw = world[tilePos]
            tileDDM = null
        }
        if ((tileToDraw == null && tileDDM == null) || event.button == GLFW_MOUSE_BUTTON_RIGHT) {
            tileDDM = WorldBuilderDropDownMenu(this, Client.window.scaledWidth - 302.0, Client.window.scaledHeight - 302.0)
            return
        }
        tileDDM?.onMouse(event)
    }

    private fun clickSelection(event:MouseClickedEvent) {
        val event1 = event.copy()
        event1.x -= x * tile_size.toFloat()
        event1.y -= y * tile_size.toFloat()
        if (event.action == GLFW_PRESS) {
            if (tileDDM == null) {
                if (sel2ed) {
                    if (isWithin(event1.x / tile_size, event1.y / tile_size, sel1, sel2)) {
                        tileDDM = WorldBuilderDropDownMenu(this, event.x.toDouble(), event.y.toDouble())
                    } else {
                        sel2ed = false
                        sel1ed = false
                    }
                } else if (!sel1ed) {
                    sel1 = ivec2(floor(event1.x / tile_size).toInt(),
                                 floor(event1.y / tile_size).toInt())
                    sel1ed = true
                }
            } else {
                tileDDM!!.onMouse(event)
            }
        } else if (event.action == GLFW_RELEASE) {
            if (sel1ed && !sel2ed) {
                sel2 = ivec2(floor(event1.x / tile_size).toInt(),
                             floor(event1.y / tile_size).toInt())
                if (sel1 != sel2) {
                    sel2ed = true
                } else {
                    sel1ed = false
                    tileDDM = WorldBuilderDropDownMenu(this, event.x.toDouble(), event.y.toDouble())
                }
            }
        }
    }

    override fun onScroll(event:MouseScrolledEvent) {
        super.onScroll(event)
        tileDDM?.onScroll(event)
    }

    override fun onKey(event:KeyPressedEvent) {
        super.onKey(event)
        if (event.action == GLFW_PRESS) {
            if (event.key == GLFW_KEY_ESCAPE) {
//                setClipboardString(this.toStr())
                saveTomlToDownloads()
                this.close()
            } else if (event.key == GLFW_KEY_B) {
                if (hasControlDown()) {
                    surroundWithBbs()
                }
            } else if (event.key == GLFW_KEY_DELETE && sel2ed) {
                for (i in safeRange(this.sel1.x, this.sel2.x)) {
                    for (j in safeRange(this.sel1.y, this.sel2.y)) {
                        this.world[ivec2(i, j)] = null
                    }
                }
            }
        }
        if (event.action != GLFW_RELEASE) {
            when (event.key) {
                GLFW_KEY_UP -> y++
                GLFW_KEY_DOWN -> y--
                GLFW_KEY_LEFT -> x++
                GLFW_KEY_RIGHT -> x--
            }
        }
    }

    private fun surroundWithBbs() {
        val bbs = ArrayList<ivec2>()
        val vals = world.values.filterNotNull().toList()
        for (i in vals) {
            if (i.texDataId != "empty") {
                if (!vals.any { it.pos.x == i.pos.x && it.pos.y == i.pos.y - 1 }) {
                    bbs.add(ivec2(i.pos.x, i.pos.y - 1))
                }
                if (!vals.any { it.pos.x == i.pos.x && it.pos.y == i.pos.y + 1 }) {
                    bbs.add(ivec2(i.pos.x, i.pos.y + 1))
                }
                if (!vals.any { it.pos.x == i.pos.x - 1 && it.pos.y == i.pos.y }) {
                    bbs.add(ivec2(i.pos.x - 1, i.pos.y))
                }
                if (!vals.any { it.pos.x == i.pos.x + 1 && it.pos.y == i.pos.y }) {
                    bbs.add(ivec2(i.pos.x + 1, i.pos.y))
                }
            }
        }
        for (i in bbs) {
            world[i] = (Tile(i, "empty", Bushery.empty_bb))
        }
    }

    private fun saveTomlToDownloads() {
        if (world.isEmpty())
            return
        val vals = world.values.filterNotNull()
        val minX = vals.minOf { it.pos.x }
        val minY = vals.minOf { it.pos.y }
        val worldBuilderData = WorldBuilderData().also {
            it.data = world.values.filterNotNull().map { TileData(it.pos.sub(minX, minY), it.texDataId, it.env?.id) }
        }
        val home = System.getProperty("user.home")
        val file = File("$home/Downloads/$name.txt")
        toml.write(worldBuilderData, file)
    }

}