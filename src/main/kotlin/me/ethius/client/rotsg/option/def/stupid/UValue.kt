package me.ethius.client.rotsg.option.def.stupid

import com.moandjiezana.toml.Toml
import me.ethius.client.rotsg.option.MValue
import me.ethius.shared.lambda_v
import me.ethius.shared.void

class UValue(
    name:String,
    valueIn:() -> void,
    description:String,
    visibility:() -> Boolean = { true },
    consumer:(() -> void, () -> void) -> () -> void = { _:() -> void, _:() -> void -> lambda_v },
):
    MValue<() -> void>(name, valueIn, visibility, consumer, description) {
    override fun write(tomlIn:MutableMap<String, Any>) {

    }

    override fun read(tomlIn:Toml) {

    }
}