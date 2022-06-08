package me.ethius.shared.rotsg.entity.player

import me.ethius.client.rotsg.data.ItemInfo
import me.ethius.client.rotsg.inventory.Inventory
import me.ethius.client.rotsg.item.*
import me.ethius.shared.int
import me.ethius.shared.opti.PlayerTexData
import me.ethius.shared.opti.TexData
import me.ethius.shared.string
import me.ethius.shared.void

enum class PlayerClass(
    val displayName:string,
    val description:string,
    val weaponBaseClass:Class<out Item>,
    val abilityBaseClass:Class<out Item>,
    val armorBaseClass:Class<out Item>,
    val initInventory:(Inventory) -> void,
    val skins:Array<PlayerTexData>, val maxStats:Array<int>,
) {
    ninja("Ninja",
          "The ninja is a class that focuses on stealth and speed.",
          KatanaItem::class.java,
          NinjaStarItem::class.java,
          LightArmorItem::class.java,
          {
              it.weapon.item = ItemInfo.basic_katana_1()
              it.ability.item = ItemInfo.basic_ninja_star_1()
          },
          arrayOf(TexData.ninja, TexData.nature_ninja, TexData.new_ninja, TexData.kunoichi),
          arrayOf(720, 252, 70, 25, 60, 70, 60, 70)),
    warrior("Warrior",
            "The warrior is a class that focuses on strength and defense.",
            SwordItem::class.java,
            ShieldItem::class.java,
            HeavyArmorItem::class.java,
            {
                it.weapon.item = ItemInfo.basic_sword_1()
                it.ability.item = ItemInfo.basic_shield_1()
            },
            arrayOf(TexData.warrior, TexData.frimar_warrior, TexData.bone_warrior, TexData.crystal_warrior, TexData.knight),
            arrayOf(770, 252, 75, 25, 50, 50, 75, 50)),
    archer("Archer",
           "The archer is a class that focuses on speed and accuracy.",
           BowItem::class.java,
           QuiverItem::class.java,
           LightArmorItem::class.java,
           {
               it.weapon.item = ItemInfo.basic_bow_1()
               it.ability.item = ItemInfo.basic_quiver_1()
           },
           arrayOf(TexData.archer, TexData.nature_archer),
           arrayOf(700, 252, 75, 25, 50, 50, 40, 50)),
    dasher("Dasher", "The dasher is a class that can dash forward very fast.",
           DaggerItem::class.java,
           DasherItem::class.java,
           HeavyArmorItem::class.java,
           {
               it.weapon.item = ItemInfo.basic_dagger_1()
               it.ability.item = ItemInfo.basic_dasher_1()
           },
           arrayOf(TexData.dasher, TexData.key_dasher, TexData.traffic_cone_dasher, TexData.demon_dasher, TexData.mushroom_dasher),
           arrayOf(625, 195, 65, 30, 60, 65, 60, 50)),
    diviner(
        "Diviner",
        "The diviner is a class that can cast spells and use items.",
        WandItem::class.java,
        OrbItem::class.java,
        RobeArmorItem::class.java,
        {
            it.weapon.item = ItemInfo.diviners_old_stick()
            it.ability.item = ItemInfo.basic_orb_1()
        },
        arrayOf(TexData.diviner),
        arrayOf(720, 252, 70, 25, 60, 70, 60, 70));

    companion object {
        operator fun get(string:string):PlayerClass {
            return valueOf(string)
        }
    }
}