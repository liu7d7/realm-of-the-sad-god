package me.ethius.server.rotsg.world

import me.ethius.shared.bool
import me.ethius.shared.int
import me.ethius.shared.string

object WorldTracker {

    val worlds = HashMap<string, ServerWorld>()
    private val ints = HashMap<string, int>()

    fun contains(id:string):bool {
        return worlds.containsKey(id)
    }

    fun get(id:string):ServerWorld {
        return worlds[id] ?: throw IllegalStateException("World $id not found")
    }

    fun rem(id:string) {
        worlds.remove(id)
    }

    fun set(className:string, world:ServerWorld):string {
        val str = "${className}_${nextInt(className)}"
        worlds[str] = world
        world.worldId = str
        return str
    }

    fun newWorld(world:() -> ServerWorld):string {
        val world = world()
        return set(world.name, world)
    }

    fun add(world:ServerWorld):string {
        return set(world.name, world)
    }

    private fun nextInt(className:string):int {
        ints.putIfAbsent(className, 0)
        check(ints[className] != null) { "computeIfAbsent somehow failed." }
        return ints[className]!!.also { ints[className] = it + 1 }
    }

}