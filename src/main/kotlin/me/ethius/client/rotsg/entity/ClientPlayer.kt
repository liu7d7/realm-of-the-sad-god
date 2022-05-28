package me.ethius.client.rotsg.entity

import me.ethius.client.Client
import me.ethius.client.rotsg.data.ItemInfo
import me.ethius.client.rotsg.gui.Exp
import me.ethius.client.rotsg.gui.LevelUp
import me.ethius.client.rotsg.inventory.Inventory
import me.ethius.client.rotsg.item.*
import me.ethius.client.rotsg.screen.DeathScreen
import me.ethius.shared.*
import me.ethius.shared.events.Listen
import me.ethius.shared.events.def.KeyPressedEvent
import me.ethius.shared.events.def.MouseClickedEvent
import me.ethius.shared.events.def.MouseScrolledEvent
import me.ethius.shared.ext.ZERO3d
import me.ethius.shared.ext.distanceSquared
import me.ethius.shared.ext.isZero
import me.ethius.shared.ext.r
import me.ethius.shared.maths.Facing
import me.ethius.shared.network.Packet
import me.ethius.shared.opti.TexData
import me.ethius.shared.rotsg.data.Formatting
import me.ethius.shared.rotsg.entity.Stat
import me.ethius.shared.rotsg.entity.player.Player
import me.ethius.shared.rotsg.entity.player.PlayerClass
import me.ethius.shared.rotsg.entity.player.PlayerProfile
import me.ethius.shared.rotsg.tile.tile_size
import org.apache.commons.lang3.RandomUtils
import org.lwjgl.glfw.GLFW
import kotlin.math.floor

private const val sqrt2 = 1.4142135

class ClientPlayer(pClass:PlayerClass, playerProfile:PlayerProfile):Player(pClass, playerProfile) {

    var invulnerable = false
    var offsetY:double = 23.14
        get() {
            return Client.window.midY + field
        }
    private val relativeXToCamera:double
        get() = Client.cameraPos.x + x
    private val relativeYToCamera:double
        get() = Client.cameraPos.y + y
    override var exp:int
        get() = super.exp
        set(value) {
            val exp = value - super.exp
            while (value > nextLevel()) {
                this.level++
                this.onLevelUp()
            }
            super.exp = value
            this.entityNotifications.add(Exp(this, exp))
        }
    override val pivotY:double
        get() {
            return texData.pivotY * 5
        }

    override var texDataId:string = "empty"
    private var currentFacing:Facing = Facing.up
    private var prevVelocity:dvec3 = dvec3()
    var texData = TexData.empty
    val inventory:Inventory = Inventory()

    fun saveProfile() {
        this.playerProfile.set(this, this.name)
    }

    override fun release() {
        super.release()
        Client.events.unregister(this)
        inventory.release()
    }

    override fun damage(damage:double, throughDef:bool, damageSourceId:long) {
        if (!Client.options.debug && !invulnerable) {
            super.damage(damage, throughDef, damageSourceId)
        }
    }

    fun canUse(item:Item):bool {
        when (item) {
            is WeaponItem -> return this.pClass.weaponBaseClass.isAssignableFrom(item.javaClass)
            is ArmorItem -> return this.pClass.armorBaseClass.isAssignableFrom(item.javaClass)
            is AbilityItem -> return this.pClass.abilityBaseClass.isAssignableFrom(item.javaClass)
        }
        return true
    }

