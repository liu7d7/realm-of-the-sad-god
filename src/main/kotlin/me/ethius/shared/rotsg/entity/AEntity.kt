package me.ethius.shared.rotsg.entity

import me.ethius.client.Client
import me.ethius.client.ext.transform
import me.ethius.client.rotsg.entity.ClientPlayer
import me.ethius.client.rotsg.gui.Damage
import me.ethius.client.rotsg.gui.EntityNotification
import me.ethius.server.Server
import me.ethius.server.network.SNetworkHandler
import me.ethius.server.rotsg.entity.ServerPlayer
import me.ethius.server.rotsg.world.ServerWorld
import me.ethius.shared.*
import me.ethius.shared.ext.distance2d
import me.ethius.shared.ext.distanceSquared
import me.ethius.shared.ext.r
import me.ethius.shared.maths.BoundingCircle
import me.ethius.shared.network.Packet
import me.ethius.shared.rotsg.data.ProjectileProperties
import me.ethius.shared.rotsg.entity.other.Effect
import me.ethius.shared.rotsg.entity.other.Projectile
import me.ethius.shared.rotsg.tile.tile_size
import me.ethius.shared.rotsg.world.IWorld
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sqrt

abstract class AEntity:Tickable() {

    // client member variables
    override val shouldTick:bool
        get() {
            ifserver {
                if (this.world == null) {
                    return false
                }
                return ((this.world as? ServerWorld)?.closestPlayer(this)?.pos?.distance2d(this.pos) ?: 10000000.0) / tile_size < 21
            }
            return true
        }
    open val prevRenderPos:dvec2 = dvec2()
    open val renderPos:dvec2 = dvec2()
    open val lerpedX:double
        get() = lerp(prevX, x, Client.ticker.tickDelta)
    open val lerpedY:double
        get() = lerp(prevY, y, Client.ticker.tickDelta)
    open val lerpedR:double
        get() = lerp(prevR, r, Client.ticker.tickDelta)
    var lastDamage = 0.0
    var depth = 0
        set(value) {
            field = value.coerceIn(0..10)
        }
    private var lastHealthUpdate = 0f
    private var prevHp = 0.0
    open val velocity:dvec3 = dvec3()
    val yCompY
        get() = -height / 2f
    val alive
        get() = hp >= 0
    protected val data = HashMap<string, Any>()
    open var shouldRender:bool = true
    abstract val width:double
    abstract val height:double
    abstract val pivotX:double
    abstract val pivotY:double
    open var prevX:double = 0.0
    open var prevY:double = 0.0
    open var prevR:double = 0.0
    var serverX:double = Double.NaN
    var serverY:double = Double.NaN


    // shared member variables
    var world:IWorld? = null
    val timeSpawned = measuringTimeMS()
    var typeId = ""
    open var entityId = getId()
    open var x:double
        get() = pos.x
        set(value) {
            pos.x = value
        }
    open var y:double
        get() = pos.y
        set(value) {
            pos.y = value
        }
    open var r:double
        get() = pos.r
        set(value) {
            pos.r = value
        }
    open var pos:dvec3 = dvec3()
    open var hp = 0.0
        set(value) {
            ifclient {
                prevHp = field
                lastDamage = field - value
                lastHealthUpdate = measuringTimeMS()
                if (prevHp == 0.0)
                    prevHp = value
            }
            ifserver {
                Server.network.broadcastIf(Packet(Packet._id_hp_update, this.entityId, value)) {
                    val sp = ServerPlayer[it] ?: return@broadcastIf false
                    sp.world == this.world && sp != this
                }
            }
            field = value
        }
    open var life:int = 0
    val boundingCircle:BoundingCircle = BoundingCircle()
    val effects = CopyOnWriteArrayList<Effect>()
    val entityNotifications = CopyOnWriteArrayList<EntityNotification>()
    abstract val texDataId:string

    // client functions
    override fun release() {
        super.release()
        this.prevX = this.x
        this.prevY = this.y
        for (i in effects) {
            i.onEntityDie(this)
            removeEffect(i.id)
        }
    }

    fun <T:Any> pushData(id:string, data:T):AEntity {
        this.data[id] = data
        return this
    }

    fun <T:Any> pushDataIfAbsent(id:string, data:T):AEntity {
        if (!this.data.containsKey(id))
            this.data[id] = data
        return this
    }

    fun <T> getData(id:string):T {
        return data[id] as T ?: throw IllegalStateException("$id is not a valid data identifier!")
    }

    protected open fun setRenderPos() {
        if (!Client.lookAtInit) {
            return
        }
        val m4f = Client.lookAt
        run {
            val vec4 = dvec4(x, y, 0.0, 1.0)
            vec4.transform(m4f)
            renderPos.x = vec4.x + pivotX
            renderPos.y = vec4.y + height / 2f + pivotY
        }
        run {
            val vec4 = dvec4(prevX, prevY, 0.0, 1.0)
            vec4.transform(m4f)
            prevRenderPos.x = vec4.x + pivotX
            prevRenderPos.y = vec4.y + height / 2f + pivotY
        }
    }

    open fun playerCanSee():bool {
        var bl = pos.distanceSquared(Client.player.x,
                                     Client.player.y) <= Client.options.renderDst * tile_size * Client.options.renderDst * tile_size
        if (bl) {
            setRenderPos()
            bl = bl && (this.renderPos.x > -width && this.renderPos.x < Client.window.scaledWidth + width) &&
                 (this.renderPos.y > -height && this.renderPos.y < Client.window.scaledHeight + height * 2)
        }
        return bl
    }

