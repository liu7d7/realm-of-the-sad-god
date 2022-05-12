package me.ethius.client.rotsg.option

import me.ethius.client.rotsg.option.def.number.DValue
import me.ethius.client.rotsg.option.def.number.FValue
import me.ethius.client.rotsg.option.def.number.IValue
import me.ethius.client.rotsg.option.def.primitive.BValue
import me.ethius.client.rotsg.option.def.primitive.EValue
import me.ethius.client.rotsg.option.def.primitive.SValue
import me.ethius.client.rotsg.option.def.stupid.UValue

class VGroup(val name:String, val visible:Boolean = true):ArrayList<AValue<*>>(), MutableList<AValue<*>> {

    fun add(v:BValue):BValue {
        super.add(v)
        return v
    }

    fun <E:Enum<E>> add(v:EValue<E>):EValue<E> {
        super.add(v)
        return v
    }

    fun add(v:FValue):FValue {
        super.add(v)
        return v
    }

    fun add(v:SValue):SValue {
        super.add(v)
        return v
    }

    fun add(v:UValue):UValue {
        super.add(v)
        return v
    }

    fun add(v:IValue):IValue {
        super.add(v)
        return v
    }

    fun add(v:DValue):DValue {
        super.add(v)
        return v
    }

}