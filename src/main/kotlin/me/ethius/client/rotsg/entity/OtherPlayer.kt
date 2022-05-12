package me.ethius.client.rotsg.entity

import me.ethius.shared.copy
import me.ethius.shared.ext.distanceSquared
import me.ethius.shared.ext.isZero
import me.ethius.shared.maths.Facing
import me.ethius.shared.opti.TexData
import me.ethius.shared.rotsg.entity.AEntity
import me.ethius.shared.rotsg.entity.player.Player
import me.ethius.shared.rotsg.entity.player.PlayerClass
import me.ethius.shared.rotsg.entity.player.PlayerProfile
import me.ethius.shared.rotsg.tile.tile_size
import me.ethius.shared.string

class OtherPlayer(pClass:PlayerClass, playerProfile:PlayerProfile):Player(pClass, playerProfile) {
    override var texDataId:string = "empty"
    var texData:TexData = TexData.empty
    var lastFacing:Facing = Facing.down

    override fun collideWith(other:AEntity) {

    }

    override fun clientTick() {
        this.prevX = this.x
        this.prevY = this.y
        if (!serverX.isNaN()) {
            moveTo(serverX, serverY)
            this.serverX = Double.NaN
            this.serverY = Double.NaN
        }
        if (this.pos.distanceSquared(this.lastWalkTexUpdate) > tile_size * tile_size) {
            this.lastWalkTexUpdate.x = this.x
            this.lastWalkTexUpdate.y = this.y
            this.walkTex++
        }
        updateTexture()
    }

    private fun updateTexture() {
        this.texXOffset = 0.0
        val delta = this.renderPos.copy().sub(this.prevRenderPos)
        val facing:Facing = if (delta.x > 0.0) {
            Facing.right
        } else if (delta.x < 0.0) {
            Facing.left
        } else if (delta.y > 0.0) {
            Facing.down
        } else if (delta.y < 0.0) {
            Facing.up
        } else {
            lastFacing
        }
        lastFacing = facing
        this.texData = when (facing) {
            Facing.up -> {
                texXOffset = 0.024
                if (!delta.isZero()) {
                    when (walkTex % 2) {
                        1 -> this.pTexData.up_w1
                        0 -> this.pTexData.up_w2
                        else -> this.pTexData.up_w1
                    }
                } else {
                    pTexData.up
                }
            }
            Facing.down -> {
                texXOffset = -0.024
                if (!delta.isZero()) {
                    when (walkTex % 2) {
                        1 -> this.pTexData.down_w1
                        0 -> this.pTexData.down_w2
                        else -> this.pTexData.down_w1
                    }
                } else {
                    pTexData.down
                }
            }
            Facing.left -> {
                if (!delta.isZero()) {
                    when (walkTex % 2) {
                        1 -> this.pTexData.left_w1
                        0 -> this.pTexData.left_w2
                        else -> this.pTexData.left_w1
                    }
                } else {
                    pTexData.left
                }
            }
            Facing.right -> {
                if (!delta.isZero()) {
                    when (walkTex % 2) {
                        1 -> this.pTexData.right_w1
                        0 -> this.pTexData.right_w2
                        else -> this.pTexData.right_w1
                    }
                } else {
                    pTexData.right
                }
            }
        }
    }

}