package me.ethius.client.rotsg.renderer.entity

import me.ethius.client.rotsg.entity.Bag
import me.ethius.client.rotsg.entity.ClientPlayer
import me.ethius.client.rotsg.entity.OtherPlayer
import me.ethius.client.rotsg.entity.Portal
import me.ethius.shared.rotsg.entity.AEntity
import me.ethius.shared.rotsg.entity.enemy.Enemy
import me.ethius.shared.rotsg.entity.other.Projectile
import org.joml.Matrix4dStack

object EntityRendererDispatcher {

    val map = buildMap {
        put(Enemy::class.java, EnemyEntityRenderer())
        put(ClientPlayer::class.java, ClientPlayerEntityRenderer())
        put(OtherPlayer::class.java, OtherPlayerEntityRenderer())
        put(Projectile::class.java, ProjectileEntityRenderer())
        put(Bag::class.java, BagEntityRenderer())
        put(Portal::class.java, PortalEntityRenderer())
    }
    private val defaultEntityRenderer = DefaultEntityRenderer()

    private fun getRenderer(entity:AEntity):EntityRenderer<out AEntity> {
        return map[entity.javaClass] ?: defaultEntityRenderer
    }

    fun render(matrix:Matrix4dStack, entity:AEntity) {
        val renderer = getRenderer(entity)
        renderer.render(matrix, entity)
        EntityRenderer.renderBars(matrix, entity)
        EntityRenderer.renderEffects(matrix, entity)
    }

}