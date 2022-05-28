package me.ethius.client.rotsg.gui

import me.ethius.client.Client
import me.ethius.client.rotsg.command.Command
import me.ethius.shared.double
import me.ethius.shared.events.Listen
import me.ethius.shared.events.def.KeyPressedEvent
import me.ethius.shared.events.def.KeyTypedEvent
import me.ethius.shared.int
import me.ethius.shared.maths.Animations
import me.ethius.shared.measuringTimeMS
import me.ethius.shared.network.Packet
import me.ethius.shared.rotsg.data.Formatting
import me.ethius.shared.string
import org.joml.Matrix4dStack
import org.lwjgl.glfw.GLFW.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.min

class ChatHud {

    var bottom:double = 0.0
    var top:double = 0.0
    var cc:double = 0.0
    var width:double = 0.0

    private val receivedChatHistory = CopyOnWriteArrayList<ChatHudLine>()
    private val sentChatHistory = CopyOnWriteArrayList<string>()
    private var usedChatIds = 0
    var isTyping = false
    private val textBox = TextBox().also { it.focused = true }
    var _acceptMessages = true

    fun removeChat(chat:string) {
        receivedChatHistory.removeIf { it.contents == chat }
    }

    fun removeChat(id:int) {
        receivedChatHistory.removeIf { it.id == id }
    }

    fun addChat(chat:string):int {
        if (!_acceptMessages) {
            return -1
        }
        val id = usedChatIds++
        this.receivedChatHistory.add(0, ChatHudLine(chat, id))
        return id
    }

    fun addChat(chatHudLine:ChatHudLine):int {
        if (!_acceptMessages) {
            return -1
        }
        chatHudLine.id = usedChatIds++
        this.receivedChatHistory.add(0, chatHudLine)
        return chatHudLine.id
    }

    inline fun runWithDisabledMessages(block:() -> Unit) {
        val old = _acceptMessages
        this._acceptMessages = false
        block()
        this._acceptMessages = old
    }

    private var historyIdx:int = 0

    fun render(matrix:Matrix4dStack) {
        this.bottom = Client.window.scaledHeight - padding * 2 - (if (Client.playerInit) totalBarsHeight else 0.0) - if (isTyping) 32.0 else 0.0
        this.width = 350.0
        var y = bottom
        val ch = receivedChatHistory.filter { if (isTyping) true else measuringTimeMS() < it.timeOut }
        this.cc = if (isTyping) bottom - receivedChatHistory.sumOf { it.lines.size * 22.0 }.toFloat()
        else bottom - min(
            receivedChatHistory.sumOf { if (measuringTimeMS() < it.timeOut) it.lines.size * 22.0 else 0.0 }.toFloat(),
            176f)
        label1@
        for ((idx, line) in ch.withIndex()) {
            val prevLine = if (idx > 0) ch[idx - 1] else null
            val xModulate = (if (isTyping) 0.0
            else if (measuringTimeMS() - line.timeIn <= 350) {
                (1.0 - Animations.getDecelerateAnimation(350f, measuringTimeMS() - line.timeIn)) * -width
            } else if (measuringTimeMS() - line.timeIn <= line.length - 350) {
                0.0
            } else {
                Animations.getAccelerateAnimation(350f, measuringTimeMS() - line.timeIn - (line.length - 350)) * -width
            }).also { line.xModulate = it }
            val yModulate = (if (isTyping) 0.0
            else if (prevLine != null) {
                if (measuringTimeMS() - prevLine.timeIn <= 350) {
                    (1.0 - Animations.getDecelerateAnimation(350f,
                                                             measuringTimeMS() - prevLine.timeIn)) * prevLine.height
                } else if (measuringTimeMS() - line.timeIn <= prevLine.length - 350) {
                    0.0
                } else {
                    Animations.getAccelerateAnimation(350f,
                                                      measuringTimeMS() - line.timeIn - (line.length - 350)) * line.height
                }
            } else {
                0.0
            }).also { line.yModulate = it }
            for (i in line.lines) {
                y -= 22
                if (y >= this.cc) {
                    Client.font.drawWithoutEnding(matrix, i, 5.0 + xModulate, y + 2f + yModulate, 0xffffffff, true)
                } else {
                    break@label1
                }
            }
            y += line.yModulate
            this.top = y.coerceAtMost(bottom - 22)
        }
        if (ch.isEmpty()) {
            this.top = bottom - 22
            Client.font.drawWithoutEnding(matrix, "${Formatting.gray}no chat...", 5.0, bottom - 20f, 0xffffffff, true)
        }
        if (top != bottom) {
            Client.render.drawShadowOutlineRectWithoutEnding(matrix,
                                                             0.0,
                                                             top,
                                                             width,
                                                             bottom - top,
                                                             0x40000000,
                                                             4.0,
                                                             true,
                                                             true,
                                                             true,
                                                             true)
        }
        postRender(matrix)
    }

    private fun postRender(matrix:Matrix4dStack) {
        if (this.isTyping) {
            this.textBox.posX0 = 2.0
            this.textBox.posY0 = bottom + 10
            this.textBox.posX1 = 350.0
            this.textBox.posY1 = bottom + 32
            this.textBox.render(matrix)
        }
    }

    @Listen(priority = 4)
    fun key(event:KeyPressedEvent) {
        if (event.key == GLFW_KEY_T && !isTyping) {
            if (event.action == GLFW_RELEASE) {
                this.isTyping = true
            }
        } else if (this.isTyping) {
            when (event.key) {
                GLFW_KEY_ESCAPE -> {
                    this.isTyping = false
                    this.textBox.str.clear()
                    this.historyIdx = sentChatHistory.size
                }
                GLFW_KEY_ENTER -> {
                    this.handleChat(this.textBox.str.toString())
                    this.isTyping = false
                    this.textBox.str.clear()
                }
                GLFW_KEY_UP -> {
                    if (this.historyIdx > 0 && event.action == GLFW_PRESS) {
                        this.historyIdx--
                        this.textBox.clear()
                        this.textBox.onChar(this.sentChatHistory[this.historyIdx])
                    }
                }
                GLFW_KEY_DOWN -> {
                    if (this.historyIdx < this.sentChatHistory.size && event.action == GLFW_PRESS) {
                        this.historyIdx++
                        this.textBox.clear()
                        if (this.historyIdx < sentChatHistory.size) {
                            this.textBox.onChar(this.sentChatHistory[this.historyIdx])
                        }
                    }
                }
                else -> {
                    textBox.onKey(event.key)
                }
            }
            event.cancel()
        }
    }

    @Listen(priority = 5)
    fun key(event:KeyTypedEvent) {
        if (this.isTyping) {
            textBox.onChar(event.str)
            event.cancel()
        } else if (event.str == Command.prefix) {
            this.isTyping = true
            this.textBox.clear()
            this.textBox.onChar(Command.prefix)
            event.cancel()
        }
    }

    private fun handleChat(str:string) {
        if (str.isEmpty()) {
            return
        }
        this.sentChatHistory.add(str)
        this.historyIdx = this.sentChatHistory.size
        if (Command.tryExec(str, false)) {
            return
        }
        val chatMsg = "<${if (Client.playerInit) Client.player.name else "Main"}> $str"
        this.addChat(chatMsg)
        Client.network.send(Packet._id_chat, chatMsg)
    }

    init {
        Client.events.register(this)
    }

}