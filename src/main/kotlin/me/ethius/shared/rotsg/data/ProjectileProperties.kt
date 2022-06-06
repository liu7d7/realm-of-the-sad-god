package me.ethius.shared.rotsg.data

import com.moandjiezana.toml.Toml
import me.ethius.shared.*
import me.ethius.shared.ext.getInt
import me.ethius.shared.opti.TexData
import me.ethius.shared.rotsg.tile.tile_size
import org.apache.commons.lang3.RandomUtils
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.jvmErasure

const val proj_data_loc = "/assets/data/entity/proj"

class ProjectileProperties {

    var texDataId:string = "empty"
    var amplitude:double = 0.0
    var frequency:double = 0.0
    var speed:double = 0.0
    var multiHit:bool = false
    var throughDef:bool = false
    var parametric:bool = false
    var boomerang:bool = false
    var damage:IntRange = 0..0
    var scale:double = 0.75
    var lifetime:double = 0.0
    val extraData = HashMap<string, Any>()
    var spinSpeed = 0.0
        get() = field * 0.4f
    var atPlayer = false
    var renderAngleAdd = 0.0
    var frequencyOffset = 0.0
    var timeOffset = 0.0
    var horizontalOffset:double = 0.0
    var atMouse:bool = false
    var moveFx:string? = null
    var baseAngle = 0.0
        get() {
            if (randomBaseAngle) {
                return RandomUtils.nextDouble(0.0, 360.0)
            }
            return field
        }
    var throughWalls = false
    var randomBaseAngle = false
    var leadShot = false
    var hitEffect:string = "NIL"
    var hitEffectDuration = 0L
    var hitEffectAmplifier = 0
    var bulletIdAlwaysEven = false
    lateinit var id:string

    fun copy():ProjectileProperties {
        val data = ProjectileProperties()
        data.texDataId = texDataId
        data.amplitude = amplitude
        data.frequency = frequency
        data.speed = speed
        data.multiHit = multiHit
        data.throughDef = throughDef
        data.parametric = parametric
        data.boomerang = boomerang
        data.damage = damage
        data.scale = scale
        data.lifetime = lifetime
        data.extraData.putAll(extraData)
        data.spinSpeed = spinSpeed
        data.atPlayer = atPlayer
        data.renderAngleAdd = renderAngleAdd
        data.frequencyOffset = frequencyOffset
        data.timeOffset = timeOffset
        data.horizontalOffset = horizontalOffset
        data.moveFx = moveFx
        data.baseAngle = baseAngle
        data.throughWalls = throughWalls
        data.randomBaseAngle = randomBaseAngle
        data.leadShot = leadShot
        data.id = id
        data.atMouse = true
        return data
    }

    constructor()

    constructor(
        texData:TexData,
        amplitude:double,
        frequency:double,
        speed:double,
        range:double,
        multiHit:bool,
        throughDef:bool,
        parametric:bool,
        boomerang:bool,
        damage:IntRange,
        scale:double = 0.8,
        freqOff:double = 0.0,
        timeOff:double = 0.0,
        throughWalls:bool = false,
    ):this() {
        this.texDataId = texData.id
        this.amplitude = amplitude
        this.frequency = frequency
        this.speed = speed
        this.multiHit = multiHit
        this.throughDef = throughDef
        this.parametric = parametric
        this.boomerang = boomerang
        this.damage = damage
        this.scale = scale
        this.lifetime = ms(range, speed)
        this.frequencyOffset = freqOff
        this.timeOffset = timeOff
        this.throughWalls = throughWalls
    }

    constructor(assetLoc:string):this() {
        val toml = Toml().readCached("$proj_data_loc/$assetLoc.dat")
        val meta = toml.getTable("meta")
        this.texDataId = meta.getString("tex_data")
        this.amplitude = meta.getDouble("amplitude", 0.0)
        this.frequency = meta.getDouble("frequency", 0.0) * 1.28
        this.speed = meta.getDouble("speed", 0.0)
        this.multiHit = meta.getBoolean("multi_hit", false)
        this.throughDef = meta.getBoolean("through_def", false)
        this.parametric = meta.getBoolean("parametric", false)
        this.boomerang = meta.getBoolean("boomerang", false)
        this.damage = meta.getString("damage", "0-0").let { val e = it.split("-"); IntRange(e[0].toInt(), e[1].toInt()) }
        this.scale = meta.getDouble("scale", 0.8)
        this.atPlayer = meta.getBoolean("at_player", false)
        this.renderAngleAdd = meta.getDouble("render_angle_add", 0.0)
        if (meta.containsTable("extra_data")) {
            this.extraData.putAll(meta.getTable("extra_data").toMap())
        }
        this.spinSpeed = meta.getDouble("spin_speed", 0.0)
        this.lifetime = if (meta.contains("range")) {
            ms(meta.getDouble("range", 0.0) * if (boomerang) 2 else 1, speed)
        } else {
            meta.getDouble("lifetime", 0.0) * if (boomerang) 2 else 1
        }
        this.randomBaseAngle = meta.getBoolean("random_base_angle", false)
        this.frequencyOffset = meta.getDouble("frequency_offset", 0.0)
        this.timeOffset = meta.getDouble("time_offset", 0.0)
        this.throughWalls = meta.getBoolean("through_walls", false)
        this.horizontalOffset = meta.getDouble("horizontal_offset", 0.0) * tile_size
        this.moveFx = meta.getString("move_fx", "")
        this.hitEffect = meta.getString("hit_effect", this.hitEffect)
        this.hitEffectAmplifier = meta.getInt("hit_effect_amplifier", this.hitEffectAmplifier)
        this.hitEffectDuration = meta.getLong("hit_effect_duration", this.hitEffectDuration)
        this.bulletIdAlwaysEven = meta.getBoolean("bullet_id_always_even", this.bulletIdAlwaysEven)
        this.atMouse = meta.getBoolean("at_mouse", false)
    }

