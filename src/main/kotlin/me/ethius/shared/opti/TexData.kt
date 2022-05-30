package me.ethius.shared.opti

import me.ethius.client.main_tex
import me.ethius.shared.*
import me.ethius.shared.maths.WeightedCollection
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.jvmErasure

open class TexData(
    open val u:double,
    open val v:double,
    open val width:double,
    open val height:double,
    open var pivotX:double = 0.0,
    open var pivotY:double = -(height / 2f - 0.5f),
    var id:string = "",
    var type:Type = stageInit,
) {

    var randomRotation:bool = true
    var blend:bool = true
    var avgColor = 0x00000000L

    constructor(
        u:float,
        v:float,
        width:float,
        height:float,
        pivotX:float = 0f,
        pivotY:float = -(height / 2f - 0.5f),
    ):this(u.toDouble(),
           v.toDouble(),
           width.toDouble(),
           height.toDouble(),
           pivotX.toDouble(),
           pivotY.toDouble())

    constructor(
        u:int,
        v:int,
        width:int,
        height:int,
    ):this(u.toFloat(),
           v.toFloat(),
           width.toFloat(),
           height.toFloat())

    enum class Type {
        tile, bushery, player, item, entity, other, projectile
    }

    open fun texData(seed:int, wind:float):TexData {
        return this
    }

    open fun getAvgColor() {
        if (this.type == Type.tile) {
            val start = (main_tex.width * v + u).toInt()
            val end = (main_tex.width * (v + height) + u + width).toInt()
            var firstRun = true
            for (i in start * 4..end * 4 step 4) {
                val red = main_tex.data!![i].toUByte().toInt()
                val grn = main_tex.data!![i + 1].toUByte().toInt()
                val blu = main_tex.data!![i + 2].toUByte().toInt()
                val alpha = main_tex.data!![i + 3].toUByte().toInt()
                val argb = ((alpha shl 24) or (red shl 16) or (grn shl 8) or blu).toLong()
                if (argb == 0L) {
                    continue
                }
                if (firstRun) {
                    this.avgColor = argb
                    firstRun = false
                } else {
                    avgColor = interpolateColor(avgColor,
                                                argb,
                                                0.5f)
                }
            }
        }
    }

    override fun equals(other:Any?):Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TexData

        if (id != other.id) return false
        return true
    }

    override fun toString():string {
        return "TexData(u=$u, v=$v, width=$width, height=$height, pivotX=$pivotX, pivotY=$pivotY, id='$id', type=$type)"
    }

    override fun hashCode():Int {
        return 31 * id.hashCode()
    }

    @Suppress("UNUSED")
    companion object {
        val values = HashMap<string, TexData>()
        private var stageInit:Type = Type.item

        // MAIN PLAYER //
        val ninja = PlayerTexData(1f, 1f)
        val nature_ninja = PlayerTexData(1f, 51f)
        val new_ninja:PlayerTexData = NewNinjaSkin()
        val archer = PlayerTexData(1f, 26f)
        val warrior = PlayerTexData(72f, 26f)
        val nature_archer = PlayerTexData(72f, 1f)
        val crystal_warrior = PlayerTexData(143f, 1f)
        val frimar_warrior = PlayerTexData(143f, 26f)
        val dasher = PlayerTexData(214f, 1f)
        val key_dasher = PlayerTexData(214f, 26f)
        val traffic_cone_dasher = PlayerTexData(72, 51, 13)
        val bone_warrior = PlayerTexData(214, 51, 12)
        val demon_dasher = PlayerTexData(214, 75, 11)
        val kunoichi = PlayerTexData(213, 100, 11)
        val diviner = PlayerTexData(285, 1, 10)
        val knight = PlayerTexData(285, 26, 11)

        // ITEMS //
        init {
            stageInit = Type.item
        }
        // katanas //
        val basic_katana_1 = TexData(9, 309, 8, 8)
        val basic_katana_2 = TexData(72, 318, 8, 8)
        val moonbeam_katana = TexData(81, 318, 8, 8)
        val stardust_cutter = TexData(90, 318, 8, 8)
        val molten_katana = TexData(63, 318, 8, 8)
        val kalon = TexData(99, 318, 8, 8)
        val frozen_katana = TexData(108, 318, 8, 8)
        val dead_man_scythe = TexData(36, 309, 8, 8)
        val dual_crystal_cutters = TexData(45, 309, 8, 8)
        val elemental_slasher = TexData(90, 309, 8, 8)
        val stem_of_the_brain = TexData(0, 309, 8, 8)
        val lightning_bolt = TexData(89, 336, 8, 8)
        val flowering_katana = TexData(0, 345, 8, 8)
        val demons_fang = TexData(45, 345, 8, 8)

        // daggers //
        val basic_dagger_1 = TexData(36, 318, 8, 8)
        val fire_breather_tail = TexData(45, 318, 8, 8)
        val warbringers_dagger = TexData(27, 318, 8, 8)
        val agateclaw_dagger = TexData(126, 309, 8, 8)
        val adv_dagger_1 = TexData(99, 327, 8, 8)
        val bloody_blade = TexData(18, 345, 8, 8)
        val demons_horn = TexData(36, 345, 8, 8)

        // abilities //
        val adv_shield_1 = TexData(126, 318, 8, 8)
        val basic_shield_1 = TexData(117, 318, 8, 8)
        val ninja_star_1 = TexData(135, 318, 8, 8)
        val ninja_star_2 = TexData(63, 309, 8, 8)
        val quiver_1 = TexData(0, 327, 8, 8)
        val quiver_2 = TexData(72, 309, 8, 8)
        val basic_dasher = TexData(9, 327, 8, 8)
        val adv_dasher = TexData(54, 318, 8, 8)
        val orb_1 = TexData(108, 336, 8, 8)

        // rings //
        val bone_ring = TexData(18, 327, 8, 8)
        val basic_ring_1 = TexData(27, 327, 8, 8)
        val extreme_ring_of_atk = TexData(18, 309, 8, 8)
        val radiant_ring = TexData(9, 345, 8, 8)

        // armors //
        val heavy_armor_1 = TexData(36, 327, 8, 8)
        val heavy_armor_2 = TexData(45, 327, 8, 8)
        val leather_armor_1 = TexData(54, 327, 8, 8)
        val leather_armor_2 = TexData(63, 327, 8, 8)
        val bone_armor = TexData(27, 309, 8, 8)
        val elemental_heavy_armor = TexData(0, 318, 8, 8)
        val elemental_light_armor = TexData(9, 318, 8, 8)
        val the_scorched_armor = TexData(81, 309, 8, 8)
        val robe_1 = TexData(117, 336, 8, 8)

        // swords //
        val colo_sword_1 = TexData(72, 327, 8, 8)
        val rotting_arm = TexData(81, 327, 8, 8)
        val basic_sword = TexData(90, 327, 8, 8)
        val crystal_sword = TexData(99, 309, 8, 8)
        val murena = TexData(0, 336, 8, 8)
        val elemental_saber = TexData(108, 309, 8, 8)
        val warbringers_lance = TexData(133, 309, 8, 8)
        val shadow_crusher = TexData(117, 309, 8, 8)

        // wands //
        val diviners_old_stick = TexData(99, 336, 8, 8)
        val golden_wand = TexData(126, 336, 8, 8)

        // bows //
        val basic_bow = TexData(126, 327, 8, 8)
        val void_bow = TexData(117, 327, 8, 8)
        val doom_bow = TexData(108, 327, 8, 8)
        val ice_bow = TexData(135, 327, 8, 8)
        val crystal_bow = TexData(54, 309, 8, 8)
        val warbringers_bow = TexData(18, 318, 8, 8)

        // potions //
        val life_potion = TexData(45, 336, 8, 8)
        val mana_potion = TexData(36, 336, 8, 8)
        val spd_potion = TexData(81, 336, 8, 8)
        val vit_potion = TexData(63, 336, 8, 8)
        val wis_potion = TexData(54, 336, 8, 8)
        val dex_potion = TexData(72, 336, 8, 8)
        val def_potion = TexData(18, 336, 8, 8)
        val atk_potion = TexData(27, 336, 8, 8)
        val apple = TexData(9, 336, 8, 8)


        // TILES //
        init {
            stageInit = Type.tile
        }
        val grass_1 = TexData(202, 193, 8, 8)
        val sand_1 = TexData(192, 203, 8, 8)
        val water_1 = FlowingTexData(TexData(414, 222, 16, 16), 8.0, 8.0)
        val border_water_1 = FlowingTexData(TexData(432, 222, 16, 16), 8.0, 8.0)
        val grass_2 = TexData(192, 253, 8, 8)
        val gore_1 = TexData(192, 213, 8, 8)
        val ice_1 = RandomTexData(TexData(192f, 273f, 8f, 8f),
                                  TexData(202f, 273f, 8f, 8f),
                                  TexData(212f, 273f, 8f, 8f),
                                  TexData(222f, 273f, 8f, 8f))
        val lava_1 = FlowingTexData(TexData(396, 222, 16, 16), 8.0, 8.0)
        val lava_2 = FlowingTexData(TexData(146f, 192f, 16f, 16f), 8.0, 8.0)
        val snow_1 = RandomTexData(TexData(192, 263, 8, 8),
                                   TexData(202, 263, 8, 8),
                                   TexData(212, 263, 8, 8),
                                   TexData(222, 263, 8, 8))
        val cave_floor_1 = TexData(202, 253, 8, 8)
        val cave_floor_2 = TexData(212, 253, 8, 8)
        val cobble_floor_1 = TexData(192, 163, 8, 8)
        val tropical_grass_1 = TexData(192, 193, 8, 8)
        val vit_rock = TexData(192, 233, 8, 8)
        val wis_rock = TexData(232, 233, 8, 8)
        val atk_rock = TexData(222, 233, 8, 8)
        val life_rock = TexData(262, 233, 8, 8)
        val mana_rock = TexData(212, 233, 8, 8)
        val spd_rock = TexData(242, 233, 8, 8)
        val def_rock = TexData(252, 233, 8, 8)
        val dex_rock = TexData(202, 233, 8, 8)
        val wood_1 = TexData(222, 153, 8, 8)
        val cave_floor_3 = TexData(222, 253, 8, 8)
        val metal_floor_1 = TexData(102, 163, 8, 8)
        val cracked_metal_floor_1 = RandomTexData(TexData(202, 163, 8, 8),
                                                  TexData(212, 163, 8, 8),
                                                  TexData(222, 163, 8, 8))
        val metal_floor_2 = TexData(242, 163, 8, 8)
        val dirt_floor = TexData(202, 213, 8, 8)
        val sick = FlowingTexData(TexData(396, 240, 16, 16), 8.0, 8.0)
        val edge_blue_crystal_tile_r = TexData(242, 173, 8, 8).also { it.randomRotation = false; it.blend = false }
        val edge_blue_crystal_tile_d = TexData(212, 173, 8, 8).also { it.randomRotation = false; it.blend = false }
        val edge_blue_crystal_tile_l = TexData(202, 173, 8, 8).also { it.randomRotation = false; it.blend = false }
        val edge_blue_crystal_tile_u = TexData(232, 173, 8, 8).also { it.randomRotation = false; it.blend = false }
        val blue_crystal_tile = TexData(222, 173, 8, 8).also { it.randomRotation = false; it.blend = false }
        val light_blue_crystal_tile = TexData(192, 173, 8, 8).also { it.randomRotation = false; it.blend = false }
        val edge_purple_crystal_tile_r = TexData(212, 183, 8, 8).also { it.randomRotation = false; it.blend = false }
        val edge_purple_crystal_tile_u = TexData(202, 183, 8, 8).also { it.randomRotation = false; it.blend = false }
        val edge_purple_crystal_tile_l = TexData(222, 183, 8, 8).also { it.randomRotation = false; it.blend = false }
        val edge_purple_crystal_tile_d = TexData(232, 183, 8, 8).also { it.randomRotation = false; it.blend = false }
        val purple_crystal_tile = TexData(192, 183, 8, 8).also { it.randomRotation = false; it.blend = false }
        val light_purple_crystal_tile = TexData(242, 183, 8, 8).also { it.randomRotation = false; it.blend = false }
        val wood_2_v = TexData(192, 153, 8, 8).also { it.randomRotation = false; it.blend = false }
        val wood_2_h = TexData(212, 153, 8, 8).also { it.randomRotation = false; it.blend = false }
        val wood_3 = TexData(202, 153, 8, 8).also { it.randomRotation = false; it.blend = false }
        val elemental_org = TexData(202, 143, 8, 8).also { it.randomRotation = false; it.blend = false }
        val elemental_blu = TexData(212, 143, 8, 8).also { it.randomRotation = false; it.blend = false }
        val stone_1 = TexData(192, 143, 8, 8).also { it.randomRotation = false; it.blend = false }
        val mtns_1 = RandomTexData(TexData(192, 283, 8, 8), TexData(202, 283, 8, 8))
        val water_2 = FlowingTexData(TexData(378, 258, 16, 16), 8.0, 8.0)
        val water_3 = FlowingTexData(TexData(378, 240, 16, 16), 8.0, 8.0)
        val empty = TexData(0f, 0f, 0f, 0f)
        val magma_1 = TexData(192, 223, 8, 8)
        val shadow_stone_1 = TexData(202, 243, 8, 8)
        val shadow_stone_2 = TexData(192, 243, 8, 8)
        val sand_stone = TexData(202, 203, 8, 8)
        val greener_grass = ChanceTexData(WeightedCollection.build {
            add(0.7, TexData(222, 193, 8, 8))
            add(0.1, TexData(232, 193, 8, 8))
            add(0.1, TexData(242, 193, 8, 8))
            add(0.1, TexData(252, 193, 8, 8))
        })

        // BUSHERY //
        init {
            stageInit = Type.bushery
        }
        val gore_bush_1 = TexData(23, 823, 6, 6)
        val gore_bush_2 = TexData(30, 823, 5, 6)
        val desert_bush_1 = TexData(10, 822, 5, 7)
        val desert_bush_2 = TexData(16, 822, 6, 7)
        val tree_1 = TexData(1, 821, 8, 8)
        val mushroom_1 = TexData(36, 823, 8, 6)
        val mushroom_2 = TexData(45, 824, 6, 5)
        val crystal_1 = TexData(52, 816, 15, 13)
        val bone_torch_1 = TexData(68f, 814f, 6f, 15f, -2.5f)
        val bone_torch_2 = TexData(75f, 814f, 5f, 15f, -1f)
        val metal_fence_1 = TexData(81, 819, 8, 10)
        val large_bush_1 = TexData(90, 814, 8, 15)
        val metal_torch_1 = TexData(99f, 813f, 2f, 16f, 0.5f)
        val metal_torch_2 = TexData(102f, 813f, 3f, 16f, 1f)
        val fir_tree_1 = TexData(106, 821, 8, 8)
        val fir_tree_2 = TexData(115, 821, 8, 8)
        val fir_tree_3 = TexData(142, 821, 8, 8)
        val palm_tree_1 = TexData(124, 821, 8, 8)
        val small_bush_1 = TexData(133, 814, 8, 7)
        val mtn_bush_1 = TexData(133, 822, 8, 7)
        val shadow_bush_1 = TexData(148, 816, 14, 13)
        val shadow_bush_2 = TexData(163, 815, 12, 14)
        val fire_bush_1 = TexData(176, 816, 11, 13)
        val fire_bush_2 = TexData(188, 814, 5, 15)

        // ENTITIES //
        init {
            stageInit = Type.entity
        }
        // projectiles //
        init {
            stageInit = Type.projectile
        }
        val basic_proj_1 = TexData(0, 408, 1, 6)
        val salmon_missile_1 = TexData(2, 409, 5, 5)
        val adv_proj_1 = TexData(8, 407, 5, 7)
        val colo_proj = TexData(14, 406, 8, 8)
        val rotting_arm_proj = TexData(30, 408, 3, 6)
        val rotting_dude2_proj = TexData(23, 408, 6, 6)
        val rotting_boss_proj = TexData(50, 406, 8, 8)
        val white_demon_proj = TexData(34, 406, 6, 8)
        val basic_arrow = TexData(41, 406, 8, 8)
        val void_arrow = TexData(59, 406, 8, 8)
        val moon_beam = TexData(68, 406, 8, 8)
        val doom_arrow = TexData(77, 406, 8, 8)
        val ice_arrow = TexData(86, 406, 8, 8)
        val cutter_proj = TexData(95, 406, 8, 8)
        val fb_1_proj = TexData(112, 412, 6, 2)
        val fb_2_proj = TexData(104, 408, 7, 6)
        val fb_3_proj = TexData(119, 409, 6, 5)
        val csword_proj = TexData(126, 408, 6, 6)
        val molten_katana_proj = TexData(133, 406, 8, 8)
        val void_dude_proj_1 = TexData(0, 415, 6, 8)
        val frozen_katana_proj = TexData(7, 419, 4, 4)
        val chain_scythe_chain = TexData(12, 415, 8, 8)
        val chain_scythe_proj = TexData(21, 415, 8, 8)
        val dual_crystal_cutters_proj = TexData(30, 415, 8, 8)
        val crystal_proj_2 = TexData(39, 416, 7, 7)
        val elemental_dude_proj_2 = TexData(47, 415, 8, 8)
        val elemental_dude_proj_1 = TexData(56, 416, 5, 7)
        val hermit_crab_proj = TexData(62, 415, 2, 8)
        val warbringers_lance_proj = TexData(65, 415, 8, 8)
        val warbringers_bow_proj = TexData(74, 415, 8, 8)
        val warbringers_dagger_proj = TexData(83, 418, 5, 5)
        val warbringer_proj = TexData(89, 417, 6, 6)
        val agateclaw_dagger_proj = TexData(96, 420, 6, 3)
        val medusa_proj = TexData(103, 415, 8, 8)
        val dasher_1_proj = TexData(112, 417, 4, 6)
        val dasher_2_proj = TexData(117, 417, 4, 6)
        val fire_breather_proj_1 = TexData(122, 416, 7, 7)
        val fire_breather_proj_2 = TexData(130, 415, 8, 8)
        val shadow_scale_proj_1 = TexData(139, 415, 8, 8)
        val shadow_scale_proj_2 = TexData(148, 415, 8, 8)
        val stem_of_the_brain_proj = TexData(157, 417, 6, 6)
        val flying_brain_proj = TexData(164, 415, 8, 8)
        val lightning_bolt_proj = TexData(142, 406, 8, 8)
        val diviners_old_stick_proj = TexData(0, 424, 4, 4)
        val golden_wand_proj = TexData(4, 424, 6, 5)
        val flowering_katana_proj = TexData(151, 407, 4, 7)
        val bloody_blade_proj = TexData(156, 408, 5, 6)
        val demons_horn_proj = TexData(162, 407, 5, 7)
        val demons_fang_proj = TexData(168, 407, 5, 7)

        // enemies //
        init {
            stageInit = Type.entity
        }
        val cube = TexData(540, 1, 10, 10)
        val minion = TexData(554, 18, 8, 8)
        val flame = TexData(527, 1, 12, 15)
        val fb_1 = TexData(527, 17, 8, 8)
        val fb_2 = TexData(536, 17, 8, 8)
        val fb_3 = TexData(545, 18, 8, 8)
        val swords_man = TexData(568, 1, 8, 8)
        val flayer_entity = TexData(563, 18, 16, 16)
        val rotting_dude1 = TexData(527, 26, 16, 15)
        val rotting_dude2 = TexData(536, 60, 10, 15)
        val rotting_boss = TexData(547, 61, 12, 16)
        val white_demon = TexData(527, 42, 15, 15)
        val white_bag = TexData(553, 52, 7, 8)
        val yellow_bag = TexData(610, 51, 8, 8)
        val pink_bag = TexData(561, 50, 6, 8)
        val ice_portal = TexData(543, 52, 8, 7)
        val flowering_portal = TexData(527, 67, 8, 8)
        val realm_portal = TexData(552, 44, 8, 7)
        val crystal_portal = TexData(543, 44, 8, 7)
        val potion_dude = TexData(544, 27, 16, 16)
        val void_dude = TexData(561, 35, 18, 14)
        val rotten_portal = TexData(527, 58, 8, 8)
        val metallic_robot = TexData(551, 1, 16, 16)
        val brown_slime = TexData(568, 50, 8, 6)
        val crystal_dude = TexData(579, 1, 13, 15)
        val hermit_crab = TexData(561, 59, 8, 8)
        val orange_elemental_dude = TexData(580, 17, 13, 16)
        val blue_elemental_dude = TexData(594, 17, 13, 16)
        val medusa = TexData(595, 34, 16, 16)
        val warbringer = TexData(577, 52, 32, 31)
        val shadow_scale = TexData(560, 68, 12, 14)
        val fire_breather = TexData(569, 81, 14, 14)
        val flying_brain = TexData(612, 34, 15, 16)

        // OTHER //
        init {
            stageInit = Type.other
        }
        val legendary = TexData(2f, 974f, 236f, 46f)
        val berserk_effect = TexData(384f, 0f, 7f, 7f)
        val damaging_effect = TexData(401f, 0f, 7f, 7f)
        val shield_effect = TexData(392, 0, 8, 8)
        val sick_effect = TexData(409, 0, 7, 7)
        val curse_effect = TexData(417, 0, 7, 7)
        val rect = TexData(373f, 0f, 1f, 1f)
        val primal = TexData(2f, 908f, 150f, 42f)
        val blend_8x8 = RandomTexData(TexData(191, 292, 8, 8), TexData(199, 292, 8, 8), TexData(207, 292, 8, 8))
        val blend_8x8c = RandomTexData(TexData(215, 292, 8, 8))
        val atk_add_effect = TexData(425, 0, 15, 9)
        val bleed_effect = TexData(441, 0, 5, 7)
        val vignette = TexData(944, 974, 80, 50)

        operator fun get(name:string?):TexData {
            return values[name] ?: empty
        }

        fun init() {
            for (field in this::class.declaredMemberProperties) {
                if (field.returnType.jvmErasure == TexData::class ||
                    field.returnType.jvmErasure == AnimatedTexData::class ||
                    field.returnType.jvmErasure == FlowingTexData::class ||
                    field.returnType.jvmErasure == RandomTexData::class) {
                    val data = (field as KProperty1<Companion, *>).get(this) as TexData
                    values[field.name] = data
                    data.id = field.name
                    ifclient {
                        if (data.type == Type.tile || data.type == Type.item || data.type == Type.player || data.type == Type.other || data.type == Type.projectile) {
                            data.pivotX = 0.0
                            data.pivotY = 0.0
                        }
                        if (data.type == Type.tile) {
                            data.getAvgColor()
                        }
                        if (data == empty) {
                            data.avgColor = 0L
                        }
                    }
                }
                if (field.returnType.jvmErasure == PlayerTexData::class) {
                    val data = (field as KProperty1<Companion, *>).get(this) as PlayerTexData
                    data.id = field.name
                }
            }
        }
    }

}