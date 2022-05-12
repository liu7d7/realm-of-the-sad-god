package me.ethius.client.rotsg.world

import me.ethius.client.Client
import me.ethius.client.rotsg.entity.ClientPlayer
import me.ethius.client.rotsg.overlay.TransitionOverlay
import me.ethius.client.rotsg.screen.MainMenuScreen
import me.ethius.shared.*
import me.ethius.shared.network.Packet
import me.ethius.shared.rotsg.entity.AEntity
import me.ethius.shared.rotsg.tile.Tile
import me.ethius.shared.rotsg.world.IWorld
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.concurrent.thread
import kotlin.math.ceil

class ClientWorld:IWorld, Tickable(true) {
    override val shouldTick:bool
        get() = Client.world == this

    override var name:string = ""
    override var tiles = HashMap<ivec2, Tile>()
    var tilesInView = HashSet<Tile>()
    override val entities = CopyOnWriteArrayList<AEntity>()
    var entitiesInView:ArrayList<AEntity> = ArrayList()
    var doneLoading = false
        set(value) {
            field = value
            if (value && !entities.contains(Client.player)) {
                addEntity(Client.player)
            }
        }

    fun shouldRenderEntity(entity:AEntity):bool {
        if (measuringTimeMS() - overlayTime < 500) return entity is ClientPlayer
        if (measuringTimeMS() - overlayTime > 1500) return true
        return false
    }

    override fun addTile(tile:Tile, force:bool):bool {
        val bl = super.addTile(tile, force)
        if (bl) {
            for (i in tile.pos.x - 1..tile.pos.x + 1) {
                for (j in tile.pos.y - 1..tile.pos.y + 1) {
                    tiles[ivec2(i, j)]?.updateAdjacentTiles(tileAt(ivec2(i - 1, j)),
                                                            tileAt(ivec2(i + 1, j)),
                                                            tileAt(ivec2(i, j - 1)),
                                                            tileAt(ivec2(i, j + 1)))
                }
            }
        }
        return bl
    }

    fun updateTerrain(important:bool = false) {
        if (measuringTimeMS() - overlayTime < 1000) {
            return
        }
        tilesInView.clear()
        if (tilesInView.isNotEmpty() && !important) {
            val i1 = ceil(Client.options.renderDst * 1.41421356).toInt()
            val ftp = Client.player.flooredTilePos()
            for (i in -i1..i1) {
                for (j in -i1..i1) {
                    val pos = ivec2(ftp.x + i, ftp.y + j)
                    val t = tileAt(pos) ?: continue
                    if (t.playerCanSee()) {
                        tilesInView.add(t)
                    }
                }
            }
        } else {
            synchronized(tilesInView) {
                tilesInView.addAll(tiles.values.filter { it.playerCanSee() })
            }
        }
    }

    override fun addEntity(entity:AEntity) {
        if (!contains(entity)) {
            entity.world = this
            entity.init()
            entities.add(entity)
            entity.onAdd(this)
        }
    }

    override fun remEntity(
        entity:AEntity,
        release:bool,
        fx:bool,
    ) {
        if (contains(entity)) {
            entities.remove(entity)
            if (release) {
                entity.release()
            }
            if (fx) {
                Client.fxManager.createFx(entity)
            }
            entity.world = null
        }
    }

    override fun contains(entity:AEntity):bool {
        return entities.contains(entity)
    }

    private var overlayTime = 0f

    fun clear(toMenu:bool = false) {
        Client.overlay = TransitionOverlay(2000f, false, true, 500f)
        overlayTime = measuringTimeMS()
        for (i in entities) {
            remEntity(i, i !is ClientPlayer, false)
        }
        entitiesInView.clear()
        if (toMenu) {
            thread(true) {
                Client.player.invulnerable = true
                Thread.sleep(1000)
                Client.screen = MainMenuScreen()
                Client.worldToNull()
            }
        } else {
            thread(true) {
                Client.player.invulnerable = true
                Thread.sleep(1000)
                Client.screen = null
                Client.player.r = 0.0
                tiles.clear()
                if (!Client.player.serverX.isNaN()) {
                    Client.player.moveTo(Client.player.serverX, Client.player.serverY)
                    Client.player.serverX = Double.NaN
                    Client.player.serverY = Double.NaN
                }
                Thread.sleep(1000)
                Client.player.invulnerable = false
            }
        }
    }

    override fun clientTick() {
        entities.removeIf { !Client.ticker.contains(it) }
        for (a in entitiesInView) {
            for (b in entitiesInView) {
                if (a != b && a.collidesWith(b)) {
                    b.collideWith(a)
                    a.collideWith(b)
                }
            }
        }
        if (ticksExisted % 2 == 0 && measuringTimeMS() - overlayTime > 1000) {
            val ftp = Client.player.flooredTilePos().copy()
            val arr = ArrayList<string>()
            label@
            for (i in -23..23) {
                for (j in -23..23) {
                    val pos = ftp.copy().add(i, j)
                    if (!tiles.containsKey(pos) && pos !in Client.network.requestedTiles) {
                        arr += "${i + ftp.x}|${j + ftp.y}"
                    }
                }
            }
            if (arr.isNotEmpty()) {
                Client.network.send(Packet._id_block_info_request, arr.joinToString(" "))
            }
            updateTerrain()
        }
        entitiesInView = entities.filter { it != null && it.playerCanSee() && it.alive } as ArrayList<AEntity>
    }

}