    @Suppress("UNUSED")
    companion object {
        val values = ArrayList<ProjectileProperties>()

        val empty = ProjectileProperties()

        const val slant_angle_correction = -45.0
        const val horizontal_angle_correction = -90.0

        fun make(lambda:ProjectileProperties.() -> void):ProjectileProperties = ProjectileProperties().apply(lambda)

        val basic_proj =
            ProjectileProperties(TexData.basic_proj_1, 0.0, 0.0, 12.0, 4.5, false, false, false, false, 25..45, 0.75)
        val basic_proj_x1 = ProjectileProperties(TexData.basic_proj_1,
                                                 0.0,
                                                 0.0,
                                                 10.0,
                                                 3.5,
                                                 false,
                                                 false,
                                                 false,
                                                 false,
                                                 25..45,
                                                 0.8).also { it.randomBaseAngle = true }
        val basic_proj_x2 = { damage:IntRange, maxDist:double, speed:double ->
            ProjectileProperties(TexData.basic_proj_1,
                                 0.0,
                                 0.0,
                                 speed,
                                 maxDist,
                                 false,
                                 false,
                                 false,
                                 false,
                                 damage,
                                 0.8)
        }

        val flayer_proj_1 = ProjectileProperties(TexData.colo_proj,
                                                 0.0,
                                                 0.0,
                                                 7.5,
                                                 5.6,
                                                 false,
                                                 false,
                                                 false,
                                                 false,
                                                 80..95,
                                                 0.8).also { it.atPlayer = true; it.renderAngleAdd = slant_angle_correction }

        val rotten_shot = { atPlayer:bool, damage:IntRange ->
            ProjectileProperties(TexData.rotting_arm_proj,
                                 0.0,
                                 0.0,
                                 8.0,
                                 4.3,
                                 false,
                                 false,
                                 false,
                                 false,
                                 damage,
                                 0.8).also { it.atPlayer = atPlayer }
        }

        val white_demon_shot = { angle:double ->
            ProjectileProperties(TexData.white_demon_proj,
                                 0.0,
                                 0.0,
                                 6.0,
                                 5.0,
                                 false,
                                 false,
                                 true,
                                 false,
                                 95..95,
                                 0.8).also { it.baseAngle = angle; it.spinSpeed = 50.0; it.atPlayer = true }
        }

        val salmon_proj =
            { damage:IntRange, maxDist:double, speed:double, randomBaseAngle:bool ->
                ProjectileProperties(TexData.salmon_missile_1, 0.0, 0.0, speed, maxDist,
                                     false, false, false, false, damage, 0.8).also { it.randomBaseAngle = randomBaseAngle; it.renderAngleAdd = slant_angle_correction }
            }

        val adv_proj = { angle:double ->
            ProjectileProperties(TexData.adv_proj_1, 0.0, 0.0, 12.0, 3.25,
                                 true, false, false, false, 75..105, 0.8).also { it.baseAngle = angle }
        }

        val basic_arrow = ProjectileProperties(TexData.basic_arrow, 0.0, 0.0, 15.0, 7.5,
                                               true, false, false, false, 20..35).also { it.renderAngleAdd = slant_angle_correction }

        val fb_1_proj = ProjectileProperties("Fb_1_proj")
        val fb_2_proj = ProjectileProperties("Fb_2_proj")
        val fb_3_proj = ProjectileProperties("Fb_3_proj")
        val basic_ninja_star_proj =
            ProjectileProperties("BasicNinjaStar_proj")
        val basic_ninja_star_2_proj =
            ProjectileProperties("BasicNinjaStar_2_proj")
        val basic_quiver_proj = ProjectileProperties("BasicQuiver_proj")
        val basic_quiver_2_proj = ProjectileProperties("BasicQuiver_2_proj")
        val murena_proj = ProjectileProperties("Murena_proj")

        // hammer
        val metallic_robot_proj_1 = ProjectileProperties("MetallicRobot_proj_1")

        // moonbeam
        val metallic_robot_proj_2 = ProjectileProperties("MetallicRobot_proj_2")

        // rings
        val metallic_robot_proj_3 = ProjectileProperties("MetallicRobot_proj_3")

        val kalon_proj = ProjectileProperties("Kalon_proj")
        val frozen_dagger_proj = ProjectileProperties("FrozenDagger_proj")
        val rotting_dude2_proj = ProjectileProperties("RottingDude_2_proj")
        val rotting_boss_proj = ProjectileProperties("RottingBoss_proj_1")
        val dual_crystal_cutters_proj = ProjectileProperties("DualCrystalCutters_proj")

        val crystal_dude_proj_1 = ProjectileProperties("CrystalDude_proj_1")
        val crystal_dude_proj_2 = ProjectileProperties("CrystalDude_proj_2")
        val crystal_dude_proj_3 = ProjectileProperties("CrystalDude_proj_3")

        val hermit_crab_proj_1 = ProjectileProperties("HermitCrab_proj_1")
        val hermit_crab_proj_2 = ProjectileProperties("HermitCrab_proj_2")
        val hermit_crab_proj_3 = ProjectileProperties("HermitCrab_proj_3")
        val hermit_crab_proj_4 = ProjectileProperties("HermitCrab_proj_4")

        val elemental_dude_proj_1 = ProjectileProperties("ElementalDude_proj_1")
        val elemental_dude_proj_2 = ProjectileProperties("ElementalDude_proj_2")

        val elemental_slasher_proj = ProjectileProperties("ElementalSlasher_proj_1")

        val basic_dagger_1_proj = ProjectileProperties("BasicDagger_1_proj")

        val fire_breather_tail_proj = ProjectileProperties("FireBreatherTail_proj")

        val warbringers_dagger_proj = ProjectileProperties("WarbringersDagger_proj")

        val warbringers_bow_proj_1 = ProjectileProperties("WarbringersBow_proj_1")
        val warbringers_bow_proj_2 = ProjectileProperties("WarbringersBow_proj_2")

        val warbringer_proj = ProjectileProperties("Warbringer_proj")
        val warbringers_proj_1 =
            ProjectileProperties("WarbringersDagger_proj").also { it.atPlayer = true; it.speed = 8.0; it.lifetime = ms(4.5, 8.0) }
        val warbringers_proj_2 =
            ProjectileProperties("WarbringersBow_proj_1").also { it.atPlayer = true; it.speed = 8.0; it.lifetime = ms(6.2, 8.0) }

        val medusa_proj = ProjectileProperties("Medusa_proj")
        val flying_brain_proj = ProjectileProperties("FlyingBrain_proj")

        val dasher_1_proj = ProjectileProperties("Dasher_1_proj")
        val dasher_2_proj = ProjectileProperties("Dasher_2_proj")

        val shadow_scale_proj_1 = ProjectileProperties("ShadowScale_proj_1")
        val shadow_scale_proj_2 = ProjectileProperties("ShadowScale_proj_2")

        val fire_breather_proj_1 = ProjectileProperties("FireBreather_proj_1")
        val fire_breather_proj_2 = ProjectileProperties("FireBreather_proj_2")

        val diviners_old_stick_proj = ProjectileProperties("DivinersOldStick_proj")
        val golden_wand_proj = ProjectileProperties("GoldenWand_proj")

        val elemental_saber_proj = ProjectileProperties("ElementalSaber_proj")

        val flowering_katana_proj = ProjectileProperties("FloweringKatana_proj")

        val shadow_crusher_proj = make {
            texDataId = TexData.shadow_scale_proj_1.id
            amplitude = 0.2
            frequency = 2.0
            speed = 0.8
            lifetime = ms(3.5, speed)
            multiHit = true
            damage = 250..350
            scale = 0.82
            renderAngleAdd = -45.0
        }

        val stem_of_the_brain_proj = ProjectileProperties("StemOfTheBrain_proj")

        val lightning_bolt_proj = ProjectileProperties("LightningBolt_proj")

        fun ms(range:double, speed:double):double {
            return range / speed * 1000.0
        }

        operator fun get(name:string?):ProjectileProperties {
            return values.find { name == it.id } ?: empty
        }

        fun init() {
            for (i in this::class.declaredMemberProperties) {
                if (i.returnType.jvmErasure == ProjectileProperties::class) {
                    val data = (i as KProperty1<Any, *>).get(this) as ProjectileProperties
                    data.id = i.name
                    values.add(data)
                }
            }
        }
    }
}