package me.ethius.client.rotsg.option.def.number

import com.moandjiezana.toml.Toml

class FValue(
    name:String,
    value:Float,
    description:String = "",
    range:ClosedFloatingPointRange<Float>,
    step:Float,
    visibility:() -> Boolean = { true },
    consumer:(prev:Float, input:Float) -> Float = { _, input -> input },
):NValue<Float>(name, description, value, range, step, visibility, consumer) {
    init {
        consumers.add(0) { _, it ->
            it.coerceIn(range)
        }
    }

    override fun setValue(valueIn:Double) {
        value = valueIn.toFloat()
    }

    override fun read(tomlIn:Toml) {
        tomlIn.getDouble(name)?.let { value = it.toFloat() }
    }
}