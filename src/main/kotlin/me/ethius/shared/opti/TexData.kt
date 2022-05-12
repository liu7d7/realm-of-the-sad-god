package me.ethius.shared.opti

import me.ethius.client.main_tex
import me.ethius.shared.*
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
        val crystal_ninja = PlayerTexData(143f, 1f)
        val frimar_warrior = PlayerTexData(143f, 26f)
        val dasher = PlayerTexData(214f, 1f)
        val key_dasher = PlayerTexData(214f, 26f)
        val traffic_cone_dasher = PlayerTexData(72, 51, 13)
        val bone_warrior = PlayerTexData(214, 51, 12)
        val demon_dasher = PlayerTexData(214, 75, 11)
        val kunoichi = PlayerTexData(213, 100, 11)

        // ITEMS //
        init {
            stageInit = Type.item
        }
        // katanas //
        val basic_katana_1 = TexData(524f, 431f, 34f, 34f)
        val basic_katana_2 = TexData(183f, 431f, 34f, 34f)
        val moonbeam_katana = TexData(115f, 397f, 34f, 34f)
        val stardust_cutter = TexData(183f, 397f, 34f, 34f)
        val molten_katana = TexData(218f, 364f, 32f, 32f)
        val kalon = TexData(286, 364, 32, 32)
        val frozen_katana = TexData(352, 364, 24, 32)
        val dead_man_scythe = TexData(150, 330, 32, 32)
        val dual_crystal_cutters = TexData(184, 330, 32, 32)
        val elemental_slasher = TexData(252, 330, 32, 32)
        val stem_of_the_brain = TexData(320, 296, 32, 32)

        // daggers //
        val basic_dagger_1 = TexData(388, 330, 32, 32)
        val fire_breather_tail = TexData(422, 330, 32, 32)
        val warbringers_dagger = TexData(490, 296, 32, 32)
        val agateclaw_dagger = TexData(388, 296, 32, 32)

        // abilities //
        val adv_shield_1 = TexData(438f, 431f, 22f, 34f)
        val basic_shield_1 = TexData(416f, 431f, 22f, 34f)
        val ninja_star_1 = TexData(319f, 397f, 34f, 34f)
        val ninja_star_2 = TexData(116f, 364f, 32f, 32f)
        val quiver_1 = TexData(353f, 397f, 34f, 34f)
        val quiver_2 = TexData(150f, 364f, 32f, 32f)

        // rings //
        val bone_ring = TexData(382f, 431f, 34f, 34f)
        val basic_ring_1 = TexData(495f, 431f, 30f, 34f)
        val extreme_ring_of_atk = TexData(252, 296, 32, 32)

        // armors //
        val heavy_armor_1 = TexData(460f, 431f, 34f, 34f)
        val heavy_armor_2 = TexData(285f, 397f, 34f, 34f)
        val leather_armor_1 = TexData(217f, 397f, 34f, 34f)
        val leather_armor_2 = TexData(251f, 397f, 34f, 34f)
        val bone_armor = TexData(116, 330, 32, 32)
        val elemental_heavy_armor = TexData(320, 330, 32, 32)
        val elemental_light_armor = TexData(354, 330, 32, 32)
        val the_scorched_armor = TexData(286, 296, 32, 32)

        // swords //
        val colo_sword_1 = TexData(277f, 431f, 34f, 34f)
        val rotting_arm = TexData(311f, 431f, 34f, 34f)
        val basic_sword = TexData(115f, 431f, 34f, 34f)
        val adv_dagger_1 = TexData(251f, 434f, 26f, 30f)
        val crystal_sword = TexData(184f, 364f, 32f, 32f)
        val murena = TexData(320, 364, 32, 32)
        val elemental_saber = TexData(286, 330, 32, 32)
        val warbringers_lance = TexData(422, 296, 32, 32)
        val shadow_crusher = TexData(354, 296, 32, 32)

        // bows //
        val basic_bow = TexData(149f, 431f, 34f, 34f)
        val void_bow = TexData(217f, 431f, 34f, 34f)
        val doom_bow = TexData(149f, 397f, 34f, 34f)
        val ice_bow = TexData(348f, 431f, 34f, 34f)
        val crystal_bow = TexData(218, 330, 32, 32)
        val warbringers_bow = TexData(456, 296, 32, 32)

        // potions //
        val life_potion = TexData(413f, 401f, 26f, 30f)
        val mana_potion = TexData(387f, 401f, 26f, 30f)
        val spd_potion = TexData(517f, 401f, 26f, 30f)
        val vit_potion = TexData(465f, 401f, 26f, 30f)
        val wis_potion = TexData(439f, 401f, 26f, 30f)
        val dex_potion = TexData(491f, 401f, 26f, 30f)
        val def_potion = TexData(387f, 371f, 26f, 30f)
        val atk_potion = TexData(413f, 371f, 26f, 30f)
        val apple = TexData(252f, 364f, 32f, 32f)

        // dashers //
        val basic_dasher = TexData(490, 330, 32, 32)
        val adv_dasher = TexData(456, 330, 32, 32)

        // TILES //
        init {
            stageInit = Type.tile
        }
        val grass_1 = TexData(216f, 256f, 8f, 8f)
        val sand_1 = TexData(146, 256, 8, 8)
        val water_1 = FlowingTexData(TexData(254f, 248f, 16f, 16f), 8.0, 8.0)
        val border_water_1 = FlowingTexData(TexData(272f, 248f, 16f, 16f), 8.0, 8.0)
        val grass_2 = TexData(290f, 256f, 8f, 8f)
        val gore_1 = TexData(310f, 246f, 8f, 8f)
        val ice_1 = RandomTexData(TexData(166f, 256f, 8f, 8f),
                                  TexData(176f, 256f, 8f, 8f),
                                  TexData(186f, 256f, 8f, 8f),
                                  TexData(196f, 256f, 8f, 8f))
        val lava_1 = FlowingTexData(TexData(236f, 248f, 16f, 16f), 8.0, 8.0)
        val lava_2 = FlowingTexData(TexData(146f, 192f, 16f, 16f), 8.0, 8.0)
        val snow_1 = RandomTexData(TexData(146f, 246f, 8f, 8f),
                                   TexData(156, 246, 8, 8),
                                   TexData(166, 246, 8, 8),
                                   TexData(176, 246, 8, 8),
                                   TexData(186, 246, 8, 8))
        val cave_floor_1 = TexData(290, 246, 8, 8)
        val cave_floor_2 = TexData(300, 256, 8, 8)
        val cobble_floor_1 = TexData(186, 246, 8, 8)
        val tropical_grass_1 = TexData(206, 256, 8, 8)
        val vit_rock = TexData(196, 266, 8, 8)
        val wis_rock = TexData(176, 266, 8, 8)
        val atk_rock = TexData(166, 266, 8, 8)
        val life_rock = TexData(216, 266, 8, 8)
        val mana_rock = TexData(156, 266, 8, 8)
        val spd_rock = TexData(186, 266, 8, 8)
        val def_rock = TexData(206, 266, 8, 8)
        val dex_rock = TexData(146, 266, 8, 8)
        val wood_1 = TexData(156, 256, 8, 8)
        val cave_floor_3 = TexData(300, 246, 8, 8)
        val metal_floor_1 = TexData(226, 266, 8, 8)
        val cracked_metal_floor_1 = RandomTexData(TexData(236, 266, 8, 8),
                                                  TexData(246, 266, 8, 8),
                                                  TexData(256, 266, 8, 8))
        val metal_floor_2 = TexData(266, 266, 8, 8)
        val dirt_floor = TexData(276, 266, 8, 8)
        val sick = FlowingTexData(TexData(217, 237, 18, 18), 8.0, 8.0)
        val edge_blue_crystal_tile_r = TexData(176, 236, 8, 8).also { it.randomRotation = false; it.blend = false }
        val edge_blue_crystal_tile_d = TexData(186, 236, 8, 8).also { it.randomRotation = false; it.blend = false }
        val edge_blue_crystal_tile_l = TexData(196, 246, 8, 8).also { it.randomRotation = false; it.blend = false }
        val edge_blue_crystal_tile_u = TexData(186, 226, 8, 8).also { it.randomRotation = false; it.blend = false }
        val blue_crystal_tile = TexData(196, 236, 8, 8).also { it.randomRotation = false; it.blend = false }
        val light_blue_crystal_tile = TexData(186, 216, 8, 8).also { it.randomRotation = false; it.blend = false }
        val edge_purple_crystal_tile_r = TexData(216, 226, 8, 8).also { it.randomRotation = false; it.blend = false }
        val edge_purple_crystal_tile_u = TexData(206, 226, 8, 8).also { it.randomRotation = false; it.blend = false }
        val edge_purple_crystal_tile_l = TexData(206, 236, 8, 8).also { it.randomRotation = false; it.blend = false }
        val edge_purple_crystal_tile_d = TexData(206, 246, 8, 8).also { it.randomRotation = false; it.blend = false }
        val purple_crystal_tile = TexData(196, 226, 8, 8).also { it.randomRotation = false; it.blend = false }
        val light_purple_crystal_tile = TexData(196, 216, 8, 8).also { it.randomRotation = false; it.blend = false }
        val mossy_stone = TexData(226, 226, 8, 8)
        val wood_2_v = TexData(236, 226, 8, 8).also { it.randomRotation = false; it.blend = false }
        val wood_2_h = TexData(236, 216, 8, 8).also { it.randomRotation = false; it.blend = false }
        val wood_3 = TexData(246, 226, 8, 8).also { it.randomRotation = false; it.blend = false }
        val elemental_org = TexData(256, 226, 8, 8).also { it.randomRotation = false; it.blend = false }
        val elemental_blu = TexData(266, 226, 8, 8).also { it.randomRotation = false; it.blend = false }
        val stone_1 = TexData(226, 216, 8, 8).also { it.randomRotation = false; it.blend = false }
        val mtns_1 = RandomTexData(TexData(206, 216, 8, 8), TexData(216, 216, 8, 8))
        val water_2 = FlowingTexData(TexData(146, 228, 16, 16), 8.0, 8.0)
        val water_3 = FlowingTexData(TexData(146, 210, 16, 16), 8.0, 8.0)
        val empty = TexData(0f, 0f, 0f, 0f)
        val magma_1 = TexData(246, 216, 8, 8)
        val shadow_stone_1 = TexData(176, 216, 8, 8)
        val shadow_stone_2 = TexData(176, 226, 8, 8)
        val sand_stone = TexData(236, 238, 8, 8)

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
        val basic_proj_1 = TexData(125f, 465f, 7f, 32f)
        val salmon_missile_1 = TexData(132f, 465f, 15f, 37f)
        val adv_proj_1 = TexData(147f, 465f, 27f, 37f)
        val colo_proj = TexData(174f, 465f, 16f, 57f)
        val rotting_arm_proj = TexData(190f, 465f, 17f, 32f)
        val rotting_dude2_proj = TexData(126, 502, 30, 30)
        val rotting_boss_proj = TexData(126, 533, 40, 40)
        val white_demon_proj = TexData(207f, 465f, 32f, 42f)
        val basic_arrow = TexData(262f, 465f, 15f, 54f)
        val void_arrow = TexData(277f, 465f, 16f, 54f)
        val moon_beam = TexData(293f, 465f, 16f, 57f)
        val doom_arrow = TexData(309f, 465f, 15f, 54f)
        val ice_arrow = TexData(324f, 465f, 15f, 54f)
        val cutter_proj = TexData(418f, 466f, 13f, 56f)
        val fb_1_proj = TexData(348f, 465f, 32f, 12f)
        val fb_2_proj = TexData(348f, 477f, 37f, 32f)
        val fb_3_proj = TexData(385f, 465f, 32f, 27f)
        val csword_proj = TexData(433f, 466f, 13f, 42f)
        val molten_katana_proj = TexData(448f, 466f, 14f, 55f)
        val void_dude_proj_1 = TexData(464f, 466f, 30f, 40f)
        val frozen_katana_proj = TexData(496f, 466f, 21f, 24f)
        val chain_scythe_chain = TexData(167, 533, 40, 40)
        val chain_scythe_proj = TexData(208, 533, 40, 40)
        val dual_crystal_cutters_proj = TexData(250, 533, 40, 40)
        val crystal_proj_2 = TexData(292, 533, 40, 40)
        val elemental_dude_proj_2 = TexData(334, 533, 40, 40)
        val elemental_dude_proj_1 = TexData(376, 533, 40, 40)
        val hermit_crab_proj = TexData(418, 533, 10, 40)
        val warbringers_lance_proj = TexData(430, 533, 8, 8)
        val warbringers_bow_proj = TexData(430, 542, 8, 8)
        val warbringers_dagger_proj = TexData(430, 551, 5, 5)
        val warbringer_proj = TexData(430, 557, 6, 6)
        val agateclaw_dagger_proj = TexData(430, 564, 6, 3)
        val medusa_proj = TexData(439, 533, 8, 8)
        val dasher_1_proj = TexData(439, 542, 4, 6)
        val dasher_2_proj = TexData(444, 542, 4, 6)
        val fire_breather_proj_1 = TexData(439, 549, 7, 7)
        val fire_breather_proj_2 = TexData(449, 542, 8, 8)
        val shadow_scale_proj_1 = TexData(439, 557, 8, 8)
        val shadow_scale_proj_2 = TexData(449, 533, 8, 8)
        val stem_of_the_brain_proj = TexData(449, 551, 6, 6)
        val flying_brain_proj = TexData(449, 558, 8, 8)

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
        val blend_8x8 = RandomTexData(TexData(145, 275, 8, 8), TexData(153, 275, 8, 8), TexData(161, 275, 8, 8))
        val blend_8x8c = RandomTexData(TexData(169, 275, 8, 8))
        val atk_add_effect = TexData(425, 0, 15, 9)
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
                    if (Side._client) {
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