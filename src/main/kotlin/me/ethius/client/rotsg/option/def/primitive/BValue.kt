package me.ethius.client.rotsg.option.def.primitive

import com.moandjiezana.toml.Toml
import me.ethius.client.rotsg.option.MValue

open class BValue(
    name:String,
    value:Boolean,
    description:String = "",
    visibility:() -> Boolean = { true },
    consumer:(prev:Boolean, input:Boolean) -> Boolean = { _, input -> input },
):MValue<Boolean>(name, value, visibility, consumer, description) {

    override fun write(tomlIn:MutableMap<String, Any>) {
        tomlIn[name] = value
    }

    override fun read(tomlIn:Toml) {
        tomlIn.getBoolean(name)?.let { value = it }
    }

}