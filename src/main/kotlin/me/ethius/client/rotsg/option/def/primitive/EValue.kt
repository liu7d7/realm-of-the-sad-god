package me.ethius.client.rotsg.option.def.primitive

import com.moandjiezana.toml.Toml
import me.ethius.client.rotsg.option.MValue

class EValue<T:Enum<T>>(
    name:String,
    value:T,
    description:String = "",
    visibility:() -> Boolean = { true },
    consumer:(prev:T, input:T) -> T = { _, input -> input },
):MValue<T>(name, value, visibility, consumer, description) {
    val enumClass:Class<T> = value.declaringClass
    val enumValues:Array<out T> = enumClass.enumConstants

    fun nextValue() {
        value = value.declaringClass.enumConstants[(value.ordinal + 1) % enumClass.enumConstants.size]
    }

    fun setValue(va:String) {
        enumValues.firstOrNull { it.name.equals(va, true) }?.let {
            value = it
        }
    }

    override fun read(tomlIn:Toml) {
        tomlIn.getString(name)?.let { element ->
            enumValues.firstOrNull { it.name.equals(element, true) }?.let {
                value = it
            }
        }
    }

    override fun write(tomlIn:MutableMap<String, Any>) {
        tomlIn[name] = value.name
    }
}