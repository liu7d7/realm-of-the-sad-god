package me.ethius.client

import com.moandjiezana.toml.Toml
import me.ethius.shared.long
import me.ethius.shared.readCached
import me.ethius.shared.rotsg.entity.player.PlayerProfile
import me.ethius.shared.toml
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.io.path.nameWithoutExtension

object Profiles {

    class GlobalProfile {
        var fame:long = 0
    }

    val profiles = CopyOnWriteArrayList<PlayerProfile>()
    var global:GlobalProfile

    fun write() {
        for (profile in profiles) {
            profile.write()
        }
        for (i in Files.walk(Paths.get("saves/"))) {
            if (i.toString().endsWith(".dat") && !profiles.any { it.name == i.nameWithoutExtension }) {
                i.toFile().renameTo(Paths.get("saves/", i.nameWithoutExtension + ".dat.deleted").toFile())
            }
        }
        toml.write(global, File("global.dat"))
    }

    init {
        global = GlobalProfile()
        if (Files.exists(Paths.get("global.dat"))) {
            global = Toml().readCached(Paths.get("global.dat").toFile()).to(GlobalProfile::class.java)
        }
        if (!Files.exists(Paths.get("saves/"))) {
            Files.createDirectory(Paths.get("saves/"))
        } else {
            for (i in Files.walk(Paths.get("saves/"))) {
                val str = i.toString()
                if (str.endsWith(".dat")) {
                    val profile = PlayerProfile.read("saves/${i.fileName}")
                    profiles.add(profile)
                }
            }
        }
    }

}