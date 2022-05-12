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
        val str:string
        worlds["${className}_${nextInt(className)}".also { str = it }] = world
        world.worldId = str
        return str
    }

    fun newWorld(world:() -> ServerWorld):string {
        val str:string
        val world = world()
        set(world.name, world).also { str = it }
        return str
    }

    fun add(world:ServerWorld):string {
        return set(world.name, world)
    }

    fun newWorld_PWS(world:() -> ServerWorld):Pair<ServerWorld, string> {
        val str:string
        val wrld = world()
        set(wrld.name, wrld).also { str = it }
        return Pair(wrld, str)
    }

    private fun nextInt(className:string):int {
        ints.computeIfAbsent(className) { 0 }
        check(ints[className] != null) { "computeIfAbsent somehow failed." }
        return ints[className]!!.also { ints[className] = it + 1 }
    }

}