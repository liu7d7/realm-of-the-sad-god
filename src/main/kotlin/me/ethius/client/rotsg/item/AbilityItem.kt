package me.ethius.client.rotsg.item

import me.ethius.client.Client
import me.ethius.client.calcAngleMPToMouse
import me.ethius.shared.*
import me.ethius.shared.opti.TexData
import me.ethius.shared.rotsg.data.EffectInfo
import me.ethius.shared.rotsg.data.ProjectileData
import me.ethius.shared.rotsg.entity.AEntity
import me.ethius.shared.rotsg.entity.PassableEntity
import me.ethius.shared.rotsg.entity.Stat
import me.ethius.shared.rotsg.tile.tile_size
import me.ethius.shared.rotsg.world.IWorld
import org.apache.commons.lang3.StringUtils
import kotlin.math.floor
import kotlin.math.sign

open class AbilityItem:Item {

    constructor(
        texData:TexData,
        tier:ItemTier,
        action:(IWorld) -> void,
        mpCost:int,
        statMap:MutableMap<Stat, int>,
        name:string,
        desc:string,
    ):super(texData, tier, name, desc) {
        this.action = action
        this.mpCost = mpCost
        this.statMap = statMap
    }

    var action:(IWorld) -> void
    var mpCost:int

    open fun onAbilityUse() {
        if (Client.player.mp >= mpCost) {
            action(Client.world)
            Client.player.mp -= mpCost
        }
    }

    override fun getTooltip():List<string> {
        val mappedList = ArrayList<string>()
        for ((k, v) in statMap) {
            val isPos = v.sign == 1
            mappedList.add("${StringUtils.capitalize(k.toString().lowercase())}: ${if (isPos) "+" else ""}$v")
        }
        val tmp = mutableListOf(name,
                                *Client.font.wrapWords(desc, 300.0),
                                "-------------------",
                                "MP Cost: $mpCost",
                                "-------------------")
        tmp.addAll(mappedList)
        if (mappedList.isNotEmpty() || legendaryEffect != null)
            tmp.add("-------------------")
        if (legendaryEffect != null)
            tmp.addAll(legendaryEffect!!.infoList)
        return tmp
    }

}

open class QuiverItem(
    texData:TexData,
    tier:ItemTier,
    action:(IWorld) -> void,
    mpCost:int,
    statMap:MutableMap<Stat, int>,
    name:string,
    desc:string,
):AbilityItem(texData, tier, action, mpCost, statMap, name, desc)

open class NinjaStarItem(
    texData:TexData,
    tier:ItemTier,
    action:(IWorld) -> void,
    mpCost:int,
    statMap:MutableMap<Stat, int>,
    name:string,
    desc:string,
):AbilityItem(texData, tier, action, mpCost, statMap, name, desc)

open class ShieldItem(
    texData:TexData,
    tier:ItemTier,
    action:(IWorld) -> void,
    mpCost:int,
    statMap:MutableMap<Stat, int>,
    name:string,
    desc:string,
):AbilityItem(texData, tier, action, mpCost, statMap, name, desc)

open class DasherItem(
    texData:TexData,
    tier:ItemTier,
    mpCost:int,
    statMap:MutableMap<Stat, int>,
    name:string,
    desc:string,
    private val atkGiven:int,
    private val projectileData:ProjectileData,
):AbilityItem(texData, tier, { }, mpCost, statMap, name, desc) {

    override fun onAbilityUse() {
        if (Client.player.mp >= mpCost) {
            Client.player.mp -= mpCost
            run { // give attack
                val atk = atkGiven.toDouble() / 2.0
                if (atkGiven % 1.0 == 0.5) {
                    Client.player.addEffect(EffectInfo.atk_add_1(4000L))
                }
                val intAtk = floor(atk).toInt()
                for (i in 0 until intAtk) {
                    Client.player.addEffect(EffectInfo.atk_add_2(4000L))
                }
            }
            Client.player.shoot(projectileData)
            Client.player.addEffect(EffectInfo.shield(480L))

            val angle = calcAngleMPToMouse()

            Client.world.addEntity(object:PassableEntity() {
                override val width:double = 0.0
                override val height:double = 0.0
                override val pivotX:double = 0.0
                override val pivotY:double = 0.0
                override val texDataId:string = Client.player.texData.id
                val timeStart = measuringTimeMS()

                override fun collideWith(other:AEntity) {

                }

                override fun clientTick() {
                    this.prevX = this.x
                    this.prevY = this.y
                    moveDirection(angle, 0.025 * tile_size)
                    if (measuringTimeMS() - timeStart > 400)
                        Client.world.remEntity(this)
                }

                init {
                    this.x = Client.player.x
                    this.prevX = this.x
                    this.y = Client.player.y
                    this.prevY = this.y
                    val f1 = Client.world.tileAt(ivec2(floor(this.x / tile_size).toInt(),
                                                       floor(this.y / tile_size).toInt()))
                    this.depth = f1?.depth ?: 0
                }

            })
            Client.ticker.add(object:Tickable() {
                override fun clientTick() {
                    if (ticksExisted == 3) {
                        release()
                        return
                    }
                    Client.player.moveDirection(angle, tile_size)
                    Client.fxManager.createFx(TexData.atk_rock,
                                              Client.player.lerpedX,
                                              Client.player.lerpedY,
                                              3,
                                              10.0,
                                              false)
                }
            })
        }
    }
}