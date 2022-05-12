package me.ethius.shared.maths

import me.ethius.shared.clamp
import me.ethius.shared.double
import me.ethius.shared.float
import me.ethius.shared.long
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt

/* All of these can be used to retrieve a value between 0 and 1. These can be used to get sick looking animations in whatever. */
object Animations {

    fun getLinearAnimation(
        duration:float,
        time:float,
    ):float {
        val time = clamp(time, 0f, duration)
        return time / duration
    }

    fun getDecelerateAnimation(
        duration:long,
        time:long,
    ):float {
        val time = clamp(time, 0L, duration)
        val x1 = time.toFloat() / duration.toFloat()
        return 1 - (x1 - 1) * (x1 - 1)
    }

    fun getDecelerateAnimation(
        duration:float,
        time:float,
    ):float {
        val time = clamp(time, 0f, duration)
        val x1 = time / duration
        return 1 - (x1 - 1) * (x1 - 1)
    }

    fun getDecelerateAnimation(
        duration:double,
        time:double,
    ):double {
        val time = clamp(time, 0.0, duration)
        val x1 = time / duration
        return 1f - (x1 - 1) * (x1 - 1)
    }

    fun getDecelFastAnimation(
        duration:float,
        time:float,
    ):float =
        getDecelerateAnimation(duration, time).pow(15.0f)

    fun getAccelerateAnimation(
        duration:long,
        time:long,
    ):float {
        val time = clamp(time, 0L, duration)
        val x1 = time.toFloat() / duration.toFloat()
        return (x1 - 1) * (x1 - 1)
    }

    fun getAccelerateAnimation(
        duration:float,
        time:float,
    ):float {
        val time = clamp(time, 0f, duration)
        val x1 = time / duration
        return 1 - sqrt(1 - x1)
    }

    fun getAccelerateAnimation(
        duration:double,
        time:double,
    ):double {
        val time = clamp(time, 0.0, duration)
        val x1 = time / duration
        return 1 - sqrt(1 - x1)
    }

    fun getAccelFastAnimation(
        duration:float,
        time:float,
    ):float =
        getAccelerateAnimation(duration, time).pow(12.0f)

    fun getSmooth2Animation(
        duration:long,
        time:long,
    ):float {
        val time = clamp(time, 0L, duration)
        val x1 = time.toFloat() / duration.toFloat() //Used to force input to range from 0 - 1
        return (6 * x1.pow(5.0f) - 15 * x1.pow(4.0f) + 10 * x1.pow(3.0f))
    }

    fun getSmooth2Animation(
        duration:float,
        time:float,
    ):float {
        val time = clamp(time, 0f, duration)
        val x1 = time / duration //Used to force input to range from 0 - 1
        return (6 * x1.pow(5.0f) - 15 * x1.pow(4.0f) + 10 * x1.pow(3.0f))
    }

    fun getLogisticAnimation(
        duration:float,
        time:float,
    ):float {
        val time = clamp(time, 0f, duration)
        //Logistic Function where maximum is max ms but divided by ms to return 0 - 1
        return duration / (1 + exp((-2 * (time - duration / 2f)))) / duration
    }

    fun upAndDownCurve(
        duration:double,
        time:double,
    ):double {
        return if (time < duration / 2.0) {
            getDecelerateAnimation(duration / 2.0, time)
        } else {
            1 - getAccelerateAnimation(duration / 2.0, time - duration / 2.0)
        }
    }

}