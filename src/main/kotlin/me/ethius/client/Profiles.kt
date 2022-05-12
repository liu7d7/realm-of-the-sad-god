package me.ethius.client

import me.ethius.shared.rotsg.entity.player.PlayerProfile
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.io.path.nameWithoutExtension

object Profiles {

    val profiles = CopyOnWriteArrayList<PlayerProfile>()

    fun write() {
        for (profile in profiles) {
            profile.write()
        }
        for (i in Files.walk(Paths.get("saves/"))) {
            if (i.toString().endsWith(".dat") && !profiles.any { it.name == i.nameWithoutExtension }) {
                i.toFile().renameTo(Paths.get("saves/", i.nameWithoutExtension + ".dat.deleted").toFile())
            }
        }
    }

    init {
        if (!Files.exists(Paths.get("saves/"))) {
            Files.createDirectory(Paths.get("saves/"))
        } else {
            for (i in Files.walk(Paths.get("saves/"))) {
                if (i.toString().endsWith(".dat")) {
                    val profile = PlayerProfile.read("saves/${i.fileName}")
                    profiles.add(profile)
                }
            }
        }
    }

}