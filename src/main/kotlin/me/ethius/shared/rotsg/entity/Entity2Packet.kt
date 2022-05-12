package me.ethius.shared.rotsg.entity

import me.ethius.client.Client
import me.ethius.client.rotsg.data.ItemInfo
import me.ethius.client.rotsg.entity.Bag
import me.ethius.client.rotsg.entity.BagTier
import me.ethius.client.rotsg.entity.OtherPlayer
import me.ethius.client.rotsg.entity.Portal
import me.ethius.server.rotsg.entity.ServerPlayer
import me.ethius.shared.network.Packet
import me.ethius.shared.rotsg.data.EffectInfo
import me.ethius.shared.rotsg.data.ProjectileData
import me.ethius.shared.rotsg.entity.enemy.Aoe
import me.ethius.shared.rotsg.entity.enemy.Enemy
import me.ethius.shared.rotsg.entity.other.Projectile
import me.ethius.shared.rotsg.entity.player.PlayerClass
import me.ethius.shared.rotsg.entity.player.PlayerProfile
import me.ethius.shared.string

fun AEntity.createSpawnPacket(): Packet {

    // god this is messy

    val data:Array<Any> = when (this) {
        is Enemy -> {
            arrayOf(
                this.texDataId,
                this.scale,
                this.life,
                this.def,
                this.dex,
                this.exp
            )
        }
        is Portal -> {
            arrayOf(
                this.texDataId,
                this.worldId,
                this.name,
                this.invulnerable
            )
        }
        is Bag -> {
            Array(9) {
                if (it == 0) {
                    this.bagTier
                } else if ((it - 1) in this.rawItems.indices) {
                    this.rawItems[it - 1]
                } else {
                    "air"
                }
            }
        }
        is Projectile -> {
            arrayOf(
                this.texDataId,
                this.owner?.entityId ?: -1,
                this.r,
                this.amplitude,
                this.frequency,
                this.speed,
                this.lifetime,
                this.multiHit,
                this.throughDef,
                this.parametric,
                this.boomerang,
                this.damage.first,
                this.damage.last,
                this.spinSpeed,
                this.frequencyOffset,
                this.timeOffset,
                this.throughWalls,
                this.horizontalOffset,
                this.moveFx,
                this.leadShot,
                this.damageMultiplier,
                this.scale,
                this.raa,
                *this.hitEffects.map { "${it.id}|${it.lifetime}" }.toTypedArray()
            )
        }
        is Aoe -> {
            arrayOf(
                this.texDataId,
                this.owner.entityId,
                this.direction,
                this.lifetime,
                this.maxDist,
                this.speed,
                this.radius,
                this.damage
            )
        }
        is ServerPlayer -> {
            arrayOf(
                this.pClass.name,
                this.playerProfile.toTomlString()
            )
        }
        else -> {
            throw IllegalArgumentException("Unknown entity type ${this::class.simpleName}")
        }
    }
    return Packet(Packet._id_spawn_entity, this.entityId, "${this.javaClass.simpleName.lowercase()}|${this.world?.name}", this.x, this.y, *data)
}

fun AEntity.Companion.getWorldNameFromSpawnPacket(packet:Packet):string {
    val _spl = packet.data[1].split("|")
    return _spl[1]
}

