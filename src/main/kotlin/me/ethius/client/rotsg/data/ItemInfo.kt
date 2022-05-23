package me.ethius.client.rotsg.data

import me.ethius.client.Client
import me.ethius.client.calcAngleMPToMouse
import me.ethius.client.mouseDstToPlayer
import me.ethius.client.rotsg.item.*
import me.ethius.client.rotsg.item.bow.DoomBow
import me.ethius.client.rotsg.item.bow.IceBow
import me.ethius.client.rotsg.item.bow.VoidBow
import me.ethius.client.rotsg.item.katana.DeadmansScythe
import me.ethius.client.rotsg.item.katana.MoltenKatana
import me.ethius.client.rotsg.item.katana.MoonbeamKatana
import me.ethius.client.rotsg.item.katana.StardustCutter
import me.ethius.client.rotsg.item.potion.AppleOfExtremeMaxeningItem
import me.ethius.client.rotsg.item.potion.PotionItem
import me.ethius.client.rotsg.item.sword.ColoSword
import me.ethius.client.rotsg.item.sword.Murena
import me.ethius.client.rotsg.item.sword.RottingArm
import me.ethius.shared.cosD
import me.ethius.shared.dvec2
import me.ethius.shared.opti.TexData
import me.ethius.shared.rotsg.data.EffectInfo
import me.ethius.shared.rotsg.data.ProjectileData
import me.ethius.shared.rotsg.entity.Stat
import me.ethius.shared.rotsg.entity.enemy.Aoe
import me.ethius.shared.rotsg.entity.other.Projectile
import me.ethius.shared.rotsg.tile.tile_size
import me.ethius.shared.sinD
import me.ethius.shared.string
import java.util.*
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.jvmErasure

class ItemInfo<T:Item>(val supplier:() -> T) {

    lateinit var id:string

    operator fun invoke():T {
        return supplier().also { it.id = this.id }
    }

