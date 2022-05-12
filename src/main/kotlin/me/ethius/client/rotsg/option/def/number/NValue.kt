package me.ethius.client.rotsg.option.def.number

import me.ethius.client.rotsg.option.MValue

abstract class NValue<T>(
    name:String,
    description:String = "",
    value:T,
    val range:ClosedRange<T>,
    val fine:T,
    visibility:() -> Boolean,
    consumer:(prev:T, input:T) -> T,
):MValue<T>(name, value, visibility, consumer, description) where T:Number, T:Comparable<T> {
    override fun write(tomlIn:MutableMap<String, Any>) {
        tomlIn[name] = value
    }

    abstract fun setValue(valueIn:Double)
}
