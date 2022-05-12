package me.ethius.client.rotsg.option

import me.ethius.client.rotsg.option.def.number.DValue
import me.ethius.client.rotsg.option.def.number.FValue
import me.ethius.client.rotsg.option.def.number.IValue
import me.ethius.client.rotsg.option.def.primitive.BValue
import me.ethius.client.rotsg.option.def.primitive.EValue
import me.ethius.client.rotsg.option.def.primitive.SValue
import me.ethius.shared.string

class Options {

    private val options = HashMap<OptionCategory, MutableList<AValue<*>>>()

    var renderDst by this.addOption(OptionCategory.render,
                                    IValue("Render Distance",
                                           21, "The distance to render blocks.",
                                           1..35,
                                           1))
    var scale by this.addOption(OptionCategory.render,
                                FValue("Scale",
                                       0.9f, "The scale of the game.",
                                       0.1f..2f,
                                       0.1f))
    var debug by this.addOption(OptionCategory.debug,
                                BValue("Debug",
                                       false, "Whether to show debug information."))

    private fun addOption(category:OptionCategory, option:IValue):IValue {
        options.computeIfAbsent(category) { ArrayList() }
        options[category]!!.add(option)
        return option
    }

    private fun addOption(category:OptionCategory, option:FValue):FValue {
        options.computeIfAbsent(category) { ArrayList() }
        options[category]!!.add(option)
        return option
    }

    private fun addOption(category:OptionCategory, option:DValue):DValue {
        options.computeIfAbsent(category) { ArrayList() }
        options[category]!!.add(option)
        return option
    }

    private fun addOption(category:OptionCategory, option:BValue):BValue {
        options.computeIfAbsent(category) { ArrayList() }
        options[category]!!.add(option)
        return option
    }

    private fun addOption(category:OptionCategory, option:EValue<*>):EValue<*> {
        options.computeIfAbsent(category) { ArrayList() }
        options[category]!!.add(option)
        return option
    }

    private fun addOption(category:OptionCategory, option:SValue):SValue {
        options.computeIfAbsent(category) { ArrayList() }
        options[category]!!.add(option)
        return option
    }

    fun getOptionByName(name:string):AValue<*>? {
        return options.values.flatten().find { it.name == name }
    }

    fun getOptionsByCategory(category:OptionCategory):List<AValue<*>> {
        options.computeIfAbsent(category) { ArrayList() }
        return options[category]!!
    }
}

enum class OptionCategory {

    render, game, debug, misc

}