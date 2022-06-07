@file:Suppress("DEPRECATION")

package me.ethius.shared.events

import me.ethius.shared.Log
import me.ethius.shared.bool
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.CopyOnWriteArrayList

class EventBus {

    private val listeners = CopyOnWriteArrayList<ListenerData>()
    private var logging = false

    fun register(listenerObject:Any) {
        for (method in listenerObject.javaClass.declaredMethods) {
            if (!method.isAccessible) {
                method.isAccessible = true
            }
            if (method.isAnnotationPresent(Listen::class.java)) {
                if (method.parameters.isNotEmpty()) {
                    val data = ListenerData(method, method.parameters[0].type, listenerObject)
                    listeners.add(data)
                    listeners.sortWith(Comparator.comparing { -it.annotation.priority })
                    if (logging) {
                        Log.info + "added data - " + data + Log.endl
                    }
                }
            }
        }
    }

    fun unregister(listenerObject:Any) {
        listeners.removeIf { it.parent == listenerObject }
        if (logging) {
            Log.info + "removed data - " + listenerObject + Log.endl
        }
    }

    fun dispatch(eventObject:Any) {
        val tempDataList = listeners.filter { it.event == eventObject.javaClass }
        for (data in tempDataList) {
            try {
                data.method.invoke(data.parent, eventObject)
                if (eventObject is Event && eventObject.isCancelled) {
                    break
                }
                if (logging) {
                    Log.info + "called event - " + data + Log.endl
                }
            } catch (e:IllegalAccessException) {
                Log.error + "Failed to dispatch event " + eventObject + Log.endl
                e.printStackTrace()
            } catch (e:IllegalArgumentException) {
                Log.error + "Failed to dispatch event " + eventObject + Log.endl
                e.printStackTrace()
            } catch (e:InvocationTargetException) {
                Log.error + "Failed to dispatch event " + eventObject + Log.endl
                e.printStackTrace()
            }
        }
    }

    fun setDebugLogging(flag:bool):EventBus {
        logging = flag
        return this
    }

}