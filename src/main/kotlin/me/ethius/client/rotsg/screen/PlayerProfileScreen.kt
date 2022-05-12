package me.ethius.client.rotsg.screen

import me.ethius.client.Client
import me.ethius.client.Profiles
import me.ethius.client.ext.push
import me.ethius.client.ext.translate
import me.ethius.client.renderer.Mesh
import me.ethius.client.rotsg.gui.Button
import me.ethius.client.rotsg.gui.TextBox
import me.ethius.client.rotsg.gui.dul
import me.ethius.client.rotsg.inventory.Inventory
import me.ethius.shared.bool
import me.ethius.shared.events.def.KeyPressedEvent
import me.ethius.shared.events.def.KeyTypedEvent
import me.ethius.shared.events.def.MouseClickedEvent
import me.ethius.shared.int
import me.ethius.shared.rotsg.data.Formatting
import me.ethius.shared.rotsg.entity.player.PlayerClass
import me.ethius.shared.rotsg.entity.player.PlayerProfile
import org.joml.Matrix4dStack
import java.util.concurrent.CopyOnWriteArrayList

private const val xOff = 10.0

class PlayerProfileScreen(private val prevScreen:MainMenuScreen):Screen() {

    override val doesGuiPauseGame:bool
        get() = true
    override val shouldCloseOnEsc:bool
        get() = true

    private var profileSubScreen:Button? = null

    init {
        renderLayer = ScreenRenderLayer.before
    }

    override fun onClose() {
        Client.screen = prevScreen
    }

    private val buttons = CopyOnWriteArrayList(Profiles.profiles.map { i ->
        buttonFromProf(i)
    })

    private val buttonAdd = Button().setText { "Add Profile" }.setOnLeft {
        this.profileSubScreen = Button().setText { "" }.setAdditionalRendering { matrix, button ->
            var yOffset = 3f
            Client.font.drawCenteredString(matrix, "Add Profile", button.centerX, button.minY + 10, 0xffffffff, true)
            yOffset += 20

            Client.render.drawRectAlpha(matrix,
                                        button.minX + 5,
                                        button.minY + yOffset,
                                        button.maxX - 5,
                                        button.minY + yOffset + 20,
                                        0x000000,
                                        0.4f)
            Client.font.draw(matrix, "Class: ", button.minX + 6, button.minY + yOffset + 1, 0xffffffff, true)
            Client.font.drawLeft(matrix,
                                 button.getData<PlayerClass>("Class").displayName,
                                 button.maxX - 6,
                                 button.minY + yOffset + 1,
                                 0xffffffff,
                                 true)
            yOffset += 22

            Client.render.drawRectAlpha(matrix,
                                        button.minX + 5,
                                        button.minY + yOffset,
                                        button.maxX - 5,
                                        button.minY + yOffset + 20,
                                        0x000000,
                                        0.4f)
            Client.font.draw(matrix, "Skin: ", button.minX + 6, button.minY + yOffset + 1, 0xffffffff, true)
            Client.font.drawLeft(matrix,
                                 button.getData<PlayerClass>("Class").skins[button.getData("SkinIndex")].id,
                                 button.maxX - 6,
                                 button.minY + yOffset + 1,
                                 0xffffffff,
                                 true)
            yOffset += 22

            Client.font.drawCenteredString(matrix, "Name: ", button.centerX, button.minY + yOffset + 12, 0xffffffff, true)
            yOffset += 22

            val nameBox = button.getData<TextBox>("NameBox")
            nameBox.posX0 = button.minX + 6.0
            nameBox.posX1 = button.maxX - 6.0
            nameBox.posY0 = button.minY + yOffset
            nameBox.posY1 = button.minY + yOffset + 20
            Mesh.triangles.begin()
            nameBox.render(matrix)
            nameBox.doRender()
            yOffset += 22

            Client.render.drawRectAlpha(matrix,
                                        button.centerX - 40,
                                        button.minY + yOffset,
                                        button.centerX + 40,
                                        button.minY + yOffset + 20,
                                        0x000000,
                                        0.4f)
            Client.font.drawCenteredString(matrix, "Done!", button.centerX, button.minY + yOffset + 9, 0xffffffff, true)
        }.setOnLeft { button ->
            val mx = Client.mouse.x
            val my = Client.mouse.y
            val nameBox = button.getData<TextBox>("NameBox")
            nameBox.focused = nameBox.containsMouse(mx, my)
            if (mx > button.minX + 6 && mx < button.maxX - 6 && my > button.minY + 20 && my < button.minY + 40) {
                button.pushData("Class", PlayerClass.values()[(button.getData<PlayerClass>("Class").ordinal + 1) % PlayerClass.values().size])
                button.pushData("SkinIndex", (button.getData<int>("SkinIndex") + 1) % PlayerClass.values()[button.getData<PlayerClass>("Class").ordinal].skins.size)
            }
            if (mx > button.minX + 6 && mx < button.maxX - 6 && my > button.minY + 42 && my < button.minY + 62) {
                button.pushData("SkinIndex", (button.getData<int>("SkinIndex") + 1) % PlayerClass.values()[button.getData<PlayerClass>("Class").ordinal].skins.size)
            }
            if (mx > button.centerX - 40 && mx < button.centerX + 40 && my > button.minY + 112 && my < button.minY + 132) {
                if (nameBox.str.isNotEmpty()) {
                    val pClass = button.getData<PlayerClass>("Class")
                    val inventory = Inventory(true)
                    pClass.initInventory(inventory)
                    val playerProfile = PlayerProfile(nameBox.str.trim().toString(),
                                                      pClass,
                                                      listOf(150, 100, 15, 0, 10, 12, 12, 12),
                                                      inventory,
                                                      0,
                                                      button.getData("SkinIndex"))
                    Profiles.profiles.add(playerProfile)
                    buttons.add(buttonFromProf(playerProfile))
                    Client.inGameHud.chatHud.addChat("Added profile: ${playerProfile.name}")
                    profileSubScreen = null
                } else {
                    Client.inGameHud.chatHud.addChat("${Formatting.red}Pleaes enter a name!")
                }
            }
        }.setTextAction { text, button ->
            val nameBox = button.getData<TextBox>("NameBox")
            nameBox.onChar(text)
        }.setKeyAction { ev, button ->
            val nameBox = button.getData<TextBox>("NameBox")
            nameBox.onKey(ev.key)
        }.also {
            it.pushData("Class", PlayerClass.ninja)
            it.pushData("NameBox", TextBox())
            it.pushData("SkinIndex", 0)
        }
    }

