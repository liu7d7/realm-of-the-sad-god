package me.ethius.shared.rotsg.data

import me.ethius.shared.int
import me.ethius.shared.long
import me.ethius.shared.opti.TexData
import me.ethius.shared.rotsg.entity.AEntity
import me.ethius.shared.rotsg.entity.Stat
import me.ethius.shared.rotsg.entity.StatEntity
import me.ethius.shared.rotsg.entity.other.Effect
import me.ethius.shared.string
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.jvmErasure

// duration:long, amplifier:int
class EffectInfo(val supplier:(long, int) -> Effect) {

    lateinit var id:string

    operator fun invoke(p1:long, p2:int):Effect {
        return supplier(p1, p2).also { it.id = this.id }
    }

    operator fun invoke(p1:long):Effect {
        return invoke(p1, -1)
    }

    companion object {
        private val values = HashMap<string, EffectInfo>()

        val shield = EffectInfo { duration, amplifier ->
            Effect(duration, TexData.shield_effect, amplifier)
        }
        val berserk = EffectInfo { duration, amplifier ->
            Effect(duration, TexData.berserk_effect, amplifier)
        }
        val damaging = EffectInfo { duration, amplifier ->
            Effect(duration, TexData.damaging_effect, amplifier)
        }
        val sick = EffectInfo { duration, amplifier ->
            Effect(duration, TexData.sick_effect, amplifier)
        }
        val curse = EffectInfo {  duration, amplifier ->
            Effect(duration, TexData.curse_effect, amplifier)
        }
        val atk_add = EffectInfo { duration, amplifier ->
            object:Effect(duration, TexData.atk_add_effect, amplifier) {
                override fun onAdd(entt:AEntity) {
                    super.onAdd(entt)
                    if (entt is StatEntity) {
                        entt.incStat(Stat.atk, amplifier, false)
                    }
                }

                override fun onRem(entt:AEntity) {
                    if (entt is StatEntity) {
                        entt.incStat(Stat.atk, -amplifier, false)
                    }
                }
            }
        }

        /**
         * precondition: Effect.getData("sourceId") returns the source entityId
         */
        val bleed = EffectInfo { duration, amplifier ->
            object:Effect(duration, TexData.bleed_effect, amplifier) {
                override fun serverTick() {
                    if (this.enttInit) {
                        val invAmp = (5 - amplifier).coerceIn(1..5)
                        if (delayNumSeconds(0.2 * invAmp)) {
                            val source = this.getData("sourceId") as long
                            entt.damage(invAmp * 10.0, true, source)
                        }
                    }
                }
            }
        }

        fun fromString(str:string):Effect? {
            val split = str.split("|")
            val id = split[0]
            val duration = split[1].toLong()
            val amplifier = split[2].toInt()
            val initTime = split[3].toLong()
            val effect = (values[id] ?: return null)(duration, amplifier).also {
                it.initTime = initTime
            }
            for (i in 4 until split.size) {
                val split1 = split[i].split(":")
                val key = split1[0]
                val value = split1[1]
                when (key) {
                    "sourceId" -> effect.pushData(key, value.toLong())
                    else -> effect.pushData(key, value)
                }
            }
            return effect
        }

        operator fun get(str:string):EffectInfo? {
            return values[str]
        }

        fun init() {
            for (field in this::class.declaredMemberProperties) {
                if (field.returnType.jvmErasure == EffectInfo::class) {
                    val effect = ((field as KProperty1<Any, *>).get(this as Any) as EffectInfo)
                    effect.id = field.name
                    values[effect.id] = effect
                }
            }
        }
    }

}