    fun shoot(proj:Projectile):Projectile {
        proj.owner = this
        Client.world.addEntity(proj)
        return proj
    }

    fun shoot(
        proj:ProjectileProperties,
        angle:double = Double.NaN,
    ):Projectile {
        val proj = Projectile().reset(this, proj, if (!angle.isNaN()) angle else proj.baseAngle)
        when (Side.currentSide) {
            Side.client -> {
                Client.world.addEntity(proj)
            }
            Side.server -> {
                this.world?.addEntity(proj)
            }
        }
        return proj
    }

    override fun clientTick() {  }


    // server functions
    open fun onAdd(world:IWorld) {
        this.world = world
    }

    open fun onRem(world:IWorld) {
        this.world = null
    }

    fun inWorld():bool {
        return this.world != null
    }


    // shared functions
    protected open fun updateBoundingCircle() {
        this.boundingCircle.cx = this.x
        this.boundingCircle.cy = this.y
        this.boundingCircle.radius = 15.0
    }

    private fun broadcastPosition() {
        when (this) {
            is ServerPlayer -> {
                Server.network.broadcastIf(Packet(Packet._id_move, this.entityId, this.x, this.y), fun(it:SNetworkHandler.ClientView):bool {
                    val them = ServerPlayer[it] ?: return false
                    return them.client != this.client && them.world == this.world && them.pos.distance2d(this.pos) / tile_size <= 21
                })
            }
            else -> {
                Server.network.broadcastIf(Packet(Packet._id_move, this.entityId, this.x, this.y),
                                           fun(it:SNetworkHandler.ClientView):bool {
                                               val player = ServerPlayer[it] ?: return false
                                               return (this.world == player.world && player.pos.distance2d(this.pos) / tile_size <= 21)
                                           })
            }
        }
    }

    private fun afterMove() {
        this.updateBoundingCircle()
        if (Side._server) {
            broadcastPosition()
            if (this !is PassableEntity) {
                this.pushOutOfBlocks()
            }
        } else if (this is ClientPlayer) {
            this.pushOutOfBlocks()
        }
    }

    fun move(x:double, y:double) {
        this.x += x
        this.y += y
        afterMove()
    }

    fun moveDirection(angle:double, amount:double) {
        this.x += cosD(angle) * amount
        this.y += sinD(angle) * amount
        afterMove()
    }

    open fun moveTo(x:double, y:double, prev:bool = false) {
        this.x = x
        this.y = y
        if (prev) {
            this.prevX = x
            this.prevY = y
        }
        afterMove()
    }

    open fun collideWithBlocks() {

    }

    fun hasEffect(type:string):bool {
        return effects.any { it.id == type }
    }

    fun addEffect(effect:Effect) {
        removeEffect(effect.id)
        effect.entt = this
        effect.init()
        effects.add(effect)
        ifclient {
            Client.network.send(Packet(Packet._id_effect_add, this.entityId, effect.toString()))
        }
    }

    fun getEffect(effect:string):Effect? {
        return effects.find { it.id == effect }
    }

    fun removeEffect(type:string) {
        val e = HashSet<Effect>()
        for (it in effects) {
            val bl = it.id == type
            if (bl) {
                it.release()
                e.add(it)
                break
            }
        }
        effects.removeAll(e)
    }

    override fun equals(other:Any?):Boolean {
        if (other !is AEntity) {
            return false
        }
        return this.entityId == other.entityId
    }

    override fun hashCode():Int {
        return entityId.toInt()
    }

    abstract fun collideWith(other:AEntity)

    open fun damage(damage:double, throughDef:bool, damageSourceId:long = -2) {
        val damage = damage * if (this.hasEffect("curse")) 1.2 else 1.0
        hp -= damage.also { if (Side._client) entityNotifications.add(Damage(this, it.roundToInt(), throughDef)) }
    }

    fun collidesWith(other:AEntity):bool {
        when (Side.currentSide) {
            Side.server -> {
                if (this.world == null) {
                    return false
                }
                if (other.world == null) {
                    return false
                }
                return this.world == other.world && other.boundingCircle.collidesWith(this.boundingCircle)
            }
            Side.client -> {
                return this.boundingCircle.collidesWith(other.boundingCircle)
            }
        }
    }

    private fun pushOutOfBlocks() {
        if (Side._client && this !is ClientPlayer) {
            return
        }
        val world = (if (Side._client) Client.world else this.world) ?: return
        val boundingBoxes = world.getBoundingCircles(this.boundingCircle)
        if (boundingBoxes.isEmpty()) {
            updateBoundingCircle()
            return
        }
        for (other in boundingBoxes) {
            val dcx = (other.cx - this.boundingCircle.cx) * -1
            val dcy = (other.cy - this.boundingCircle.cy) * -1
            val dst = sqrt(dcx * dcx + dcy * dcy)
            val mult = (this.boundingCircle.radius + other.radius) / dst
            this.x = other.cx + dcx * mult
            this.y = other.cy + dcy * mult
            updateBoundingCircle()
        }
        ifserver {
            collideWithBlocks()
        }
    }

    fun flooredTilePos():ivec2 {
        return ivec2(floor(x / tile_size).toInt(), floor(y / tile_size).toInt())
    }

    companion object {
        protected fun getId():long {
            return UUID.randomUUID().mostSignificantBits
        }
    }

}