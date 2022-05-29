package me.ethius.client.network

import me.ethius.client.Client
import me.ethius.client.rotsg.data.ItemInfo
import me.ethius.client.rotsg.entity.Bag
import me.ethius.client.rotsg.item.Item
import me.ethius.shared.Tickable
import me.ethius.shared.int
import me.ethius.shared.ivec2
import me.ethius.shared.network.Packet
import me.ethius.shared.opti.TexData
import me.ethius.shared.rotsg.data.EffectInfo
import me.ethius.shared.rotsg.entity.AEntity
import me.ethius.shared.rotsg.entity.fromSpawnPacket
import me.ethius.shared.rotsg.tile.Bushery
import me.ethius.shared.rotsg.tile.Tile
import me.ethius.shared.string
import org.apache.commons.lang3.RandomUtils
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.concurrent.thread

class CNetworkHandler:Tickable(true, 1) {

    var addr:string = ""
    var port:int = -1
    var connected = false

    lateinit var recieveThread:Thread
    lateinit var serverView:ServerView

    val requestedTiles = CopyOnWriteArraySet<ivec2>()

    fun connect(addr:string, port:int) {
        if (connected) {
            shutdown()
        }
        Client.inGameHud.chatHud.addChat("Connected to $addr:$port")
        this.connected = true
        this.addr = addr
        this.port = port
        serverView = ServerView(addr, port)
        recieveThread = thread(true, true) {
            while (!Thread.currentThread().isInterrupted) {
                while (serverView.from.ready() && !Thread.currentThread().isInterrupted) {
                    val line = serverView.from.readLine()
                    doPacketAction(line)
                }
            }
        }
    }

    private fun doPacketAction(line:string) {
        try {
            val packet = Packet.fromString(line)
            when (packet.id) {
                Packet._id_particle -> {
                    Client.fxManager.createFx(TexData[packet.data[0]], packet.data[1].toDouble(), packet.data[2].toDouble())
                }
                Packet._id_block_info -> {
                    Client.tasksToRun.add {
                        val pos = ivec2(packet.data[0].toInt(), packet.data[1].toInt())
                        val tex = TexData[packet.data[2]]
                        val env = Bushery[packet.data[3]]
                        val tile = Tile(pos, tex, env)
                        Client.world.addTile(tile, true)
                        requestedTiles -= pos
                    }
                }
                Packet._id_world_info -> {
                    val name = packet.data[0]
                    Client.world.name = name
                    Client.world.doneLoading = true
                }
                Packet._id_spawn_entity -> {
                    Client.tasksToRun.add {
                        AEntity.fromSpawnPacket(packet)?.let {
                            Client.world.addEntity(it)
                        }
                    }
                }
                Packet._id_chat -> {
                    Client.inGameHud.chatHud.addChat(packet.data[0])
                }
                Packet._id_move -> {
                    val entityId = packet.data[0].toLong()
                    val entt = Client.world.getEntityById(entityId) ?: return
                    val x = packet.data[1].toDouble()
                    val y = packet.data[2].toDouble()
                    entt.serverX = x
                    entt.serverY = y
                }
                Packet._id_block_info_batch -> {
                    val split = packet.data[0].split(" ")
                    Client.tasksToRun.add {
                        for (i in split) {
                            val split1 = i.split("|")
                            val pos = ivec2(split1[0].toInt(), split1[1].toInt())
                            val tex = TexData[split1[2]]
                            val env = Bushery[split1[3]]
                            val tile = Tile(pos, tex, env)
                            Client.world.addTile(tile, true)
                            requestedTiles -= pos
                        }
                    }
                }
                Packet._id_exp_add -> {
                    val exp = packet.data[0].toInt()
                    val expAdd = if (Client.player.level < 20) {
                        exp.coerceAtMost(Client.player.level * 10 - 5)
                    } else {
                        exp
                    }
                    Client.player.exp += expAdd
                }
                Packet._id_bag_spawn -> {
                    val x = packet.data[0].toDouble()
                    val y = packet.data[1].toDouble()
                    val items = packet.data[2].split(" ").map { ItemInfo[it]() }.toMutableList()
                    items.sortByDescending { it.tier.ordinal }
                    if (items.isNotEmpty()) {
                        for (i in 0 until items.size step 8) {
                            val list = mutableListOf<Item>()
                            for (j in i..i + 7) {
                                if (j in items.indices) {
                                    list.add(items[j])
                                } else {
                                    break
                                }
                            }
                            Client.tasksToRun.add {
                                Client.world.addEntity(Bag(items[0].tier.bagTier, list).also {
                                    it.x = x + RandomUtils.nextDouble(0.0, 30.0) - 15.0
                                    it.y = y + RandomUtils.nextDouble(0.0, 30.0)
                                })
                            }
                        }
                    }
                }
                Packet._id_delete_entity -> {
                    val entityId = packet.data[0].toLong()
                    Client.world.getEntityById(entityId)?.let {
                        Client.world.remEntity(it, true, true)
                    }
                }
                Packet._id_hp_update -> {
                    val entityId = packet.data[0].toLong()
                    val hp = packet.data[1].toDouble()
                    val entt = Client.world.getEntityById(entityId) ?: return
                    entt.hp = hp
                }
                Packet._id_effect_add -> {
                    val entityId = packet.data[0].toLong()
                    val effectAsString = packet.data[1]
                    val effect = EffectInfo.fromString(effectAsString) ?: return

                    val entt = Client.world.getEntityById(entityId) ?: return
                    entt.addEffect(effect)
                }
            }
        } catch (e:Exception) {
            e.printStackTrace()
        }
    }

    fun shutdown() {
        if (this::recieveThread.isInitialized)
            recieveThread.interrupt()
        if (this::serverView.isInitialized) {
            send(Packet._id_logoff)
            serverView.close()
        }
        connected = false
    }

    fun send(packet:Packet) {
        if (!serverView.closed) {
            when (packet.id) {
                Packet._id_world_request -> {
                    Client.world.doneLoading = false
                    Client.world.name = packet.data[0]
                    Client.world.clear()
                }
            }
            serverView.to.write(packet.toString())
            serverView.to.newLine()
        }
    }

    fun send(id:int, vararg data:Any) {
        send(Packet(id, *data))
    }

    override fun clientTick() {
        if (serverView.socket.isClosed) {
            release()
            return
        }
        serverView.to.flush()
    }

}