    override fun incStat(stat:Stat, amt:int, max:bool) {
        when (stat) {
            Stat.dex -> {
                val cap = if (max) Client.player.pClass.maxStats[stat.ordinal] - dexMaxed else int.MAX_VALUE
                dex += amt.coerceAtMost(cap).also { if (!max) dexAdd += it }
            }
            Stat.def -> {
                val cap = if (max) Client.player.pClass.maxStats[stat.ordinal] - defMaxed else int.MAX_VALUE
                def += amt.coerceAtMost(cap).also { if (!max) defAdd += it }
            }
            Stat.wis -> {
                val cap = if (max) Client.player.pClass.maxStats[stat.ordinal] - wisMaxed else int.MAX_VALUE
                wis += amt.coerceAtMost(cap).also { if (!max) wisAdd += it }
            }
            Stat.vit -> {
                val cap = if (max) Client.player.pClass.maxStats[stat.ordinal] - vitMaxed else int.MAX_VALUE
                vit += amt.coerceAtMost(cap).also { if (!max) vitAdd += it }
            }
            Stat.atk -> {
                val cap = if (max) Client.player.pClass.maxStats[stat.ordinal] - atkMaxed else int.MAX_VALUE
                atk += amt.coerceAtMost(cap).also { if (!max) atkAdd += it }
            }
            Stat.spd -> {
                val cap = if (max) Client.player.pClass.maxStats[stat.ordinal] - spdMaxed else int.MAX_VALUE
                spd += amt.coerceAtMost(cap).also { if (!max) spdAdd += it }
            }
            Stat.life -> {
                val cap = if (max) Client.player.pClass.maxStats[stat.ordinal] - lifeMaxed else int.MAX_VALUE
                life += amt.coerceAtMost(cap).also { if (!max) lifeAdd += it }
            }
            Stat.mana -> {
                val cap = if (max) Client.player.pClass.maxStats[stat.ordinal] - manaMaxed else int.MAX_VALUE
                mana += amt.coerceAtMost(cap).also { if (!max) manaAdd += it }
            }
        }
    }

