package me.ethius.client.rotsg.connections

import club.minnced.discord.rpc.DiscordEventHandlers
import club.minnced.discord.rpc.DiscordRichPresence
import me.ethius.client.Client
import me.ethius.client.rotsg.screen.MainMenuScreen
import me.ethius.client.rotsg.screen.PlayerProfileScreen
import me.ethius.client.rotsg.screen.worldbuilder.WorldBuilderScreen
import me.ethius.shared.rotsg.entity.player.PlayerClass

typealias Discord = club.minnced.discord.rpc.DiscordRPC

class DiscordRPC {

    private val inst = Discord.INSTANCE
    private val rp = DiscordRichPresence()
    lateinit var thread:Thread

    fun startup() {
        return
        val discEventHandlers = DiscordEventHandlers()
        val id = "922663266830929951"
        this.inst.Discord_Initialize(id, discEventHandlers, true, "")
        this.rp.startTimestamp = System.currentTimeMillis() / 1000L
        this.inst.Discord_UpdatePresence(this.rp)
        this.thread = Thread {
            while (!Thread.currentThread().isInterrupted) {
                try {
                    if (!Client.playerInit) {
                        when (Client.screen) {
                            is PlayerProfileScreen -> {
                                this.rp.state = "Selecting a Profile"
                            }
                            is MainMenuScreen -> {
                                this.rp.state = "In Main Menu"
                            }
                            is WorldBuilderScreen -> {
                                this.rp.state = "In World Builder"
                            }
                        }
                    } else {
                        this.rp.state = "In Game"
                        when (Client.player.pClass) {
                            PlayerClass.archer -> {
                                this.rp.largeImageKey = "screenshot_2021-12-20_174133"
                                this.rp.details = "Playing Archer"
                            }
                            PlayerClass.warrior -> {
                                this.rp.largeImageKey = "screenshot_2021-12-20_174120"
                                this.rp.details = "Playing Warrior"
                            }
                            PlayerClass.ninja -> {
                                this.rp.largeImageKey = "screenshot_2021-12-20_173716"
                                this.rp.details = "Playing Ninja"
                            }
                            PlayerClass.dasher -> {
                                this.rp.largeImageKey = "screenshot_2022-04-03_214542"
                                this.rp.details = "Playing Dasher"
                            }
                        }
                    }
                    this.inst.Discord_UpdatePresence(this.rp)
                } catch (e:Exception) {
                    e.printStackTrace()
                }
                try {
                    Thread.sleep(1000)
                } catch (_:InterruptedException) {

                }
            }
        }.also {
            it.name = "RPC-Callback-Handler"
            it.start()
        }
    }

    fun shutdown() {
        return
        this.inst.Discord_Shutdown()
        this.thread.interrupt()
    }

}