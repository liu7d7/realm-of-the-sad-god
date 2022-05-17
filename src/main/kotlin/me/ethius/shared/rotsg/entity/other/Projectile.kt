package me.ethius.shared.rotsg.entity.other

import com.google.common.collect.Maps
import me.ethius.client.Client
import me.ethius.client.rotsg.entity.ClientPlayer
import me.ethius.client.rotsg.fx.Fx
import me.ethius.server.rotsg.world.ServerWorld
import me.ethius.shared.*
import me.ethius.shared.ext.todvec2
import me.ethius.shared.opti.TexData
import me.ethius.shared.rotsg.data.ProjectileData
import me.ethius.shared.rotsg.entity.AEntity
import me.ethius.shared.rotsg.entity.PassableEntity
import me.ethius.shared.rotsg.entity.enemy.Enemy
import me.ethius.shared.rotsg.entity.player.Player
import me.ethius.shared.rotsg.tile.tile_size
import org.apache.commons.lang3.RandomUtils
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.floor
import kotlin.math.min

class Projectile:PassableEntity() {
    // [meta]
    override var texDataId:string = "empty"
        set(value) {
            field = value
            val texData = TexData[value]
            width = texData.width / 2f
            height = texData.height / 2f
            pivotX = texData.pivotX
            pivotY = texData.pivotY
        }
    var projProps:ProjectileData = ProjectileData.empty

    // [data]
    override var width:double = 0.0
    override var height:double = 0.0
    override var pivotX:double = 0.0
    override var pivotY:double = 0.0
    private val entitiesHit:MutableList<AEntity> = mutableListOf()
    private var damagesEnemies:bool = false
    private var damagesPlayers:bool = false
    var bulletId:int = -1
    override var r:double
        get() = super.r
        set(value) {
            super.r = value
        }
    var owner:AEntity? = null
        set(value) {
            field = value
            if (value is Player) {
                damagesPlayers = false
                damagesEnemies = true
            } else {
                damagesEnemies = false
                damagesPlayers = true
            }
            startPos = value?.pos?.todvec2() ?: dvec2(0.0, 0.0)
            prevX = startPos.x
            prevY = startPos.y
            x = startPos.x
            y = startPos.y
            updateBoundingCircle()
            if (bulletId == -1 && value != null) {
                bulletId = nextBulletId(value)
                z = when (abs(this.bulletId % 5)) {
                    1 -> 2.5
                    2 -> 0.0
                    else -> 5.0
                }
            }
        }
    lateinit var startPos:dvec2
    private var prevDirection:double = 0.0
    private var direction:double = 0.0
    var damageMultiplier:double = 1.0
    val lerpedDirection:double
        get() {
            var pd = prevDirection
            if (direction - pd > 180.0) {
                pd += 360f
            } else if (pd - direction > 180.0) {
                pd -= 360f
            }
            return (lerp(pd, direction, Client.ticker.tickDelta))
        }
    private var praa:double = 0.0
    var raa:double = 0.0
    val lraa:double
        get() = lerp(praa, raa, Client.ticker.tickDelta)
    var leadShot = false
    var hitEffects = mutableListOf<Effect>()
    var z = 0.0

    override fun updateBoundingCircle() {
        boundingCircle.radius = min(width, height) * 0.5 + 3f
        boundingCircle.cx = this.x
        boundingCircle.cy = this.y
    }

    private fun offsetStartPos() {
        startPos.x += cosD(r) * 24f
        startPos.y += sinD(r) * 24f
        startPos.x += cosD(r + 90.0) * projProps.horizontalOffset
        startPos.y += sinD(r + 90.0) * projProps.horizontalOffset
        this.x = startPos.x
        this.y = startPos.y
        this.prevX = startPos.x
        this.prevY = startPos.y
    }

    fun reset(owner:AEntity, projectileData:ProjectileData):Projectile {
        reset(owner, projectileData, projectileData.baseAngle)
        return this
    }

    fun reset(
        owner:AEntity,
        projectileData:ProjectileData,
        angle:double,
    ):Projectile {
        this.owner = owner
        this.r = angle
        this.projProps = projectileData
        this.texDataId = this.projProps.texDataId
        if (owner is ClientPlayer) {
            this.r = wrapDegrees(calcAngle(-((Client.cameraPos.y + startPos.y) - Client.mouse.y),
                                           -((Client.cameraPos.x + startPos.x) - Client.mouse.x)) - Client.player.r + angle)
        } else if (projectileData.atPlayer) {
            val world = (owner.world as? ServerWorld) ?: throw IllegalStateException("Projectile owner has no world")
            val player = world.closestPlayer(owner) ?: world.entities.randomOrNull() ?: owner
            this.r = wrapDegrees(calcAngle(owner, player, projectileData.leadShot, (1.0 - projProps.speed / 20.0) * 12.0) + angle)
        }
        this.prevR = this.r
        this.direction = this.r - 90.0
        this.prevDirection = this.direction
        this.raa = projectileData.renderAngleAdd
        this.praa = this.raa
        this.offsetStartPos()
        return this
    }