    override fun clientTick() {
        this.prevX = this.x
        this.prevY = this.y
        this.prevR = this.r
        this.lastTilePos.x = this.tilePos.x
        this.lastTilePos.y = this.tilePos.y
        this.updateBoundingCircle()
        if (this.delayNumSeconds(0.04) && !hasEffect("sick")) {
            this.hp = (this.hp + this.hphs).coerceAtMost(this.life.toDouble())
        }
        if (this.delayNumSeconds(0.08)) {
            this.mp = (this.mp + this.mphs).coerceAtMost(this.maxMp.toDouble())
        }
        var h = false
        var v = false
        var f = 0f
        this.velocity.zero()
        if (Client.keyboard.areKeysDown(GLFW.GLFW_KEY_W)) {
            this.velocity.x += sin(this.r.toRadians())
            this.velocity.y += cos(this.r.toRadians())
            this.currentFacing = Facing.up
            v = true
        }
        if (Client.keyboard.areKeysDown(GLFW.GLFW_KEY_S)) {
            this.velocity.y += -cos(this.r.toRadians())
            this.velocity.x += -sin(this.r.toRadians())
            this.currentFacing = Facing.down
            v = !v
        }
        if (Client.keyboard.areKeysDown(GLFW.GLFW_KEY_A)) {
            this.velocity.y += -sin(this.r.toRadians())
            this.velocity.x += cos(this.r.toRadians())
            this.currentFacing = Facing.left
            h = true
        }
        if (Client.keyboard.areKeysDown(GLFW.GLFW_KEY_D)) {
            this.velocity.y += sin(this.r.toRadians())
            this.velocity.x += -cos(this.r.toRadians())
            this.currentFacing = Facing.right
            h = !h
        }
        val bl = !Client.keyboard.anyKeysDown(GLFW.GLFW_KEY_W, GLFW.GLFW_KEY_S, GLFW.GLFW_KEY_A, GLFW.GLFW_KEY_D)
        val f1 = Client.world.tileAt(ivec2(floor(this.x / tile_size).toInt(),
                                           floor(this.y / tile_size).toInt()))
        if (f1 != null) {
            f = f1.slippy
            this.depth = f1.depth
            if (this.delayNumSeconds(0.26)) {
                f1.onPlayerWalk(this)
            }
        }
        this.velocity.x *= (1f - (depth.toFloat() / 20f))
        this.velocity.y *= (1f - (depth.toFloat() / 20f))
        if (!bl && f != 0f) {
            this.prevVelocity = this.velocity.copy()
        }
        if (Client.keyboard.areKeysDown(GLFW.GLFW_KEY_Q)) {
            this.velocity.r = -1.6 * 2.5
        }
        if (Client.keyboard.areKeysDown(GLFW.GLFW_KEY_E)) {
            this.velocity.r = 1.6 * 2.5
        }
        val divBy = if (v && h) sqrt2 else 1.0
        if (bl) {
            if (f == 0f) {
                this.prevVelocity = ZERO3d.copy()
            } else {
                this.velocity.x = f * this.prevVelocity.x
                this.velocity.y = f * this.prevVelocity.y
                this.prevVelocity.x = this.velocity.x
                this.prevVelocity.y = this.velocity.y
            }
        }
        if (!this.velocity.isZero()) {
            move(-(this.velocity.x * this.tps / divBy), -(this.velocity.y * this.tps / divBy))
            this.r -= this.velocity.r
        }
        this.tilePos.x = floor(this.x / tile_size).toInt()
        this.tilePos.y = floor(this.y / tile_size).toInt()
        if (measuringTimeMS() - lastShot >= 1000f / aps) {
            if (!Client.isPaused) {
                Client.run {
                    if (mouse.isKeyDown(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
                        player.inventory.weapon.getWeapon()?.onShoot(player)
                        for (i in MainPlayerHook.on_shoot.hooked) {
                            i(this@ClientPlayer)
                        }
                    }
                }
            }
            lastShot = measuringTimeMS()
        }
        if (pos.distanceSquared(this.lastWalkTexUpdate) > tile_size * tile_size) {
            this.lastWalkTexUpdate.x = this.x
            this.lastWalkTexUpdate.y = this.y
            this.walkTex++
        }
        for (i in MainPlayerHook.on_tick.hooked) {
            i(this)
        }
        this.updateTexture()
        this.width = texData.width * 5
        this.height = texData.height * 5
        if (!alive) {
            Client.network.send(Packet._id_logoff)
            Client.screen = DeathScreen()
            Client.world.remEntity(this)
        }
        Client.network.send(Packet._id_move, this.x, this.y)
        Client.network.send(Packet._id_hp_update, this.hp)
    }

    private fun updateTexture() {
        this.texXOffset = 0.0
        val shooting = Client.mouse.leftDown() && this.inventory.weapon.item is WeaponItem
        val facing = if (shooting) {
            when (wrapDegrees(calcAngle(-(this.relativeYToCamera - Client.mouse.y),
                                        -(this.relativeXToCamera - Client.mouse.x)))) {
                in -135f..-45f -> Facing.up
                in -45f..45f -> Facing.right
                in 45f..135f -> Facing.down
                in 135f..180f, in -180f..-135f -> Facing.left
                else -> this.currentFacing
            }
        } else {
            this.currentFacing
        }
        val shootAnim = (measuringTimeMS() - lastShot) < (500f / aps)
        this.texData = when (facing) {
            Facing.up -> {
                texXOffset = 0.024
                if (shooting) {
                    if (shootAnim) {
                        this.pTexData.up_s1
                    } else {
                        this.pTexData.up_s2
                    }
                } else if (!velocity.isZero()) {
                    when (walkTex % 2) {
                        1 -> this.pTexData.up_w1
                        0 -> this.pTexData.up_w2
                        else -> this.pTexData.up_w1
                    }
                } else {
                    this.pTexData.up
                }
            }
            Facing.down -> {
                texXOffset = -0.024
                if (shooting) {
                    if (shootAnim) {
                        this.pTexData.down_s1
                    } else {
                        this.pTexData.down_s2
                    }
                } else if (!velocity.isZero()) {
                    when (walkTex % 2) {
                        1 -> this.pTexData.down_w1
                        0 -> this.pTexData.down_w2
                        else -> this.pTexData.down_w1
                    }
                } else {
                    this.pTexData.down
                }
            }
            Facing.left -> {
                if (shooting) {
                    texXOffset = -(this.pTexData.shootingWidth - 8) * 0.476
                    if (shootAnim) {
                        this.pTexData.left_s1
                    } else {
                        this.pTexData.left_s2
                    }
                } else if (!velocity.isZero()) {
                    when (walkTex % 2) {
                        1 -> this.pTexData.left_w1
                        0 -> this.pTexData.left_w2
                        else -> this.pTexData.left_w1
                    }
                } else {
                    this.pTexData.left
                }
            }
            Facing.right -> {
                if (shooting) {
                    texXOffset = (this.pTexData.shootingWidth - 8) * 0.476
                    if (shootAnim) {
                        this.pTexData.right_s1
                    } else {
                        this.pTexData.right_s2
                    }
                } else if (!velocity.isZero()) {
                    when (walkTex % 2) {
                        1 -> this.pTexData.right_w1
                        0 -> this.pTexData.right_w2
                        else -> this.pTexData.right_w1
                    }
                } else {
                    this.pTexData.right
                }
            }
        }
    }

    private fun onLevelUp() {
        if (this.level <= 20 && !rawExp) {
            this.incStat(Stat.life, RandomUtils.nextInt(20, 31), true)
            this.incStat(Stat.mana, RandomUtils.nextInt(2, 9), true)
            this.incStat(Stat.atk, RandomUtils.nextInt(1, 3), true)
            this.incStat(Stat.spd, RandomUtils.nextInt(1, 3), true)
            this.incStat(Stat.dex, RandomUtils.nextInt(1, 3), true)
            this.incStat(Stat.vit, RandomUtils.nextInt(1, 3), true)
            this.incStat(Stat.wis, RandomUtils.nextInt(1, 3), true)
        }
        this.hp = this.life.toDouble()
        this.mp = this.mana.toDouble()
        this.entityNotifications.add(LevelUp(this))
    }

    @Listen
    fun scroll(event:MouseScrolledEvent) {
        if (!Client.keyboard.areKeysDown(GLFW.GLFW_KEY_LEFT_CONTROL) && !Client.keyboard.areKeysDown(GLFW.GLFW_KEY_LEFT_ALT)) {
            this.offsetY += -Client.window.midY + event.modifier * 50
        }
    }

    @Listen
    fun key(event:KeyPressedEvent) {
        if (event.action == GLFW.GLFW_PRESS && event.key == GLFW.GLFW_KEY_Z)
            this.r = 0.0
        if (event.action == GLFW.GLFW_PRESS && event.key == GLFW.GLFW_KEY_R && Client.worldInit) {
            if ("nexus" !in Client.world.name) {
                Client.network.send(Packet._id_world_request, "nexus")
            } else {
                Client.inGameHud.chatHud.addChat("${Formatting.red}You are already in the nexus!")
            }
        }
    }

    @Listen
    fun click(event:MouseClickedEvent) {
        if (this == null) return
        if (this != Client.player) return
        if (event.action == GLFW.GLFW_PRESS && event.button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && !inventory.isIn(event.x.toDouble(), event.y.toDouble())) {
            val item = this.inventory.ability.getItemForUse()
            if (item is AbilityItem)
                item.onAbilityUse()
        }
    }

    enum class MainPlayerHook {
        on_shoot, on_tick, none;

        val hooked:ArrayList<(ClientPlayer) -> void> = ArrayList()
    }

    fun hasLegendaryEffect(effect:LegendaryEffect) =
        effect == this.inventory.ability.item.legendaryEffect || effect == this.inventory.weapon.item.legendaryEffect || effect == this.inventory.armor.item.legendaryEffect || effect == this.inventory.ring.item.legendaryEffect

    init {
        init()
        Client.events.register(this)
        this.pClass.initInventory(this.inventory)
    }

    companion object {
        fun load(profileIn:PlayerProfile):ClientPlayer {
            Client.player = ClientPlayer(PlayerClass.valueOf(profileIn.clazz), profileIn)
            Client.player.inventory.slots.forEachIndexed { index, slot ->
                if (index in profileIn.items.indices) {
                    val item = ItemInfo.values.find { it.id == profileIn.items[index] }
                    if (item != null) {
                        slot.item = item()
                    }
                }
            }
            return Client.player
        }

        fun copyToMainProfile() {
            Client.player.playerProfile.set(Client.player,
                                            Client.player.name)
        }
    }

}