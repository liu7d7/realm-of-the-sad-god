package me.ethius.shared.rotsg.tile

import me.ethius.shared.string

class ThreeDWall:Bushery("sand_1", 0.0, 1.0, 0.0, BoundingBoxType.none) {

    override var is3d = true
    override var boundingBoxType = BoundingBoxType.wall

    lateinit var renderData:RenderData

    override fun copy():Bushery {
        return this
    }

    data class RenderData(val left:string, val right:string, val front:string, val back:string, val top:string)

}