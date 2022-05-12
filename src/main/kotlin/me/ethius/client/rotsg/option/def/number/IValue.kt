package me.ethius.client.rotsg.option.def.number

import com.moandjiezana.toml.Toml

class IValue(
    name:String,
    value:Int,
    description:String = "",
    range:IntRange,
    step:Int,
    visibility:() -> Boolean = { true },
    consumer:(prev:Int, input:Int) -> Int = { _, input -> input },
):NValue<Int>(name, description, value, range, step, visibility, consumer) {
    init {
        consumers.add(0) { _, it ->
            it.coerceIn(range)
        }
    }

    override fun setValue(valueIn:Double) {
        value = valueIn.toInt()
    }

    override fun write(tomlIn:MutableMap<String, Any>) {
        tomlIn[name] = value
    }

    override fun read(tomlIn:Toml) {
        tomlIn.getLong(name)?.let { value = it.toInt() }
    }
}