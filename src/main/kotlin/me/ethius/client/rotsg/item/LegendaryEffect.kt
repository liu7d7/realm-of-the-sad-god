package me.ethius.client.rotsg.item

import me.ethius.client.Client
import me.ethius.client.rotsg.entity.ClientPlayer
import me.ethius.client.rotsg.gui.EntityNotification
import me.ethius.shared.*
import me.ethius.shared.rotsg.data.EffectInfo
import me.ethius.shared.rotsg.data.Formatting
import me.ethius.shared.rotsg.data.ProjectileProperties
import me.ethius.shared.rotsg.entity.AEntity
import me.ethius.shared.rotsg.entity.Stat
import org.apache.commons.lang3.RandomUtils

private val cptv = { _:ClientPlayer -> }

enum class LegendaryEffect(
    val nm:string,
    val desc:string,
    private val delay:long,
    val chance:float,
    private val nameFormat:Formatting,
    private val descFormat:Formatting,
    private val hook:ClientPlayer.MainPlayerHook,
    private val _onActivate:(ClientPlayer) -> void,
) {
    berserk_on_shoot_25("Berserk",
                        "25% chance to berserk for 1 second on shoot, delay 2 seconds.",
                        2000L,
                        0.25f,
                        Formatting.red,
                        Formatting.white,
                        ClientPlayer.MainPlayerHook.on_shoot,
                        { player ->
                            player.addEffect(EffectInfo.berserk(1000L))
                        }),
    berserk_and_damaging_on_shoot_25("Berserk and Damaging",
                                     "25% chance to berserk and give damaging for 1 second on shoot, delay 2 seconds.",
                                     2000L,
                                     0.25f,
                                     Formatting.gold,
                                     Formatting.white,
                                     ClientPlayer.MainPlayerHook.on_shoot,
                                     { player ->
                                         player.addEffect(EffectInfo.berserk(1000L)); player.addEffect(
                                         EffectInfo.damaging(
                                             1000L))
                                     }),
    berserk_on_shoot_50_no_delay("Berserk",
                                 "25% chance to berserk for 1 second on shoot.",
                                 750L,
                                 0.50f,
                                 Formatting.red,
                                 Formatting.white,
                                 ClientPlayer.MainPlayerHook.on_shoot,
                                 { player -> player.addEffect(EffectInfo.berserk(1000L)) }),
    scythe("Scythe",
           "10% chance to scythe on shoot",
           100L,
           0.02f,
           Formatting.dark_red,
           Formatting.white,
           ClientPlayer.MainPlayerHook.on_shoot,
           { doScythe(it) }),
    elemental_burst("${Formatting.blue}Elemental Burst",
                    "Shoots a burst of five shots dealing massive damage",
                    800L,
                    0.05f,
                    Formatting.blue,
                    Formatting.white,
                    ClientPlayer.MainPlayerHook.on_shoot,
                    { doElementalBurst(it) }
    ),
    curse("Curse",
          "2.8% chance to curse on shoot",
          0L,
          0.028f,
          Formatting.red,
          Formatting.white,
          ClientPlayer.MainPlayerHook.none,
          cptv
    ),
    angels_touch("Angel's Touch",
                 "0.5% chance to heal 3.5% of max hp on shoot",
                 0L,
                 0.05f,
                 Formatting.aqua,
                 Formatting.white,
                 ClientPlayer.MainPlayerHook.on_shoot,
                 {
                     Client.player.hp += Client.player.life * 0.035
                     Client.player.hp = Client.player.hp.coerceAtMost(Client.player.life.toDouble())
                 }
    ),
    throwback("Throwback",
              "0.5% chance to add defense for 1s on shoot",
              0L,
               0.05f,
               Formatting.green,
               Formatting.white,
               ClientPlayer.MainPlayerHook.on_shoot,
              {
                  it.incStat(Stat.def, 15)
                  after(3000) {
                      it.incStat(Stat.def, -15)
                  }
              });

    val infoList:List<string> by lazy {
        listOf("${this.nameFormat}$nm",
               *(Client.font.wrapWords(desc, 300.0).map { "${this.descFormat}$it" }.toTypedArray()),
               "-------------------")
    }

    override fun toString():string {
        return "LegendaryEffect(nm='$nm', desc='$desc', delay=$delay, hook=$hook, _onActivate=$_onActivate)"
    }

    var lastActivate = 0f

    private val onActivate = { player:ClientPlayer ->
        if (measuringTimeMS() - lastActivate > delay && RandomUtils.nextInt(0, (1f / chance).toInt()) == 0) {
            player.entityNotifications.add(EntityNotification(player, "${nameFormat}$nm!", 0xffffffff))
            lastActivate = measuringTimeMS()
            _onActivate(player)
        }
    }

    fun onActivateRaw(player:AEntity) {
        player.entityNotifications.add(EntityNotification(player, "${nameFormat}$nm!", 0xffffffff))
    }

    fun addHook() {
        hook.hooked.add(onActivate)
    }

    fun removeHook() {
        hook.hooked.remove(onActivate)
    }

    companion object {

        private val scythe_proj_1 = ProjectileProperties("ChainScythe_proj_1")
        private val scythe_proj_2 = ProjectileProperties("ChainScythe_proj_2")
        private val scythe_proj_3 = ProjectileProperties("ChainScythe_proj_3")
        private val scythe_proj_4 = ProjectileProperties("ChainScythe_proj_4")
        private val scythe_proj_5 = ProjectileProperties("ChainScythe_proj_5")

        private val elemental_slasher_proj_1 = ProjectileProperties("ElementalSlasher_proj_2")
        private val elemental_slasher_proj_2 = ProjectileProperties("ElementalSlasher_proj_3")
        private val elemental_slasher_proj_3 = ProjectileProperties("ElementalSlasher_proj_4")
        private val elemental_slasher_proj_4 = ProjectileProperties("ElementalSlasher_proj_5")
        private val elemental_slasher_proj_5 = ProjectileProperties("ElementalSlasher_proj_6")

        private fun doShoot(player:ClientPlayer) {
            player.shoot(scythe_proj_1).also { it.bulletId = 0 }
            player.shoot(scythe_proj_2).also { it.bulletId = 0 }
            player.shoot(scythe_proj_3).also { it.bulletId = 0 }
            player.shoot(scythe_proj_4).also { it.bulletId = 0 }
            player.shoot(scythe_proj_5).also { it.bulletId = 0 }
        }

        private fun doScythe(player:ClientPlayer) {
            Client.ticker.add(object:Tickable(true) {
                override fun clientTick() {
                    doShoot(player)
                    release()
                }
            })
            Client.ticker.add(object:Tickable(true) {
                override fun clientTick() {
                    if (ticksExisted <= 5) return
                    doShoot(player)
                    release()
                }
            })
            Client.ticker.add(object:Tickable(true) {
                override fun clientTick() {
                    if (ticksExisted <= 10) return
                    doShoot(player)
                    release()
                }
            })
        }

        private fun doElementalBurst(player:ClientPlayer) {
            player.shoot(elemental_slasher_proj_1).also { it.bulletId = 0 }
            player.shoot(elemental_slasher_proj_2).also { it.bulletId = 0 }
            player.shoot(elemental_slasher_proj_3).also { it.bulletId = 0 }
            player.shoot(elemental_slasher_proj_4).also { it.bulletId = 0 }
            player.shoot(elemental_slasher_proj_5).also { it.bulletId = 0 }
        }
    }

}