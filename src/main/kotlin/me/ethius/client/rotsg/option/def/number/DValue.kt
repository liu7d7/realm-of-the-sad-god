package me.ethius.client.rotsg.option.def.number

import com.moandjiezana.toml.Toml

class DValue(
    name:String,
    value:Double,
    description:String = "",
    range:ClosedFloatingPointRange<Double>,
    step:Double,
    visibility:() -> Boolean = { true },
    consumer:(prev:Double, input:Double) -> Double = { _, input -> input },
):
    NValue<Double>(name, description, value, range, step, visibility, consumer) {

    init {
        consumers.add(0) { _, it ->
            it.coerceIn(range)
        }
    }

    override fun read(tomlIn:Toml) {
        tomlIn.getDouble(name)?.let { value = it }
    }

    override fun setValue(valueIn:Double) {
        value = valueIn
    }

}