    override fun collideWith(other:AEntity) {
        if (other in entitiesHit)
            return
        if (other is PassableEntity)
            return
        if (other is Enemy && !damagesEnemies)
            return
        if (other is Player && !damagesPlayers)
            return
        if (other == owner)
            return
        entitiesHit.add(other)
        if (!other.hasEffect("shield")) {
            other.damage((RandomUtils.nextInt(projProps.damage.first, projProps.damage.last) * damageMultiplier), projProps.throughDef, owner?.entityId ?: -2)
            for (i in hitEffects) other.addEffect(i)
        }
        if (!projProps.multiHit) {
            Client.world.remEntity(this, true, false)
        }
        Client.fxManager.createFx(other, other.x, other.y)
    }

    override fun clientTick() {
        prevX = x
        prevY = y
        prevDirection = direction
        praa = raa
        val local3 = ticksExisted * 20.0
        if (ticksExisted >= floor((projProps.lifetime - projProps.timeOffset * 3f) / 20f).toInt()) {
            Client.world.remEntity(this)
            return
        }
        if (positionAt(local3, pt)) {
            Client.world.remEntity(this, true, true)
            return
        }
        if (Fx[projProps.moveFx] != null) {
            Client.fxManager.createFx(this, this.x, this.y, Fx[projProps.moveFx]!!, 1, -15.0)
        }
        if (ticksExisted == 0 && projProps.timeOffset > 0.0) {
            this.prevX = lerp(this.prevX, this.x, 0.9f)
            this.prevY = lerp(this.prevY, this.y, 0.9f)
        }
        direction = if (ticksExisted == 0) {
            r - 90f
        } else if (projProps.boomerang && ticksExisted == floor(projProps.lifetime / 40f).toInt()) {
            r + 90f
        } else {
            atan2(y - prevY, x - prevX).toDegrees() - 90f
        }
        if (ticksExisted == 0 || (projProps.boomerang && ticksExisted == floor(projProps.lifetime / 40f).toInt())) {
            this.prevDirection = direction
        }
        raa += projProps.spinSpeed
    }

    private fun positionAt(time:double, pos:dvec2):bool {
        val time = time + projProps.timeOffset
        val _local_8:double
        val _local_9:double
        val _local_10:double
        val _local_11:double
        val _local_12:double
        val halfRange:double
        val _local_14:double
        pos.x = this.startPos.x
        pos.y = this.startPos.y
        var distance:double = time * projProps.speed / 1000f * tile_size
        val local4:double = if ((this.bulletId % 2) == 0) 0.0 else PI
        if (projProps.parametric) {
            _local_8 = ((time / projProps.lifetime) * 2f) * PI
            _local_9 = sin(_local_8) * if (this.bulletId % 2 == 1) 1.0 else -1.0
            _local_10 = sin((2f * _local_8)) * (if ((this.bulletId % 4) < 2) 1.0 else -1.0)
            _local_11 = cosD(this.r)
            _local_12 = cosD(this.r)
            pos.x += (_local_9 * _local_12) - (_local_10 * _local_11)
            pos.y += (_local_9 * _local_11) + (_local_10 * _local_12)
        } else {
            halfRange = (projProps.lifetime * (projProps.speed / 1000f)) / 2f * tile_size
            var bl = false
            if (projProps.boomerang) {
                if (distance > halfRange) {
                    distance = halfRange - (distance - halfRange)
                    bl = true
                }
            }
            pos.x += distance * cosD(this.r)
            pos.y += distance * sinD(this.r)
            val local15 = (if (!projProps.boomerang) (time / projProps.lifetime) else distance / halfRange)
            if (projProps.amplitude != 0.0) {
                _local_14 = (projProps.amplitude * sin(local4 + local15 * projProps.frequency * 2f * PI)) * if (bl) -1.0 else 1.0
                pos.x += _local_14 * tile_size * cosD(this.r + 90.0)
                pos.y += _local_14 * tile_size * sinD(this.r + 90.0)
            }
        }
        this.x = pt.x
        this.y = pt.y
        updateBoundingCircle()
        return Client.world.getBoundingCircles(this.boundingCircle).isNotEmpty() && !projProps.throughWalls
    }

    companion object {
        private val pt = dvec2()

        private val bulletIdsToOwner = Maps.newConcurrentMap<AEntity, int>()

        private fun nextBulletId(owner:AEntity):int {
            bulletIdsToOwner.computeIfAbsent(owner) { 0 }
            bulletIdsToOwner[owner] = bulletIdsToOwner[owner]!! + 1
            return bulletIdsToOwner[owner]!!
        }
    }
}