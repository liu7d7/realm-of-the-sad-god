package me.ethius.client.rotsg.option

import kotlin.reflect.KProperty

abstract class AValue<T:Any> {

    abstract val value:T
    abstract val defaultValue:T
    abstract val valueClass:Class<T>
    abstract val visibility:() -> Boolean
    abstract val description:String
    abstract val name:String

    val listeners = ArrayList<() -> Unit>()
    val valueListeners = ArrayList<(prev:T, input:T) -> Unit>()
    val children = ArrayList<AValue<*>>()

    val isVisible get() = visibility()
    val isModified get() = this.value != this.defaultValue

    var opened:Boolean = false

    operator fun getValue(thisRef:Any?, property:KProperty<*>) = value

    abstract fun resetValue()

    override fun toString() = value.toString()

    override fun equals(other:Any?) =
        this === other || (other is AValue<*> && this.valueClass == other.valueClass && this.name == other.name && this.value == other.value)

    override fun hashCode() = valueClass.hashCode() * 31 + name.hashCode() * 31 + value.hashCode()

}