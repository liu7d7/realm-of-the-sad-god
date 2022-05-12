package me.ethius.client.rotsg.option

import com.moandjiezana.toml.Toml
import kotlin.reflect.KProperty

abstract class MValue<T:Any>(
    override val name:String,
    valueIn:T,
    override val visibility:() -> Boolean,
    consumer:(prev:T, input:T) -> T,
    override val description:String,
):AValue<T>() {

    override val defaultValue = valueIn
    override var value = valueIn
        set(value) {
            if (value != field) {
                val prev = field
                var new = value

                for (index in consumers.size - 1 downTo 0) {
                    new = consumers[index](prev, new)
                }
                field = new

                valueListeners.forEach { it(prev, field) }
                listeners.forEach { it() }
            }
        }

    override val valueClass:Class<T> = valueIn.javaClass
    val consumers = arrayListOf(consumer)

    operator fun setValue(thisRef:Any?, property:KProperty<*>, value:T) {
        this.value = value
    }

    final override fun resetValue() {
        value = defaultValue
    }

    open fun read(tomlIn:Toml) {

    }

    open fun write(tomlIn:MutableMap<String, Any>) {
        tomlIn[name] = value
    }
}