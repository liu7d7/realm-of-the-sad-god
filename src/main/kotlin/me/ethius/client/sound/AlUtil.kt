package me.ethius.client.sound

import me.ethius.shared.bool
import me.ethius.shared.int
import me.ethius.shared.long
import me.ethius.shared.string
import org.lwjgl.openal.AL10
import org.lwjgl.openal.ALC10
import javax.sound.sampled.AudioFormat

object AlUtil {
    private fun getErrorMessage(errorCode:int):string {
        return when (errorCode) {
            AL10.AL_INVALID_NAME -> "Invalid name parameter."
            AL10.AL_INVALID_ENUM -> "Invalid enumerated parameter value."
            AL10.AL_INVALID_VALUE -> "Invalid parameter parameter value."
            AL10.AL_INVALID_OPERATION -> "Invalid operation."
            AL10.AL_OUT_OF_MEMORY -> "Unable to allocate memory."
            else -> "An unrecognized error occurred."
        }
    }


    fun checkErrors(sectionName:string?):bool {
        val i = AL10.alGetError()
        return i != 0
    }

    private fun getAlcErrorMessage(errorCode:int):string {
        return when (errorCode) {
            AL10.AL_INVALID_NAME -> "Invalid device."
            AL10.AL_INVALID_ENUM -> "Invalid context."
            AL10.AL_INVALID_VALUE -> "Illegal enum."
            AL10.AL_INVALID_OPERATION -> "Invalid value."
            AL10.AL_OUT_OF_MEMORY -> "Unable to allocate memory."
            else -> "An unrecognized error occurred."
        }
    }

    fun checkAlcErrors(
        deviceHandle:long,
        sectionName:string?,
    ):bool {
        val i = ALC10.alcGetError(deviceHandle)
        return i != 0
    }


    fun getFormatId(format:AudioFormat):int {
        val encoding = format.encoding
        val i = format.channels
        val j = format.sampleSizeInBits
        if (encoding == AudioFormat.Encoding.PCM_UNSIGNED || encoding == AudioFormat.Encoding.PCM_SIGNED) {
            if (i == 1) {
                if (j == 8) {
                    return AL10.AL_FORMAT_MONO8
                }
                if (j == 16) {
                    return AL10.AL_FORMAT_MONO16
                }
            } else if (i == 2) {
                if (j == 8) {
                    return AL10.AL_FORMAT_STEREO8
                }
                if (j == 16) {
                    return AL10.AL_FORMAT_STEREO16
                }
            }
        }
        throw IllegalArgumentException("Invalid audio format: $format")
    }
}