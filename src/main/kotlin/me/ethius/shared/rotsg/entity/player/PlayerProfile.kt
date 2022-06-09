package me.ethius.shared.rotsg.entity.player

import com.moandjiezana.toml.Toml
import me.ethius.client.rotsg.entity.ClientPlayer
import me.ethius.client.rotsg.inventory.Inventory
import me.ethius.server.rotsg.entity.ServerPlayer
import me.ethius.shared.*
import java.io.File

class PlayerProfile {

    var clazz:string = ""
    var stats:List<int> = emptyList()
    var items:List<string> = emptyList()
    var exp:int = 0
    var skin:int = 0
    var name:string = ""

    constructor()

    constructor(
        name:string,
        clazz:PlayerClass,
        stats:List<int>,
        items:Inventory,
        exp:int,
        skin:int,
    ) {
        this.name = name
        this.clazz = clazz.name
        this.stats = stats
        this.items = items.slots.map { it.item.id }
        this.exp = exp
        this.skin = skin
    }

    fun set(player:ClientPlayer, name:string = player.name) {
        this.clazz = player.pClass.name
        this.skin = player.selectedTexData
        this.stats = listOf(player.lifeMaxed,
                            player.manaMaxed,
                            player.atkMaxed,
                            player.defMaxed,
                            player.spdMaxed,
                            player.dexMaxed,
                            player.vitMaxed,
                            player.wisMaxed)
        this.items = player.inventory.slots.map { it.item.id }
        this.exp = player.exp
        this.name = name
    }

    fun set(player:ServerPlayer, name:string = player.name) {
        this.clazz = player.pClass.name
        this.skin = player.selectedTexData
        this.stats = listOf(player.lifeMaxed,
                            player.manaMaxed,
                            player.atkMaxed,
                            player.defMaxed,
                            player.spdMaxed,
                            player.dexMaxed,
                            player.vitMaxed,
                            player.wisMaxed)
        this.items = player.itemIds.toList()
        this.exp = player.exp
        this.name = name
    }

    fun write() {
        val file = File("saves")
        file.mkdirs()
        toml.write(this, File("saves/$name.dat"))
    }

    fun toTomlString():string {
        return toml.write(this)
    }

    companion object {
        fun read(location:string, file:bool = true):PlayerProfile {
            return if (file) {
                val file = File(location)
                Toml().readCached(file).to(PlayerProfile::class.java)
            } else {
                Toml().read(location).to(PlayerProfile::class.java)
            }
        }
    }

}