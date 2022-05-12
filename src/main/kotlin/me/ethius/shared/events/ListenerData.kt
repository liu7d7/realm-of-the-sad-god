package me.ethius.shared.events

import me.ethius.shared.string
import java.lang.reflect.Method

class ListenerData(val method:Method, val event:Class<*>, val parent:Any) {

    val annotation:Listen = method.getAnnotation(Listen::class.java)

    override fun toString():string {
        return """method: ${method.name} event: ${event.simpleName} parent: ${parent.javaClass.simpleName}"""
    }

}