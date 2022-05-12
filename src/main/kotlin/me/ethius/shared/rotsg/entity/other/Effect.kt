package me.ethius.shared.rotsg.entity.other

import me.ethius.shared.*
import me.ethius.shared.opti.TexData
import me.ethius.shared.rotsg.entity.AEntity

open class Effect(
    val lifetime:long,
    val texData:TexData,
    val onAdd:(AEntity, Effect) -> void,
    val onRem:(AEntity, Effect) -> void,
):Tickable() {

    lateinit var entt:AEntity
    var initTime = measuringTimeMS()
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

    override fun clientTick() {
        if (measuringTimeMS() - initTime >= lifetime) {
            entt.removeEffect(this.id)
        }
    }

    override fun release() {
        super.release()
        onRem(entt, this)
    }

    override fun init() {
        super.init()
        initTime = measuringTimeMS()
        onAdd(entt, this)
    }

}