    @Suppress("UNUSED")
    companion object {
        val values = ArrayList<ItemInfo<*>>()

        // MISC //
        val air = ItemInfo { AirItem() }

        // WEAPONS //
        val basic_katana_1 = ItemInfo {
            KatanaItem(TexData.basic_katana_1,
                       ItemTier.normal,
                       listOf(ProjectileData.basic_proj),
                       EnumMap(Stat::class.java),
                       "Katana",
                       "The most basic katana.")
        }
        val basic_katana_2 = ItemInfo {
            KatanaItem(TexData.basic_katana_2,
                       ItemTier.normal,
                       listOf(ProjectileData.basic_proj_x2(35..65, 4.0, 14.0),
                              ProjectileData.basic_proj_x2(35..65, 4.0, 14.0)),
                       hashMapOf(Pair(Stat.def, 1)),
                       "Metal Katana",
                       "The second most basic katana. It's made of metal.").also { it.arcGap = 10.0 }
        }
        val basic_bow_1 = ItemInfo {
            BowItem(TexData.basic_bow,
                    ItemTier.normal,
                    listOf(ProjectileData.basic_arrow),
                    EnumMap(Stat::class.java),
                    "Bow",
                    "The most basic bow.")
        }
        val basic_sword_1 = ItemInfo {
            SwordItem(TexData.basic_sword,
                      ItemTier.normal,
                      listOf(ProjectileData.basic_proj_x2(110..125, 3.25, 8.0)),
                      EnumMap(Stat::class.java),
                      "Sword",
                      "The most basic sword.")
        }
        val adv_dirk = ItemInfo {
            DaggerItem(TexData.adv_dagger_1, ItemTier.heroic, listOf(
                ProjectileData.adv_proj(-5.0),
                ProjectileData.adv_proj(5.0)
            ), hashMapOf(Pair(Stat.dex, 4)), "Advanced Dirk", "The most advanced dirk.").also {
                it.arcGap = 20.0
            }
        }
        val agateclaw_dagger = ItemInfo {
            DaggerItem(TexData.agateclaw_dagger,
                       ItemTier.heroic,
                       listOf(ProjectileData(TexData.agateclaw_dagger_proj,
                                             0.0,
                                             0.0,
                                             14.0,
                                             5.6,
                                             false,
                                             false,
                                             false,
                                             false,
                                             50..110,
                                             0.82).also { it.renderAngleAdd = -90.0 }),
                       hashMapOf(Pair(Stat.dex, 4)),
                       "Agateclaw Dagger",
                       "The most advanced dirk.")
        }
        val doom_bow = ItemInfo { DoomBow() }
        val moonbeam_katana = ItemInfo { MoonbeamKatana() }
        val stardust_cutter = ItemInfo { StardustCutter() }
        val colo_sword = ItemInfo { ColoSword() }
        val rotting_arm = ItemInfo { RottingArm() }
        val ice_bow = ItemInfo { IceBow() }
        val void_bow = ItemInfo { VoidBow() }
        val molten_katana = ItemInfo { MoltenKatana() }
        val murena = ItemInfo { Murena() }
        val kalon = ItemInfo { KatanaItem("Kalon") }
        val frozen_katana = ItemInfo { KatanaItem("FrozenKatana") }
        val deadmans_scythe = ItemInfo { DeadmansScythe() }
        val dual_crystal_cutters = ItemInfo { KatanaItem("DualCrystalCutters") }
        val elemental_slasher = ItemInfo { KatanaItem("ElementalSlasher") }
        val shadow_crusher = ItemInfo { SwordItem("ShadowCrusher") }
        val basic_dagger_1 = ItemInfo { DaggerItem("BasicDagger_1") }
        val fire_breather_tail = ItemInfo { DaggerItem("FireBreatherTail") }
        val warbringers_dagger = ItemInfo { DaggerItem("WarbringersDagger") }
        val warbringers_bow = ItemInfo { BowItem("WarbringersBow") }
        val stem_of_the_brain = ItemInfo { KatanaItem("StemOfTheBrain") }
        val lightning_bolt = ItemInfo { KatanaItem("LightningBolt") }
        val diviners_old_stick = ItemInfo { WandItem("DivinersOldStick") }
        val golden_wand = ItemInfo { WandItem("GoldenWand") }

        // ABILITIES //
        val basic_shield_1 = ItemInfo {
            ShieldItem(TexData.basic_shield_1,
                       ItemTier.normal,
                       { Client.player.addEffect(EffectInfo.shield(750L)) },
                       70,
                       EnumMap(Stat::class.java),
                       "Basic Shield",
                       "The most basic shield.")
        }
        val shield_2 = ItemInfo {
            ShieldItem(TexData.adv_shield_1,
                       ItemTier.normal,
                       { Client.player.addEffect(EffectInfo.shield(1000L)) },
                       90,
                       EnumMap(Stat::class.java),
                       "Advanced Shield",
                       "An advanced shield.")
        }
        val basic_ninja_star_1 = ItemInfo {
            NinjaStarItem(TexData.ninja_star_1,
                          ItemTier.normal,
                          {
                              Client.world.addEntity(Projectile().reset(Client.player, ProjectileData.basic_ninja_star_proj))
                          },
                          30,
                          EnumMap(Stat::class.java),
                          "Basic Ninja Star",
                          "The most basic ninja star."
            )
        }
        val ninja_star_2 = ItemInfo {
            NinjaStarItem(TexData.ninja_star_2,
                          ItemTier.normal,
                          {
                              Client.world.addEntity(Projectile().reset(Client.player, ProjectileData.basic_ninja_star_2_proj))
                          },
                          50,
                          EnumMap(Stat::class.java),
                          "Advanced Ninja Star",
                          "An advanced ninja star."
            )
        }
        val basic_quiver_1 = ItemInfo {
            QuiverItem(TexData.quiver_1,
                       ItemTier.normal,
                       {
                           Client.world.addEntity(Projectile().reset(Client.player, ProjectileData.basic_quiver_proj))
                       },
                       30,
                       EnumMap(Stat::class.java),
                       "Basic Quiver",
                       "The most basic quiver.")
        }
        val quiver_2 = ItemInfo {
            QuiverItem(TexData.quiver_2,
                       ItemTier.normal,
                       {
                           Client.world.addEntity(Projectile().reset(Client.player, ProjectileData.basic_quiver_2_proj))
                       },
                       50,
                       EnumMap(Stat::class.java),
                       "Advanced Quiver",
                       "An advanced quiver.")
        }
        val basic_dasher_1 = ItemInfo {
            DasherItem(TexData.basic_dasher,
                       ItemTier.normal,
                       65,
                       EnumMap(Stat::class.java),
                       "Basic Dasher",
                       "This dasher allows the player to dash forwards quickly.",
                       3, ProjectileData.dasher_1_proj)
        }
        val adv_dasher = ItemInfo {
            DasherItem(TexData.adv_dasher,
                       ItemTier.heroic,
                       65,
                       hashMapOf(Pair(Stat.dex, 4)),
                       "Advanced Dasher",
                       "This dasher allows the player to dash forwards quickly.",
                       6, ProjectileData.dasher_2_proj)
        }
        val basic_orb_1 = ItemInfo {
            OrbItem(TexData.orb_1,
                    ItemTier.normal,
                    {
                        val angle = calcAngleMPToMouse()
                        val direction = dvec2(cosD(angle), sinD(angle))
                        val dist = mouseDstToPlayer().coerceAtMost(6.0 * tile_size)
                        direction.mul(dist)
                        val a = Aoe(direction.x + Client.player.x, direction.y + Client.player.y).also {
                            it.owner = Client.player
                            it.damage = 75.0
                            it.radius = 2.5
                            it.lifetime = 12.0
                            it.modulateZ = false
                            it.texDataId = TexData.light_blue_crystal_tile.id
                            it.speed = direction.length() / it.lifetime / 1000.0
                            it.direction = angle
                        }
                        it.addEntity(a)
                    },
                    40,
                    EnumMap(Stat::class.java),
                    "Basic Orb",
                    "This orb allows the player to dash forwards quickly.")
        }

        // ARMORS //
        val heavy_armor_1 = ItemInfo {
            HeavyArmorItem(TexData.heavy_armor_1, ItemTier.normal,
                           hashMapOf(Pair(Stat.def, 10)),
                           "Steel Armor",
                           "The most basic heavy armor.")
        }
        val light_armor_1 = ItemInfo {
            LightArmorItem(TexData.leather_armor_1, ItemTier.normal,
                           hashMapOf(Pair(Stat.def, 5), Pair(Stat.dex, 2)),
                           "Leather Armor",
                           "The most basic leather armor.")
        }
        val heavy_armor_2 = ItemInfo {
            HeavyArmorItem(TexData.heavy_armor_2, ItemTier.heroic,
                           hashMapOf(Pair(Stat.def, 20)),
                           "Alloy Armor",
                           "The second most basic heavy armor.")
        }
        val light_armor_2 = ItemInfo {
            LightArmorItem(TexData.leather_armor_2, ItemTier.heroic,
                           hashMapOf(Pair(Stat.def, 10), Pair(Stat.dex, 5)),
                           "Flayer's Armor",
                           "The second most basic leather armor.")
        }
        val bone_armor = ItemInfo {
            LightArmorItem(TexData.bone_armor,
                           ItemTier.legendary,
                           hashMapOf(Stat.def to 14, Stat.dex to -6, Stat.atk to 12),
                           "Bone Armor",
                           "An armor crafted out of the remains of past warriors.")
        }
        val robe_1 = ItemInfo {
            RobeArmorItem(TexData.robe_1,
            ItemTier.normal,
            hashMapOf(Pair(Stat.def, 5), Pair(Stat.wis, 2)),
            "Robe",
            "A simple robe.")
        }
        val the_scorched_armor = ItemInfo { LightArmorItem("TheScorchedArmor") }

        // RINGS //
        val basic_ring = ItemInfo {
            RingItem(TexData.basic_ring_1, ItemTier.normal,
                     hashMapOf(Pair(Stat.dex, 5)),
                     "Basic Ring",
                     "The most basic ring.")
        }
        val bone_ring = ItemInfo {
            RingItem(
                TexData.bone_ring,
                ItemTier.legendary,
                hashMapOf(Pair(Stat.vit, 15),
                          Pair(Stat.atk, 9),
                          Pair(Stat.dex, 9),
                          Pair(Stat.def, -6)),
                "Bone Ring",
                "A ring that inspires its wearer with fury, but makes them forget to protect themselves."
            )
        }
        val extreme_ring_of_atk = ItemInfo { RingItem("ExtremeRingOfAtk") }

        // POTIONS //
        val dex_potion = ItemInfo { PotionItem(Stat.dex) }
        val vit_potion = ItemInfo { PotionItem(Stat.vit) }
        val atk_potion = ItemInfo { PotionItem(Stat.atk) }
        val def_potion = ItemInfo { PotionItem(Stat.def) }
        val life_potion = ItemInfo { PotionItem(Stat.life) }
        val mana_potion = ItemInfo { PotionItem(Stat.mana) }
        val spd_potion = ItemInfo { PotionItem(Stat.spd) }
        val wis_potion = ItemInfo { PotionItem(Stat.wis) }
        val random_potion
            get() = PotionItem.supplierFromStat(Stat.values().random())
        val apple = ItemInfo {
            AppleOfExtremeMaxeningItem()
        }

        operator fun get(str:string):ItemInfo<out Item> {
            return values.find { it.id == str } ?: air
        }

        fun init() {
            for (i in this::class.declaredMemberProperties) {
                if (i.returnType.jvmErasure == ItemInfo::class) {
                    values.add(((i as KProperty1<Any, *>).get(this as Any) as ItemInfo<*>).also { it.id = i.name })
                }
            }
        }
    }

}

