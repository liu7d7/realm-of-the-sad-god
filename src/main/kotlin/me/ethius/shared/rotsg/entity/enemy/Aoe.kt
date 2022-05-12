package me.ethius.shared.rotsg.entity.enemy

import me.ethius.client.Client
import me.ethius.client.mouseDstToPlayer
import me.ethius.shared.*
import me.ethius.shared.maths.Animations
import me.ethius.shared.opti.TexData
import me.ethius.shared.rotsg.entity.AEntity
import me.ethius.shared.rotsg.entity.player.Player
import me.ethius.shared.rotsg.tile.tile_size

class Aoe(x:double, y:double):AEntity() {
    override var width:double = 0.0
    override var height:double = 0.0
    override val pivotX:double = 0.0
    override val pivotY:double = 0.0
    override var texDataId:string = "rect"
    override var shouldRender:bool = false
    var direction:double = 0.0
    val startPos = dvec2()
    var lifetime:double = 0.0
    var maxDist = 0.0
    var speed:double = 0.0
        set(value) {
            field = value
            if (owner is Player) {
                val dist = mouseDstToPlayer()
                maxDist = value * 0.001 * lifetime * tile_size
                if (dist < maxDist) {
                    field = dist / maxDist * value
                }
            }
        }
    var radius = 0.0
    var acted = false
    var damage = 0.0
    private var _owner:AEntity? = null
    var owner:AEntity
        get() = _owner!!
        set(value) {
            _owner = value
        }

    override fun playerCanSee():bool {
        return true
    }

    override fun damage(damage:double, throughDef:bool, damageSourceId:long) {

    }

    override fun collideWith(other:AEntity) {
        if (!acted && other != owner) {
            other.damage(damage, false)
            acted = true
        }
    }

    override fun clientTick() {
        val time = ticksExisted * 20.0
        if (time >= lifetime) {
            val distRad = (time - lifetime) / 60.0 * radius * tile_size
            val x = startPos.x + speed * 0.001 * lifetime * tile_size * cosD(direction)
            val y = startPos.y + speed * 0.001 * lifetime * tile_size * sinD(direction)
            this.boundingCircle.radius = radius * tile_size
            this.boundingCircle.cx = x
            this.boundingCircle.cy = y
            for (_i in 0..360 step 30) {
                val i = _i.toDouble()
                val xOff = distRad * cosD(i)
                val yOff = distRad * sinD(i)
                Client.fxManager.createFx(TexData[texDataId], x + xOff, y + yOff, 1, 0.0, false)
            }
            if (time - lifetime > 60.0) {
                Client.world.remEntity(this, true, false)
            }
        }
        val z = Animations.upAndDownCurve(lifetime, time) * tile_size * 12
        val distance = speed * 0.001 * time * tile_size
        val x = startPos.x + distance * cosD(direction)
        val y = startPos.y + distance * sinD(direction)
        Client.fxManager.createFx(TexData[texDataId], x, y, 2, z)
    }

    init {
        this.startPos.set(x, y)
    }

}