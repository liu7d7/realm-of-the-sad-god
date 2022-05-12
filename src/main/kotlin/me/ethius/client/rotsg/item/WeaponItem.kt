package me.ethius.client.rotsg.item

import com.moandjiezana.toml.Toml
import me.ethius.client.Client
import me.ethius.shared.ext.getInt
import me.ethius.shared.int
import me.ethius.shared.opti.TexData
import me.ethius.shared.rotsg.data.EffectInfo
import me.ethius.shared.rotsg.data.ProjectileData
import me.ethius.shared.rotsg.entity.Stat
import me.ethius.shared.rotsg.entity.createSpawnPacket
import me.ethius.shared.rotsg.entity.other.Projectile
import me.ethius.shared.string
import org.apache.commons.lang3.RandomUtils
import org.apache.commons.lang3.StringUtils
import kotlin.math.sign

open class WeaponItem:Item {

    lateinit var shotPattern:List<ProjectileData>
    var arcGap = 0.0
    var apsMultiplier = 1f
    var damage:ClosedRange<int>
    private var numShots:int

    constructor(
        texData:TexData,
        tier:ItemTier,
        damage:ClosedRange<int>,
        numShots:int,
        statMap:MutableMap<Stat, int>,
        name:string,
        desc:string,
    ):super(texData, tier, name, desc) {
        this.damage = damage
        this.numShots = numShots
        this.statMap = statMap
    }

    constructor(assetLoc:string):super(assetLoc) {
        val toml = Toml().read(Client::class.java.getResourceAsStream("$item_data_loc/$assetLoc.dat"))
        // weapon meta
        val weaponMeta = toml.getTable("weapon_meta")
        val da = weaponMeta.getString("damage").split("-").map { it.toInt() }
        this.damage = da[0]..da[1]
        this.shotPattern =
            weaponMeta.getList<string>("shot_pattern").map { str -> ProjectileData.values.find { it.id == str }!! }
        this.numShots = weaponMeta.getInt("num_shots", shotPattern.size)
        this.apsMultiplier = weaponMeta.getDouble("aps_multiplier", 1.0).toFloat()
        this.arcGap = weaponMeta.getDouble("arc_gap", 0.0)
    }

    open fun onShoot(owner:me.ethius.shared.rotsg.entity.AEntity) {
        Client.player.shotsFired++
        val ags = (numShots - 1) * -arcGap / 2f
        for ((idx, it) in shotPattern.withIndex()) {
            val prj = Projectile().reset(owner, it, ags + arcGap * idx).also {
                it.damageMultiplier = Client.player.damageMultiplier
                if (Client.player.hasLegendaryEffect(LegendaryEffect.curse)) {
                    if (RandomUtils.nextFloat(0f, 1f / LegendaryEffect.curse.chance) < 1f) {
                        it.hitEffects.add(EffectInfo.curse(2800L))
                        LegendaryEffect.curse.onActivateRaw(owner)
                    }
                }
            }
            Client.world.addEntity(prj)
            Client.network.send(prj.createSpawnPacket())
        }
    }

    override fun getTooltip():List<string> {
        val mappedList = ArrayList<string>()
        for ((k, v) in statMap) {
            val isPos = v.sign == 1
            mappedList.add("${StringUtils.capitalize(k.toString().lowercase())}: ${if (isPos) "+" else ""}$v")
        }
        val tmp = mutableListOf(name, *Client.font.wrapWords(desc, 300.0),
                                "-------------------",
                                "Shots: ${shotPattern.size}",
                                "Damage: $damage",
                                "Attack Speed: ${(apsMultiplier * 100).toInt()}%")
        if (mappedList.isNotEmpty())
            tmp.add("-------------------")
        tmp.addAll(mappedList)
        if (mappedList.isNotEmpty() || legendaryEffect != null)
            tmp.add("-------------------")
        if (legendaryEffect != null)
            tmp.addAll(legendaryEffect!!.infoList)
        return tmp
    }

    override fun onEquip() {
        Client.player.apsMultiplier = apsMultiplier
    }

    override fun onDequip() {
        Client.player.apsMultiplier = 1f
    }

}

open class KatanaItem:WeaponItem {

    constructor(
        texData:TexData,
        tier:ItemTier,
        damage:ClosedRange<int>,
        numShots:int,
        statMap:MutableMap<Stat, int>,
        name:string,
        desc:string,
    ):super(texData, tier, damage, numShots, statMap, name, desc)

    constructor(
        texData:TexData,
        tier:ItemTier,
        shotPattern:List<ProjectileData>,
        statMap:MutableMap<Stat, int>,
        name:string,
        desc:string,
    ):this(texData, tier, shotPattern.first().damage, shotPattern.size, statMap, name, desc) {
        this.shotPattern = shotPattern
    }

    constructor(assetLoc:string):super(assetLoc)

}

open class BowItem:WeaponItem {

    constructor(
        texData:TexData,
        tier:ItemTier,
        damage:ClosedRange<int>,
        numShots:int,
        statMap:MutableMap<Stat, int>,
        name:string,
        desc:string,
    ):super(texData, tier, damage, numShots, statMap, name, desc)

    constructor(
        texData:TexData,
        tier:ItemTier,
        shotPattern:List<ProjectileData>,
        statMap:MutableMap<Stat, int>,
        name:string,
        desc:string,
    ):this(texData, tier, shotPattern.first().damage, shotPattern.size, statMap, name, desc) {
        this.shotPattern = shotPattern
    }

    constructor(assetLoc:string):super(assetLoc)

}

open class SwordItem:WeaponItem {

    constructor(
        texData:TexData,
        tier:ItemTier,
        damage:ClosedRange<int>,
        numShots:int,
        statMap:MutableMap<Stat, int>,
        name:string,
        desc:string,
    ):super(texData, tier, damage, numShots, statMap, name, desc)

    constructor(
        texData:TexData,
        tier:ItemTier,
        shotPattern:List<ProjectileData>,
        statMap:MutableMap<Stat, int>,
        name:string,
        desc:string,
    ):this(texData, tier, shotPattern.first().damage, shotPattern.size, statMap, name, desc) {
        this.shotPattern = shotPattern
    }

    constructor(assetLoc:string):super(assetLoc)

}

open class DaggerItem:WeaponItem {
    constructor(
        texData:TexData,
        tier:ItemTier,
        damage:ClosedRange<int>,
        numShots:int,
        statMap:MutableMap<Stat, int>,
        name:string,
        desc:string,
    ):super(texData, tier, damage, numShots, statMap, name, desc)

    constructor(
        texData:TexData,
        tier:ItemTier,
        shotPattern:List<ProjectileData>,
        statMap:MutableMap<Stat, int>,
        name:string,
        desc:string,
    ):this(texData, tier, shotPattern.first().damage, shotPattern.size, statMap, name, desc) {
        this.shotPattern = shotPattern
    }

    constructor(assetLoc:string):super(assetLoc)
}