    private fun buttonFromProf(i:PlayerProfile):Button {
        val pClass = PlayerClass.valueOf(i.clazz)
        return Button().setText {
            i.name
        }.setTextAlignment(Button.TextAlignment.right).setOnLeft {
            prevScreen.playerProfile = i
            Client.inGameHud.chatHud.addChat("${Formatting.gold}${i.name}${Formatting.reset} is now ${Formatting.green}selected${Formatting.reset} as your profile.")
        }.setAdditionalRendering { matrix, button ->
            matrix.push {
                matrix.translate(button.minX + 15 + pClass.skins[i.skin].down.width / 2f,
                                 button.minY + 15 + pClass.skins[i.skin].down.height / 2f,
                                 0.0) {
                    matrix.scale(2.5, 2.5, 1.0)
                }
                Client.render.drawTex(pClass.skins[i.skin].down, matrix, button.minX + 15, button.minY + 15)
            }
        }.setOnRight {
            buttons.remove(it)
            Profiles.profiles.remove(i)
        }
    }

    override fun render(matrix:Matrix4dStack) {
        renderBackground(matrix)
        renderAllProfiles(matrix)
        buttonAdd.setWidth(100.0).setHeight(30.0).setY(5.0).setX(Client.window.scaledWidth - 110.0).render(matrix)
        if (profileSubScreen != null) {
            Client.render.drawRectAlpha(matrix,
                                        0.0,
                                        0.0,
                                        Client.window.scaledWidth.toDouble(),
                                        Client.window.scaledHeight.toDouble(),
                                        dul,
                                        0.5f)
            profileSubScreen!!.setWidth(300.0).setHeight(134.0).setCenterX(Client.window.midX.toDouble())
                .setCenterY(Client.window.midY.toDouble())
                .render(matrix)
        }
    }

    override fun onChar(event:KeyTypedEvent) {
        super.onChar(event)
        if (profileSubScreen != null) {
            profileSubScreen!!.onChars(event)
            event.cancel()
            return
        }
    }

    override fun onKey(event:KeyPressedEvent) {
        super.onKey(event)
        if (profileSubScreen != null) {
            profileSubScreen!!.onKey(event)
            event.cancel()
            return
        }
    }

    override fun onMouse(event:MouseClickedEvent) {
        super.onMouse(event)
        if (profileSubScreen != null) {
            profileSubScreen!!.onMouse(event)
            return
        }
        buttonAdd.onMouse(event)
        for (button in buttons) {
            button.onMouse(event)
        }
    }

    private fun renderAllProfiles(matrix:Matrix4dStack) {
        for ((idx, it) in buttons.withIndex()) {
            val yOff = idx * 50.0 + 40.0
            it.setWidth(Client.window.scaledWidth - 20.0).setHeight(40.0).setX(xOff).setY(yOff).render(matrix)
        }
    }

}