package me.ethius.shared.rotsg.tile

import me.ethius.shared.bool
import me.ethius.shared.double
import me.ethius.shared.string

class GeneralThreeDBushery:Bushery {

    val modelIds:Array<string>
    val textures:Array<string>
    override var boundingBoxType:BoundingBoxType
    var rotation:double = 0.0

    constructor(model:string, texData:string, boundingBoxType:BoundingBoxType):super(texData) {
        this.modelIds = arrayOf(model)
        this.textures = arrayOf(texData)
        this.boundingBoxType = boundingBoxType
    }

    constructor(models:Array<string>, textures:Array<string>, boundingBoxType:BoundingBoxType):super(textures[0]) {
        this.modelIds = models
        this.textures = textures
        this.boundingBoxType = boundingBoxType
    }

    override var is3d:bool = true
    override fun copy():Bushery {
        return this
    }

}