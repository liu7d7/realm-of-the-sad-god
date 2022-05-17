package me.ethius.server.network

import com.google.common.collect.Queues
import com.google.common.collect.Sets
import me.ethius.server.Server
import me.ethius.server.rotsg.entity.ServerPlayer
import me.ethius.server.rotsg.world.Nexus
import me.ethius.server.rotsg.world.Realm
import me.ethius.server.rotsg.world.ServerWorld
import me.ethius.server.rotsg.world.WorldTracker
import me.ethius.shared.*
import me.ethius.shared.network.Packet
import me.ethius.shared.rotsg.entity.AEntity
import me.ethius.shared.rotsg.entity.enemy.Enemy
import me.ethius.shared.rotsg.entity.getWorldNameFromSpawnPacket
import me.ethius.shared.rotsg.entity.player.PlayerProfile
import me.ethius.shared.rotsg.tile.tile_size
import java.io.BufferedReader
import java.io.BufferedWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import kotlin.concurrent.thread

class SNetworkHandler {

    private var port = 25565
    private lateinit var addr:string
    private val clients:MutableSet<ClientView> = Sets.newConcurrentHashSet() // player associated or not
    val players:MutableSet<ServerPlayer> = Sets.newConcurrentHashSet() // player associated

    lateinit var serverSocket:ServerSocket
    lateinit var acceptThread:Thread

    private val receivedPackets = Queues.newConcurrentLinkedQueue<QueuedPacket>()

    fun start(addr:string = this.addr, port:int = this.port) {
        this.addr = addr
        this.port = port
        this.serverSocket = ServerSocket(port)
        acceptThread = thread(true, false) {
            while (!Thread.currentThread().isInterrupted) {
                try {
                    val socket = serverSocket.accept()
                    socket.tcpNoDelay = true
                    clients.add(ClientView(socket))
                } catch (e:Exception) {
                    e.printStackTrace()
                }
            }
        }
        println("Started server on $addr:$port")
    }

    private fun doPacketAction(queuedPacket:QueuedPacket) {
        try {
            val packet = queuedPacket.packet
            val client = queuedPacket.client
            when (packet.id) {
                Packet._id_logon -> {
                    this.players.add(ServerPlayer(client, PlayerProfile.read(packet.data[1], false), packet.data[0].toLong()))
                    println("${client.id} logged in as ${ServerPlayer[client]?.name}")
                }
                Packet._id_logoff -> {
                    val pl = ServerPlayer[client] ?: return
                    this.players.remove(pl)
                    this.clients.remove(client)
                    client.close()
                    for (i in WorldTracker.worlds.values) {
                        i.remEntity(pl)
                    }
                }
                Packet._id_chat -> {
                    val world = (ServerPlayer[client]?.world as? ServerWorld) ?: return
                    for (i in world.players) {
                        if (i.client != client) {
                            send(i.client, packet)
                        }
                    }
                }
                Packet._id_world_request -> {
                    val world = when (packet.data[0]) {
                        "nexus" -> {
                            WorldTracker.get(Nexus.worldId)
                        }
                        "realm" -> {
                            WorldTracker.get(Realm.worldId)
                        }
                        else -> {
                            try {
                                WorldTracker.get(packet.data[0])
                            } catch (e:Exception) {
                                WorldTracker.get(Nexus.worldId)
                            }
                        }
                    }
                    val entity = ServerPlayer[client]
                    if (entity != null) {
                        entity.world?.remEntity(entity)
                        world.addEntity(entity)
                        send(client, Packet._id_world_info, world.worldId)
                        send(client, Packet._id_move, entity.entityId, world.spawnPosition.x * tile_size, world.spawnPosition.y * tile_size)
                    }
                }
                Packet._id_spawn_entity -> {
                    val w = WorldTracker.get(AEntity.getWorldNameFromSpawnPacket(packet))
                    for (i in w.players) {
                        if (i.client != client) {
                            send(i.client, packet)
                        }
                    }
                }
                Packet._id_move -> {
                    ServerPlayer[client]?.moveTo(packet.data[0].toDouble(), packet.data[1].toDouble())
                }
                Packet._id_block_info_request -> {
                    val split = packet.data[0].split(" ")
                    val world = ServerPlayer[client]?.world ?: return
                    val str = StringBuilder()
                    for (i in split) {
                        val split1 = i.split("|")
                        val x = split1[0].toInt()
                        val y = split1[1].toInt()

                        val block = world.tileAt(ivec2(x, y)) ?: continue
                        str.append(block.pos.x).append("|").append(block.pos.y).append("|").append(block.texDataId).append("|").append(block.env?.id ?: "NIL").append(" ")
                    }
                    if (str.isNotBlank()) {
                        str.deleteCharAt(str.lastIndexOf(" "))
                        send(client, Packet._id_block_info_batch, str.toString())
                    }
                }
                Packet._id_damage_entity -> {
                    val dsid = packet.data[0].toLong()
                    val dtid = packet.data[1].toLong()
                    for (i in WorldTracker.worlds.values) {
                        val damageSource = i.getEntityById(dsid)
                        val damageTarget = i.getEntityById(dtid)
                        if (dsid == -1L && damageTarget != null) {
                            damageTarget.hp -= packet.data[2].toDouble()
                            if (damageTarget.hp < 0) {
                                damageTarget.world?.remEntity(damageTarget, true, true)
                            }
                            broadcastIf(Packet(Packet._id_hp_update, damageTarget.entityId, damageTarget.hp)) {
                                ServerPlayer[it]?.world == damageTarget.world && it != client
                            }
                        } else if (damageSource != null && damageTarget != null) {
                            damageTarget.hp -= packet.data[2].toDouble()
                            if (damageTarget is Enemy) {
                                damageTarget.damagerIds += dsid
                            }
                            if (damageTarget.hp < 0) {
                                damageTarget.world?.remEntity(damageTarget, true, true)
                            }
                            broadcastIf(Packet(Packet._id_hp_update, damageTarget.entityId, damageTarget.hp)) {
                                ServerPlayer[it]?.world == damageTarget.world && it != client
                            }
                            break
                        }
                    }
                }
                Packet._id_hp_update -> {
                    val entity = ServerPlayer[client]
                    if (entity != null) {
                        entity.hp = packet.data[0].toDouble()
                    }
                }
            }
        } catch (_:Exception) {

        }
    }
    
