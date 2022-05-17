package me.ethius.shared.rotsg.entity.player

import me.ethius.shared.*
import me.ethius.shared.opti.PlayerTexData
import me.ethius.shared.opti.TexData
import me.ethius.shared.rotsg.entity.AEntity
import me.ethius.shared.rotsg.entity.StatEntity

open class Player(val pClass:PlayerClass, val playerProfile:PlayerProfile):StatEntity(12, 0, 12, 12, 15, 10, 150, 100) {

    override var width:double = 0.0
    override var height:double = 0.0
    override val pivotX:double
        get() {
            return TexData[texDataId].pivotX * 5
        }
    override val pivotY:double = 0.0
    val lastTilePos = ivec2()
    val tilePos = ivec2()
    override var texDataId:string = "empty"
    var texXOffset:double = 0.0
    open var exp:int = 0
    var level:int = 1
    var shotsFired = 0
    var lastShot = 0f
    var name:string = "unnamed"
    var rawExp = true
    var walkTex = 0
    var lastWalkTexUpdate = dvec2()
    var lastRotationTexUpdate = 0.0
    var selectedTexData = playerProfile.skin
        set(value) {
            pTexData = this.pClass.skins[value]
            field = value
        }
    protected lateinit var pTexData:PlayerTexData

    fun nextLevel():int {
        return this.level * this.level * 50
    }

    fun prevLevel():int {
        return (this.level - 1) * (this.level - 1) * 50
    }

    override fun collideWith(other:AEntity) {

    }

    override fun clientTick() {

    }
    
    init {
        this.life = playerProfile.stats[0]
        this.mana = playerProfile.stats[1]
        this.hp = this.life.toDouble()
        this.mp = this.mana.toDouble()
        this.atk = playerProfile.stats[2]
        this.def = playerProfile.stats[3]
        this.spd = playerProfile.stats[4]
        this.dex = playerProfile.stats[5]
        this.vit = playerProfile.stats[6]
        this.wis = playerProfile.stats[7]
        this.rawExp = true
        this.exp = playerProfile.exp
        this.rawExp = false
        if (Side._client) {
            this.pTexData = this.pClass.skins[this.playerProfile.skin]
        }
        this.name = this.playerProfile.name
    }
    
}