package me.ethius.client.rotsg.option.def.primitive

import com.moandjiezana.toml.Toml
import me.ethius.client.rotsg.option.MValue

class SValue(
    override var name:String,
    value:String,
    description:String = "",
    visibility:() -> Boolean = { true },
    consumer:(prev:String, input:String) -> String = { _, input -> input },
):MValue<String>(name, value, visibility, consumer, description) {
    var typing:Boolean = false
    override fun read(tomlIn:Toml) {
        tomlIn.getString(name)?.let { value = it }
    }
}