    fun actOnQueuedPackets() {
        var queuedPacket:QueuedPacket?
        while (receivedPackets.poll().also { queuedPacket = it } != null) {
            queuedPacket?.let { doPacketAction(it) }
        }
    }

    fun broadcast(id:int, vararg data:Any) {
        broadcastIf(Packet(id, *data))
    }

    inline fun broadcastIf(packet:Packet, condition:(ClientView) -> bool = { true }) {
        for (player in this.players) {
            if (condition(player.client)) {
                send(player.client, packet)
            }
        }
    }

    fun send(client:ClientView, packet:Packet) {
        val str = packet.toString()
        try {
            client.to.write(str)
            client.to.newLine()
            client.writes++
        } catch (_:Exception) {
            client.close()
        }
    }

    fun send(client:ClientView, id:int, vararg data:Any) {
        val packet = Packet(id, *data)
        send(client, packet)
    }

    fun flush() {
        for (client in this.clients) {
            if (client.writes <= 0) {
                continue
            }
            if (client.closed) {
                continue
            }
            try {
                client.to.flush()
            } catch (_:Exception) {
                client.close()
            }
        }
    }

    fun reset() {
        this.clients.removeIf { it.closed }
        this.players.removeIf { !clients.contains(it.client) }
        for (i in WorldTracker.worlds.values) {
            for (j in ServerPlayer.clientToPlayer.keys) {
                if (!j.closed) {
                    continue
                }
                val sp = ServerPlayer[j] ?: continue
                i.remEntity(sp)
            }
        }
        for (client in this.clients) {
            client.writes = 0
        }
    }

    fun shutdown() {
        acceptThread.interrupt()
        serverSocket.close()
    }

    fun getPlayerById(id:long):ServerPlayer? {
        return players.find { it.entityId == id }
    }

    private class QueuedPacket(val client:ClientView, val packet:Packet)

    data class ClientView(val socket:Socket,
                          val to:BufferedWriter = socket.outputStream.bufferedWriter(),
                          val from:BufferedReader = socket.inputStream.bufferedReader(),
                          var writes:int = 0) {

        var closed:bool = false
            private set

        val id = getId()
        private val receiveThread = thread(true, false) {
            while (!Thread.currentThread().isInterrupted) {
                try {
                    while (from.ready()) {
                        val line = from.readLine()
                        Server.network.receivedPackets.add(QueuedPacket(this@ClientView, Packet.fromString(line)))
                    }
                } catch (e:Exception) {
                    e.printStackTrace()
                    close()
                }
            }
        }

        override fun hashCode():Int {
            return id.toInt()
        }

        fun close() {
            if (closed) {
                return
            }
            println("Client $id disconnected.")
            ServerPlayer.clientToPlayer -= this
            socket.close()
            receiveThread.interrupt()
            closed = true
        }

        override fun equals(other:Any?):Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ClientView

            if (id != other.id) return false

            return true
        }

        companion object {
            fun getId():long {
                return UUID.randomUUID().mostSignificantBits
            }
        }
    }
    
}