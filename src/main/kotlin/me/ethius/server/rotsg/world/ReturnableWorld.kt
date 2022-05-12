package me.ethius.server.rotsg.world

import me.ethius.client.rotsg.entity.Portal
import me.ethius.shared.string

abstract class ReturnableWorld(name:string, vararg entities:me.ethius.shared.rotsg.entity.AEntity):ServerWorld(name, *entities) {

    private val portal:Portal

    init {
        addEntity(Portal("realm_portal", Realm.worldId, "Realm").also {
            it.invulnerable = true
            portal = it
        })
    }

}