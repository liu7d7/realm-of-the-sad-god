package me.ethius.shared.rotsg.data

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

class EffectInfo(val supplier:(long) -> Effect) {

    lateinit var id:string

    operator fun invoke(p1:Long):Effect {
        return supplier(p1).also { it.id = this.id }
    }

    companion object {
        private val emptyFunc = { _:AEntity, _:Effect -> }

        private val values = HashMap<string, EffectInfo>()

        val shield = EffectInfo { Effect(it, TexData.shield_effect, emptyFunc, emptyFunc) }
        val berserk = EffectInfo { Effect(it, TexData.berserk_effect, emptyFunc, emptyFunc) }
        val damaging = EffectInfo { Effect(it, TexData.damaging_effect, emptyFunc, emptyFunc) }
        val sick = EffectInfo { Effect(it, TexData.sick_effect, emptyFunc, emptyFunc) }
        val curse = EffectInfo { Effect(it, TexData.curse_effect, emptyFunc, emptyFunc) }
        val atk_add_1 = EffectInfo { duration ->
            Effect(duration,
                   TexData.atk_add_effect,
                   { entt:AEntity, eff:Effect -> if (entt is StatEntity) entt.incStat(Stat.atk, 1) },
                   { entt:AEntity, eff:Effect -> if (entt is StatEntity) entt.incStat(Stat.atk, -1) })
        }
        val atk_add_2 = EffectInfo { duration ->
            Effect(duration,
                   TexData.atk_add_effect,
                   { entt:AEntity, eff:Effect -> if (entt is StatEntity) entt.incStat(Stat.atk, 2) },
                   { entt:AEntity, eff:Effect -> if (entt is StatEntity) entt.incStat(Stat.atk, -2) })
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