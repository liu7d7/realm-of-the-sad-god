package me.ethius.shared.rotsg.entity

import me.ethius.client.Client
import me.ethius.client.rotsg.gui.Damage
import me.ethius.shared.*
import me.ethius.shared.network.Packet
import kotlin.math.roundToInt

abstract class StatEntity(
    var dex:int,
    var def:int,
    var wis:int,
    var vit:int,
    var atk:int,
    var spd:int,
    final override var life:int,
    var mana:int,
):AEntity() {

    // this entity's mana level. //
    open var mp = 0.0
        set(value) {
            prevMp = field
            maxMp = maxMp.coerceAtLeast(value.roundToInt())
            lastManaUpdate = measuringTimeMS()
            field = value
            if (prevMp == 0.0)
                prevMp = field
        }

    // last health update time //
    var lastManaUpdate = 0f

    // previous hp value //
    var prevMp = 0.0

    // this entity's max health //
    open var maxMp:int = 0

    val aps:float
        get() {
            return (1.5f + 6.5f * (dex.toFloat() * 0.013333334f)) * apsMultiplier * (if (this.hasEffect("berserk")) 1.5f else 1f)
        }

    val tps:float
        get() {
            return (4 + 5.6f * (spd.toFloat() / 75f)) * 0.625f
        }

    val damageMultiplier:double
        get() {
            return (0.5 + atk.toDouble() * 0.02) * (if (this.hasEffect("damaging")) 1.5 else 1.0)
        }

    val hphs:float
        get() {
            return (1f + 0.24f * vit) * 0.04f
        }

    val mphs:float
        get() {
            return (0.5f + 0.12f * wis) * 0.08f
        }

    var apsMultiplier:float = 1f

    var dexAdd = 0
    var defAdd = 0
    var wisAdd = 0
    var vitAdd = 0
    var atkAdd = 0
    var spdAdd = 0
    var lifeAdd = 0
    var manaAdd = 0

    val manaMaxed
        get() = mana - manaAdd
    val lifeMaxed
        get() = life - lifeAdd
    val dexMaxed
        get() = dex - dexAdd
    val defMaxed
        get() = def - defAdd
    val wisMaxed
        get() = wis - wisAdd
    val vitMaxed
        get() = vit - vitAdd
    val atkMaxed
        get() = atk - atkAdd
    val spdMaxed
        get() = spd - spdAdd

    override fun damage(damage:double, throughDef:bool, damageSourceId:long) {
        val damage = damage * if (this.hasEffect("curse")) 1.2 else 1.0
        var fDmg = damage
        if (!throughDef) {
            fDmg -= def
        }
        hp -= fDmg.coerceAtLeast(2.0).also { if (Side._client) entityNotifications.add(Damage(this, it.roundToInt(), throughDef)) }
        if (Side._client && damageSourceId != -2L) {
            Client.network.send(Packet._id_damage_entity, damageSourceId, this.entityId, fDmg.coerceAtLeast(2.0))
        }
    }

    open fun incStat(stat:Stat, amt:int, max:bool = false) {
        when (stat) {
            Stat.dex -> {
                dex += amt
            }
            Stat.def -> {
                def += amt
            }
            Stat.wis -> {
                wis += amt
            }
            Stat.vit -> {
                vit += amt
            }
            Stat.atk -> {
                atk += amt
            }
            Stat.spd -> {
                spd += amt
            }
            Stat.life -> {
                life += amt
            }
            Stat.mana -> {
                mana += amt
            }
        }
    }

    init {
        mp = mana.toDouble()
        hp = life.toDouble()
    }

}