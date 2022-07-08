package me.ethius.client.rotsg.screen

import me.ethius.client.Client
import me.ethius.client.ext.getDegreesQuaternion
import me.ethius.client.ext.multiply
import me.ethius.client.ext.push
import me.ethius.client.ext.translate
import me.ethius.client.rotsg.entity.ClientPlayer
import me.ethius.client.rotsg.gui.Button
import me.ethius.client.rotsg.screen.worldbuilder.WorldBuilderScreen
import me.ethius.client.rotsg.world.ClientWorld
import me.ethius.shared.*
import me.ethius.shared.events.def.MouseClickedEvent
import me.ethius.shared.ext.POSITIVE_Z
import me.ethius.shared.network.Packet
import me.ethius.shared.rotsg.data.Formatting
import me.ethius.shared.rotsg.entity.player.PlayerProfile
import org.apache.commons.io.IOUtils
import org.joml.Matrix4dStack
import org.lwjgl.glfw.GLFW.glfwSwapInterval
import java.nio.charset.Charset

class MainMenuScreen:Screen() {
    override val doesGuiPauseGame:bool
        get() = true
    override val shouldCloseOnEsc:bool
        get() = false
    private var clickTime = 0f

    private val splashText = IOUtils.readLines(Client::class.java.getResourceAsStream("/assets/data/splash_texts.txt"),
                                               Charset.defaultCharset()).random()

    lateinit var playerProfile:PlayerProfile

    private val start = Button().setOnLeft {
        this.clickTime = measuringTimeMS()
        if (!this::playerProfile.isInitialized) {
            Client.inGameHud.chatHud.addChat("${Formatting.red}Make or select a player profile first!")
            Client.screen = PlayerProfileScreen(this)
            return@setOnLeft
        }
        it.setText {
            when ((measuringTimeMS() - this.clickTime) % 1600.01f) {
                in 0f..400f -> "Loading World"
                in 400f..800f -> "Loading World."
                in 800f..1200f -> "Loading World.."
                in 1200f..1600f -> "Loading World..."
                else -> "Loading World"
            }
        }
        val left = it.left
        it.setOnLeft { }
        Client.ticker.submitTask(Client) {
            try {
                Client.network.connect(if (Client.runArgs.testing) getLocalIp() else "rotsg.ethius.us", if (Client.runArgs.testing) 9928 else 9927)
                if (this::playerProfile.isInitialized) {
                    ClientPlayer.load(playerProfile)
                } else {
                    Client.inGameHud.chatHud.addChat("${Formatting.red}Player profile not initialized! Exiting...")
                    throw IllegalStateException("Player profile not initialized!")
                }
                Client.world = ClientWorld()
                Client.network.send(Packet._id_logon, Client.player.entityId, Client.player.playerProfile.toTomlString())
                Client.network.send(Packet._id_world_request, "nexus")
                Client.tasksToRun.add { glfwSwapInterval(0) }
            } catch (e:Exception) {
                Client.network.shutdown()
                it.setOnLeft(left)
                it.setText { "Start Game" }
                Client.worldToNull()
                Log.error + "Failed to connect to the server!" + Log.endl
                Client.inGameHud.chatHud.addChat("${Formatting.red}Failed to connect to the server!")
            }
        }
    }.setText { "Start Game" }

    private val selClass = Button().setOnLeft {
        Client.screen = PlayerProfileScreen(this)
    }.setText {
        "Select Profile"
    }

    private val worldBuilder = Button().setOnLeft {
        Client.screen = WorldBuilderScreen(this)
    }.setText {
        "World Builder"
    }

    override fun render(matrix:Matrix4dStack) {
        this.renderBackground(matrix)
        // the title
        matrix.push {
            matrix.translate(Client.window.midX.toDouble(), 200.0, 1.0) {
                matrix.scale(4.0, 4.0, 1.0)
            }
            Client.font.drawCenteredString(matrix,
                                           "Realm of the Sad God",
                                           Client.window.midX.toDouble(),
                                           200.0,
                                           0xffffffff,
                                           true)
        }
        matrix.push {
            matrix.translate(Client.window.midX + 265.0, 220.0, 0.0) {
                matrix.multiply(POSITIVE_Z.getDegreesQuaternion(-20.0))
                matrix.scale(2.0 + sin(Math.toRadians(measuringTimeMS() % 360.0)) / 12.5,
                             2.0 + sin(Math.toRadians(measuringTimeMS() % 360.0)) / 12.5,
                             1.0)
            }
            // draw as yellow color
            Client.font.drawCenteredString(matrix,
                                           splashText,
                                           Client.window.midX + 265.0,
                                           220.0,
                                           0xffffd000,
                                           true)
        }
        this.start.setCenterX(Client.window.midX.toDouble())
            .setCenterY(Client.window.midY.toDouble()).setWidth(160.0)
            .setHeight(30.0)
            .render(matrix)
        this.selClass.setCenterX(Client.window.midX.toDouble())
            .setCenterY(Client.window.midY + 31.0).setWidth(160.0)
            .setHeight(30.0)
            .render(matrix)
        this.worldBuilder.setCenterX(Client.window.midX.toDouble())
            .setCenterY(Client.window.midY + 62.0).setWidth(160.0)
            .setHeight(30.0)
            .render(matrix)
    }

    override fun onMouse(event:MouseClickedEvent) {
        start.onMouse(event)
        selClass.onMouse(event)
        worldBuilder.onMouse(event)
    }

    init {
        renderLayer = ScreenRenderLayer.before
        glfwSwapInterval(1)
    }
}