fun AEntity.Companion.fromSpawnPacket(packet:Packet):AEntity? {
    val entityId = packet.data[0].toLong()
    val _spl = packet.data[1].split("|")
    val type = _spl[0]
    val x = packet.data[2].toDouble()
    val y = packet.data[3].toDouble()
    when (type) {
        "enemy" -> {
            val texDataId = packet.data[4]
            val scale = packet.data[5].toDouble()
            val life = packet.data[6].toInt()
            val def = packet.data[7].toInt()
            val dex = packet.data[8].toInt()
            val exp = packet.data[9].toInt()
            return Enemy().also {
                it.entityId = entityId
                it.setTexData(texDataId)
                it.scale = scale * 0.2
                it.life = life
                it.hp = life.toDouble()
                it.def = def
                it.dex = dex
                it.exp = exp
                it.moveTo(x, y, true)
            }
        }
        "portal" -> {
            val texDataId = packet.data[4]
            val worldId = packet.data[5]
            val name = packet.data[6]
            val invulnerable = packet.data[7].toBoolean()
            return Portal(texDataId, worldId, name).also {
                it.entityId = entityId
                it.invulnerable = invulnerable
                it.moveTo(x, y, true)
            }
        }
        "bag" -> {
            val bagTier = BagTier.valueOf(packet.data[4])
            val rawItems = packet.data.sliceArray(5 until packet.data.size).map { ItemInfo[it]() }
            return Bag(bagTier, rawItems).also {
                it.entityId = entityId
                it.moveTo(x, y, true)
            }
        }
        "projectile" -> {
            val texDataId = packet.data[4]
            val ownerId = packet.data[5].toLong()
            val r = packet.data[6].toDouble()
            val amplitude = packet.data[7].toDouble()
            val frequency = packet.data[8].toDouble()
            val speed = packet.data[9].toDouble()
            val lifetime = packet.data[10].toDouble()
            val multiHit = packet.data[11].toBoolean()
            val throughDef = packet.data[12].toBoolean()
            val parametric = packet.data[13].toBoolean()
            val boomerang = packet.data[14].toBoolean()
            val damage = Pair(packet.data[15].toInt(), packet.data[16].toInt())
            val spinSpeed = packet.data[17].toDouble()
            val frequencyOffset = packet.data[18].toDouble()
            val timeOffset = packet.data[19].toDouble()
            val throughWalls = packet.data[20].toBoolean()
            val horizontalOffset = packet.data[21].toDouble()
            val moveFx = packet.data[22]
            val leadShot = packet.data[23].toBoolean()
            val damageMultiplier = packet.data[24].toDouble()
            val scale = packet.data[25].toDouble()
            val raa = packet.data[26].toDouble()
            val effects = packet.data.sliceArray(27 until packet.data.size).mapNotNull {
                val split = (it).split("|")
                EffectInfo[split[0]]?.let { it(split[1].toLong()) }
            }
            return Client.world.getEntityById(ownerId)?.let {
                Projectile().reset(
                    it,
                    ProjectileData().also {
                        it.texDataId = texDataId
                        it.amplitude = amplitude
                        it.frequency = frequency
                        it.speed = speed
                        it.lifetime = lifetime
                        it.multiHit = multiHit
                        it.throughDef = throughDef
                        it.parametric = parametric
                        it.boomerang = boomerang
                        it.damage = damage.first..damage.second
                        it.spinSpeed = spinSpeed
                        it.frequencyOffset = frequencyOffset
                        it.timeOffset = timeOffset
                        it.throughWalls = throughWalls
                        it.horizontalOffset = horizontalOffset
                        it.moveFx = moveFx
                        it.leadShot = leadShot
                        it.scale = scale
                        it.renderAngleAdd = raa
                    },
                    r
                ).also {
                    it.entityId = entityId
                    it.hitEffects.addAll(effects)
                    it.damageMultiplier = damageMultiplier
                }
            }
        }
        "aoe" -> {
            val texDataId = packet.data[4]
            val ownerId = packet.data[5].toLong()
            val direction = packet.data[6].toDouble()
            val lifetime = packet.data[7].toDouble()
            val maxDist = packet.data[8].toDouble()
            val speed = packet.data[9].toDouble()
            val radius = packet.data[10].toDouble()
            val damage = packet.data[11].toDouble()

            return Client.world.getEntityById(ownerId)?.let { owner ->
                Aoe(owner.x, owner.y).also {
                    it.entityId = entityId
                    it.owner = owner
                    it.texDataId = texDataId
                    it.direction = direction
                    it.lifetime = lifetime
                    it.maxDist = maxDist
                    it.speed = speed
                    it.radius = radius
                    it.damage = damage
                }
            }
        }
        "serverplayer" -> {
            val pClass = PlayerClass[packet.data[4]]
            val profile = PlayerProfile.read(packet.data[5], false)

            return OtherPlayer(pClass, profile).also {
                it.entityId = entityId
                it.move(x, y)
                it.name = profile.name
            }
        }
        else -> {
            throw IllegalArgumentException("Unknown entity type: $type")
        }
    }
}