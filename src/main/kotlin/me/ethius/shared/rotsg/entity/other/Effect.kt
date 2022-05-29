package me.ethius.shared.rotsg.entity.other

import me.ethius.shared.*
import me.ethius.shared.opti.TexData
import me.ethius.shared.rotsg.entity.AEntity

/**
 * @param lifetime The amount of milliseconds the effect lasts.
 * @param texData The texture data for the effect.
 * @param amplifier The amplifier of the effect.
 */
open class Effect(
    val lifetime:long,
    val texData:TexData,
    private val amplifier:int
):Tickable() {

    lateinit var entt:AEntity
    val enttInit
        get() = this::entt.isInitialized
    var initTime = System.currentTimeMillis()
    lateinit var id:string

    private val data:HashMap<string, Any> = HashMap()

    // push data to the data map //
    fun pushData(id:string, data:Any):Effect {
        // set the data //
        this.data[id] = data
        // return this //
        return this
    }

    // get data from the data map //
    fun <T> getData(id:string):T {
        // return the data, if it is null throw an exception //
        return data[id] as T ?: throw IllegalStateException("$id is not a valid identifier!")
    }

    fun hasData(id:string):bool {
        return data.containsKey(id)
    }

    open fun onAdd(entt:AEntity) {

    }

    open fun onRem(entt:AEntity) {

    }

    override fun clientTick() {
        if (System.currentTimeMillis() - initTime >= lifetime) {
            entt.removeEffect(this.id)
        }
    }

    override fun serverTick() {
        if (System.currentTimeMillis() - initTime >= lifetime) {
            entt.removeEffect(this.id)
        }
    }

    override fun release() {
        super.release()
        onRem(entt)
    }

    override fun toString():string {
        return buildString {
            append("$id|$lifetime|$amplifier|$initTime")
            for (i in data) {
                append("|${i.key}:${i.value}")
            }
        }
    }

    override fun init() {
        super.init()
        onAdd(entt)
    }

}