package me.ethius.shared.rotsg.entity.enemy

import com.moandjiezana.toml.Toml
import me.ethius.client.Client
import me.ethius.client.rotsg.entity.Portal
import me.ethius.server.Server
import me.ethius.server.rotsg.entity.ServerPlayer
import me.ethius.server.rotsg.world.ServerWorld
import me.ethius.server.rotsg.world.WorldTracker
import me.ethius.shared.*
import me.ethius.shared.ext.getInt
import me.ethius.shared.loottable.LootTableEntry
import me.ethius.shared.maths.Facing
import me.ethius.shared.maths.WeightedCollection
import me.ethius.shared.network.Packet
import me.ethius.shared.opti.TexData
import me.ethius.shared.rotsg.data.ProjectileData
import me.ethius.shared.rotsg.entity.AEntity
import me.ethius.shared.rotsg.entity.StatEntity
import me.ethius.shared.rotsg.entity.ai.AIBase
import me.ethius.shared.rotsg.entity.ai.impl.AIAttackDefault
import me.ethius.shared.rotsg.entity.ai.impl.AIWander
import me.ethius.shared.rotsg.tile.tile_size
import kotlin.math.floor
import kotlin.math.sign


class Enemy private constructor(
    scale:double,
    var shotPattern:(Enemy) -> List<ProjectileData>,
    dex:int,
    var lootTable:MutableList<LootTableEntry>,
    var portal:WeightedCollection<() -> ServerWorld?>,
    var exp:int,
    health:int,
    def:int,
):StatEntity(dex, def, 0, 0, 0, 1, health, 0) {

    constructor():this(1.0, { listOf() }, 0, emptyLootTable, emptyPortal, 0, 0, 0)

    var scale = scale
        get() {
            return field * 5f
        }
    override lateinit var texDataId:string
    private var lastFacing = Facing.horizontalValues.random()
    var facing = Facing.horizontalValues.random()
    val movementAIs = WeightedCollection<AIBase>()
    var currentMoveAI:AIBase? = null
        set(value) {
            if (value != null && value.type != AIBase.Type.move) {
                throw IllegalStateException("field 'currentMovementAI' must be set to an AI with type 'movement.'")
            }
            field = value
        }
    val attackAIs = WeightedCollection<AIBase>()
    var currentAttackAI:AIBase? = null
        set(value) {
            if (value != null && value.type != AIBase.Type.attack) {
                throw IllegalStateException("field 'currentAttackAI' must be set to an AI with type 'attack.'")
            }
            field = value
        }
    override val velocity:dvec3
        get() {
            return dvec3(-(x - prevX).sign, -(y - prevY).sign, 0.0)
        }
    var vision = 8f
    var arcGap = 0.0
    override var height:double = 0.0
    override var pivotX:double = 0.0
    override var pivotY:double = 0.0
    override var width:double = 0.0
    var spawnPos = dvec2(0.0, 0.0)
    var leadShot:bool = false
    var damagerIds = hashSetOf<long>()

    override fun setRenderPos() {
        super.setRenderPos()
        this.updateDirection()
    }

    override fun damage(damage:double, throughDef:bool, damageSourceId:long) {
        super.damage(damage, throughDef, damageSourceId)
        if (damageSourceId == -1L || damageSourceId == -2L) {
            return
        }
        damagerIds.add(damageSourceId)
    }

    fun fire() {
        val sp = shotPattern(this)
        val sangle = -arcGap * sp.size.toFloat() * 0.25f
        for ((e, it) in sp.withIndex()) {
            this.shoot(it, sangle + e * arcGap + it.baseAngle)
        }
    }

    override fun release() {
        super.release()
        if (Side._server) {
            run {
                val ldid = this.damagerIds.lastOrNull() ?: return@run
                if (ldid != -1L) {
                    val damager = this.world?.getEntityById(ldid) ?: return@run
                    if (damager is ServerPlayer) {
                        Server.network.send(damager.client, Packet._id_exp_add, exp)
                    }
                }
            }
            for (i in damagerIds) {
                val damager = this.world?.getEntityById(i) ?: continue
                if (damager !is ServerPlayer) {
                    continue
                }
                val items = StringBuilder()
                for (j in this.lootTable) {
                    val k = j.next() ?: continue
                    items.append(k).append(" ")
                }
                if (items.isBlank()) {
                    continue
                }
                items.deleteCharAt(items.lastIndexOf(" "))
                Server.network.send(damager.client, Packet._id_bag_spawn, x, y + 15f, items.toString())
            }
            portal.next()?.let {
                it()?.let { WorldTracker.add(it) }
            }
            val worldSupplier = portal.next() ?: return
            val world = worldSupplier() ?: return
            val worldId = WorldTracker.add(world)
            this.world?.addEntity(Portal(world.texDataId, worldId, world.name).also { it.y = this.y; it.x = this.x; it.invulnerable = true })
        }
    }

    override fun clientTick() {
        this.prevX = this.x
        this.prevY = this.y
        if (!serverX.isNaN() && !serverY.isNaN()) {
            moveTo(serverX, serverY)
            this.serverX = Double.NaN
            this.serverY = Double.NaN
        }
        val f1 = Client.world.tileAt(ivec2(floor(x / tile_size).toInt(),
                                           floor(y / tile_size).toInt()))
        if (f1 != null) {
            depth = f1.depth
        }
    }

    override fun serverTick() {
        if (this.currentMoveAI == null || this.currentMoveAI!!.isDone()) {
            this.currentMoveAI = (this.movementAIs.next() ?: AIWander(this)).also { it.reset() }
        }
        this.currentMoveAI!!.update()
        if (this.currentAttackAI == null || this.currentAttackAI!!.isDone()) {
            this.currentAttackAI = (attackAIs.next() ?: AIAttackDefault(this)).also { it.reset() }
        }
        this.currentAttackAI!!.update()
    }

    override fun collideWithBlocks() {
        this.currentMoveAI?.collideWithBlocks()
    }

    override fun collideWith(other:AEntity) {

    }

    private fun updateDirection() {
        lastFacing = facing
        facing = when ((renderPos.x - prevRenderPos.x).sign) {
            1.0 -> Facing.left
            -1.0 -> Facing.right
            else -> lastFacing
        }
    }

    fun setTexData(texDataId:string):Enemy {
        this.texDataId = texDataId
        val texData = TexData[texDataId]
        height = texData.height * scale
        width = texData.width * scale
        pivotX = texData.pivotX * scale
        pivotY = texData.pivotY * scale
        return this
    }

    fun initPosition(posX:double, posY:double):Enemy {
        this.x = posX
        this.y = posY
        this.prevX = this.x
        this.prevY = this.y
        this.spawnPos.set(posX, posY)
        updateBoundingCircle()
        return this
    }

    init {
        this.hp = health.toDouble()
    }

    companion object {
        val emptyLootTable = ArrayList<LootTableEntry>()
        val emptyPortal = WeightedCollection<() -> ServerWorld?>()

        fun newEnemy(enemyEntityInfo:Builder):Enemy {
            val e = Enemy()
            e.scale = enemyEntityInfo.scale
            e.setTexData(enemyEntityInfo.texDataId)
            e.def = enemyEntityInfo.def
            e.dex = enemyEntityInfo.dex
            e.spd = enemyEntityInfo.speed
            e.exp = enemyEntityInfo.exp
            e.lootTable = enemyEntityInfo.lootTable
            e.portal = enemyEntityInfo.portal
            e.hp = enemyEntityInfo.health.toDouble()
            e.life = enemyEntityInfo.health
            e.depth = 0
            e.shotPattern = enemyEntityInfo.shotPattern
            e.currentMoveAI = null
            e.currentAttackAI = null
            e.arcGap = enemyEntityInfo.arcGap
            e.effects.clear()
            e.leadShot = enemyEntityInfo.leadShot
            return e
        }

        fun make(lambda:Builder.() -> void):Builder = Builder().apply(lambda)
    }

    class Builder {
        lateinit var texDataId:string
        var scale:double = 1.0
        var health:int = 1
        var def:int = 1
        var dex:int = 10
        var shotPattern:(Enemy) -> List<ProjectileData> = { emptyList() }
        var lootTable = ArrayList<LootTableEntry>()
        var exp:int = 30
        var portal = WeightedCollection<() -> ServerWorld?>()
        var speed = 1
        var arcGap = 0.0
        var leadShot = false

        constructor()

        private constructor(assetLoc:string) {
            val toml = Toml().read(Client::class.java.getResourceAsStream(assetLoc))
            val meta = toml.getTable("meta")
            this.texDataId = meta.getString("tex_data")
            this.health = meta.getInt("health")
            this.def = meta.getInt("def", 5)
            this.dex = meta.getInt("dex", 10)
            this.arcGap = meta.getDouble("arc_gap", 0.0)
            this.scale = meta.getDouble("scale", 1.0)
            this.leadShot = meta.getBoolean("lead_shot", false)
            if (meta.contains("shot_pattern")) {
                this.shotPattern =
                    { meta.getList<string>("shot_pattern").map { i -> ProjectileData.values.find { it.id == i }!! } }
            }
            if (meta.contains("loot_table")) {
                for (i in meta.getList<string>("loot_table")) {
                    val entry = LootTableEntry.find(i)
                    if (entry != LootTableEntry.empty) {
                        this.lootTable.add(entry)
                    }
                }
            }
            this.exp = meta.getInt("exp", 30)
            if (meta.containsTable("portal_table")) {
                for (i in meta.getTable("portal_table").entrySet()) {
                    this.portal.add(i.value as double) { Class.forName(i.key as string).getConstructor().newInstance() as ServerWorld }
                }
            }
            this.speed = meta.getInt("speed", 1)
        }

        fun withArcGap(arcGap:double):Builder {
            this.arcGap = arcGap
            return this
        }

        fun withTexData(texData:TexData):Builder {
            this.texDataId = texData.id
            return this
        }

        fun withTexData(texDataId:string):Builder {
            this.texDataId = texDataId
            return this
        }

        fun withScale(scale:double):Builder {
            this.scale = scale
            return this
        }

        fun withHealth(health:int):Builder {
            this.health = health
            return this
        }

        fun withDef(def:int):Builder {
            this.def = def
            return this
        }

        fun withDex(dex:int):Builder {
            this.dex = dex
            return this
        }

        fun withShotPattern(shotPattern:(Enemy) -> List<ProjectileData>):Builder {
            this.shotPattern = shotPattern
            return this
        }

        fun withLeadShot(leadShot:bool):Builder {
            this.leadShot = leadShot
            return this
        }

        fun addToLootTable(vararg lootTableEntry:LootTableEntry):Builder {
            this.lootTable.addAll(lootTableEntry)
            return this
        }

        fun addToPortalTable(chance:double, entry:() -> ServerWorld?):Builder {
            portal.add(chance, entry)
            return this
        }

        fun emptyPortalChance(chance:double):Builder {
            portal.add(chance) { null }
            return this
        }

        fun withExp(exp:int):Builder {
            this.exp = exp
            return this
        }

        companion object {
            private const val asset_loc = "/assets/data/entity"
            val cachedBuilders:HashMap<string, Builder> = HashMap()

            fun newBuilder(assetLoc:string):Builder {
                val assetLoc = if (assetLoc.endsWith(".dat")) assetLoc else "$asset_loc/$assetLoc.dat"
                if (cachedBuilders.containsKey(assetLoc)) {
                    return cachedBuilders[assetLoc]!!
                }
                val builder = Builder(assetLoc)
                cachedBuilders[assetLoc] = builder
                return builder
            }

            fun newBuilder():Builder {
                return Builder()
            }
        }
    }

}