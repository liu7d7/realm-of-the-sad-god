package me.ethius.client.renderer

import me.ethius.client.Client
import me.ethius.client.iden_m4d
import me.ethius.shared.bool
import org.joml.Matrix4d
import java.util.*

class RenderTaskTracker {

    var layer = RenderLayer.tile
    private val map = EnumMap<RenderLayer, ArrayList<() -> Any>>(RenderLayer::class.java).also {
        for (i in RenderLayer.values()) {
            if (i == RenderLayer.ignored) {
                continue
            }
            it[i] = ArrayList()
        }
    }
    val threeD = Framebuffer(true)
    val outlines = Framebuffer(true)
    val shadows = Framebuffer(true)
    var lookAt:Matrix4d = iden_m4d

    var hasWorld3d:bool = false

    fun layer(layer:RenderLayer) {
        this.layer = layer
    }

    fun onLayer(layer:RenderLayer, task:() -> Unit) {
        val prevLayer = this.layer
        this.layer = layer
        this.track(task)
        this.layer = prevLayer
    }

    fun track(action:() -> Any) {
        if (layer == RenderLayer.ignored) return
        map[layer]!! += action

        hasWorld3d = hasWorld3d || layer == RenderLayer.world_feature_3d
    }

    fun render() {
        if (Client.lookAtInit) {
            lookAt = Client.lookAt
        }
        if (Client.worldInit) {
            for (i in RenderLayer.notIgnoredWorld) {
                if (map[i]!!.isEmpty()) {
                    continue
                }
                layer(i)
                if (!layer.beEachTime) {
                    layer.begin()
                }
                for (task in map[i]!!) {
                    if (layer.beEachTime) {
                        layer.begin()
                    }
                    task()
                    if (layer.beEachTime) {
                        layer.end()
                    }
                }
                if (!layer.beEachTime) {
                    layer.end()
                }
                map[i]?.clear()
            }
        }
        lookAt = iden_m4d
        for (i in RenderLayer.notIgnoredHud) {
            if (map[i]!!.isEmpty()) {
                continue
            }
            layer(i)
            i.begin()
            for (task in map[i]!!) {
                task()
            }
            i.end()
            map[i]?.clear()
        }
        hasWorld3d = false
    }

}