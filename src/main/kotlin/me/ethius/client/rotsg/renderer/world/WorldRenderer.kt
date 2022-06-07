package me.ethius.client.rotsg.renderer.world

import me.ethius.client.Client
import me.ethius.client.ext.getDegreesQuaternion
import me.ethius.client.ext.multiply
import me.ethius.client.ext.push
import me.ethius.client.ext.translate
import me.ethius.client.main_tex
import me.ethius.client.renderer.Mesh
import me.ethius.client.renderer.Model3d
import me.ethius.client.renderer.RenderLayer
import me.ethius.client.rotsg.renderer.entity.EntityRenderer
import me.ethius.client.rotsg.renderer.entity.EntityRendererDispatcher
import me.ethius.client.rotsg.world.ClientWorld
import me.ethius.shared.*
import me.ethius.shared.ext.NEGATIVE_Z
import me.ethius.shared.ext.POSITIVE_Z
import me.ethius.shared.ext.tickToMs
import me.ethius.shared.maths.Animations
import me.ethius.shared.opti.AnimatedTexData
import me.ethius.shared.opti.FlowingTexData
import me.ethius.shared.opti.TexData
import me.ethius.shared.rotsg.tile.*
import org.apache.commons.lang3.RandomUtils
import org.joml.Matrix4dStack

private const val white = 0xffffffff
private const val darker = 0xffbbbbbb
private const val darkest = 0xffafafaf

class WorldRenderer:Tickable(true) {

    val windX:float
        get() {
            return lerp(prevWindX, curWindX, Animations.getDecelerateAnimation(1f, (measuringTimeMS() - lastWindUpdate) / 75.tickToMs.toFloat())
            )
        }
    private var lastWindUpdate:float = measuringTimeMS()
    private var curWindX:float = 0f
    private var prevWindX:float = 0f

    fun render(matrix:Matrix4dStack, world:ClientWorld) {
        with(world) {
            if (tilesInView.isEmpty()) {
                updateTerrain(true)
            }
            Client.renderTaskTracker.layer(RenderLayer.tile)
            Client.renderTaskTracker.track {
                try {
                    for (it in tilesInView) {
                        it.renderMesh(matrix)
                        it.renderBlend(matrix)
                    }
                } catch (_:Exception) {
                    Log.warn + "Failed to render some tiles" + Log.endl
                }
            }
            Client.renderTaskTracker.layer(RenderLayer.world_feature)
            for (it in tilesInView) {
                if (it.env != null) {
                    if (it.env!!.texDataId == "empty") {
                        continue
                    }
                    it.renderOverlays(matrix)
                }
            }
            Client.renderTaskTracker.track {
                for (it in entitiesInView) {
                    if (!it.shouldRender || !this.shouldRenderEntity(it)) {
                        continue
                    }
                    EntityRendererDispatcher.render(matrix, it)
                }
            }
            Client.renderTaskTracker.layer(RenderLayer.world_feature_text)
            Client.renderTaskTracker.track {
                for (it in entitiesInView) {
                    EntityRenderer.renderNotifications(matrix, it)
                }
            }
            Client.fxManager.renderFxs(matrix)
        }
    }

    override fun clientTick() {
        if (delayNumSeconds(1.5)) {
            lastWindUpdate = measuringTimeMS()
            prevWindX = curWindX
            curWindX = (RandomUtils.nextFloat(0f, 23f) - 11.5f) / 11.5f
        }
    }

    companion object {
        fun Tile.renderMesh(matrix:Matrix4dStack) {
            if (this.texDataId != "empty") {
                matrix.push {
                    if (TexData[texDataId] !is AnimatedTexData && TexData[texDataId] !is FlowingTexData && this.randomRotationDirectionAngle != 0.0) {
                        matrix.translate(pos.x * tile_size + tile_size / 2f, pos.y * tile_size + tile_size / 2f, 1.0) {
                            matrix.multiply(POSITIVE_Z.getDegreesQuaternion(randomRotationDirectionAngle))
                        }
                    }
                    Client.render.drawTexWithoutEnding(modulatedTexData(), matrix, pos.x * tile_size, pos.y * tile_size, 0.0, 0xffffffff, tile_size, tile_size)
                }
            }
        }

        private fun Tile.renderBlend(matrix:Matrix4dStack) {
            for (i in blendOrder) {
                if (tilesAdj[i] != null && shouldBlend[i]) {
                    val tdata = TexData.blend_8x8.texData(this.hash + (i * 31) % 27, Client.worldRenderer.windX)
                    matrix.push {
                        matrix.translate(pos.x * tile_size + tile_size / 2f, pos.y * tile_size + tile_size / 2f, 1.0) {
                            matrix.multiply(POSITIVE_Z.getDegreesQuaternion(Tile.blend_rotation_arr[i]))
                        }
                        Client.render.drawTexWithoutEnding(tdata,
                                                           matrix,
                                                           pos.x * tile_size,
                                                           pos.y * tile_size,
                                                           0.0,
                                                           tilesAdj[i]!!.modulatedTexData().avgColor, tile_size, tile_size)
                    }
                }
            }
            // corners
            for (i in 0..1) {
                for (j in 2..3) {
                    if (tilesAdj[i] != null && tilesAdj[j] != null) {
                        if (tilesAdj[i]!!.shouldBlend[j] && tilesAdj[j]!!.shouldBlend[i]) {
                            val brca_idx = when (packIJ(i, j)) {
                                2 -> 0
                                4 -> 1
                                5 -> 2
                                3 -> 3
                                else -> -1
                            }
                            if (brca_idx == -1)
                                continue
                            val tdata = TexData.blend_8x8c.texData(this.hash + (i * 27) % 31, Client.worldRenderer.windX)
                            matrix.push {
                                matrix.translate(pos.x * tile_size + tile_size / 2f,
                                                 pos.y * tile_size + tile_size / 2f,
                                                 0.0) {
                                    matrix.multiply(POSITIVE_Z.getDegreesQuaternion(Tile.blend_rotation_outcorner_arr[brca_idx]))
                                }
                                Client.render.drawTexWithoutEnding(tdata,
                                                                   matrix,
                                                                   pos.x * tile_size,
                                                                   pos.y * tile_size,
                                                                   0.0,
                                                                   interpolateColor(tilesAdj[i]!!.tilesAdj[j]!!.modulatedTexData().avgColor,
                                                                                    tilesAdj[j]!!.tilesAdj[i]!!.modulatedTexData().avgColor,
                                                                                    0.5f), tile_size, tile_size)
                            }
                        }
                    }
                }
            }
        }

        private fun Tile.renderOverlays(matrix:Matrix4dStack) {
            if (env == null) {
                return
            }
            Client.renderTaskTracker.onLayer(if (this.env!!.is3d) RenderLayer.world_feature_3d else RenderLayer.world_feature) {
                env!!.renderAtTile(this, matrix, frand(this.pos.hashCode()))
            }
        }

        fun Bushery.renderAtTile2D(tile:Tile, matrix:Matrix4dStack, frand:float) {
            if (this.texDataId != "empty") {
                val centerX = (randomCenterMax * 2 * frand - randomCenterMax) * 0.5
                val centerY = (randomCenterMax * 2 * cos(frand * PI2) - randomCenterMax) * 0.5
                val r = if (Client.playerInit) Client.player.lerpedR else 0.0
                val x = tile.pos.x * tile_size + tile_size * 0.5 + centerX
                val y = tile.pos.y * tile_size + tile_size * 0.5 + centerY
                val tdata = TexData[texDataId].texData(tile.hash, Client.worldRenderer.windX)
                matrix.push {
                    matrix.translate(x, y, 0.0) {
                        matrix.multiply(NEGATIVE_Z.getDegreesQuaternion(r))
                        matrix.scale(scale, scale, 1.0)
                    }
                    Client.render.drawCenteredTexWithoutEnding(tdata, matrix, x, y)
                }
            }
        }

        fun Bushery.renderAtTile(tile:Tile, matrix:Matrix4dStack, frand:float) {
            when (this) {
                is GeneralThreeDBushery -> {
                    matrix.translate(tile.pos.x * tile_size + tile_size * 0.5, tile.pos.y * tile_size + tile_size * 0.5, 0.0) {
                        matrix.multiply(POSITIVE_Z.getDegreesQuaternion(rotation))
                        for (i in modelIds.indices) {
                            Model3d[modelIds[i]].upload(Mesh.model3dMesh, matrix, TexData[textures[i.coerceIn(textures.indices)]])
                        }
                    }
                }
                is ThreeDWall -> {
                    val top = TexData[renderData.top]
                    val left = TexData[renderData.left]
                    val right = TexData[renderData.right]
                    val front = TexData[renderData.front]
                    val back = TexData[renderData.back]
                    val mesh = Mesh.model3dMesh

                    /* top */ mesh.quad(
                        mesh.addVertex(matrix, tile.pos.x * tile_size, tile.pos.y * tile_size, 50.0).addVertex(0.0, 0.0, 1.0)
                            .tex(top.u, top.v, main_tex).color(white).next(),
                        mesh.addVertex(matrix, tile.pos.x * tile_size + tile_size, tile.pos.y * tile_size, 50.0).addVertex(0.0, 0.0, 1.0)
                            .tex(top.u, top.v + top.height, main_tex).color(white).next(),
                        mesh.addVertex(matrix, tile.pos.x * tile_size + tile_size, tile.pos.y * tile_size + tile_size, 50.0).addVertex(0.0, 0.0, 1.0)
                            .tex(top.u + top.width, top.v + top.height, main_tex).color(white).next(),
                        mesh.addVertex(matrix, tile.pos.x * tile_size, tile.pos.y * tile_size + tile_size, 50.0).addVertex(0.0, 0.0, 1.0)
                            .tex(top.u + top.width, top.v, main_tex).color(white).next())

                    /* left */ mesh.quad(
                        mesh.addVertex(matrix, tile.pos.x * tile_size, tile.pos.y * tile_size, 50.0).addVertex(-1.0, 0.0, 0.0)
                            .tex(left.u, left.v, main_tex).color(white).next(),
                        mesh.addVertex(matrix, tile.pos.x * tile_size, tile.pos.y * tile_size + tile_size, 50.0).addVertex(-1.0, 0.0, 0.0)
                            .tex(left.u + left.width, left.v, main_tex).color(white).next(),
                        mesh.addVertex(matrix, tile.pos.x * tile_size, tile.pos.y * tile_size + tile_size, 0.0).addVertex(-1.0, 0.0, 0.0)
                            .tex(left.u + left.width, left.v + left.height, main_tex).color(white).next(),
                        mesh.addVertex(matrix, tile.pos.x * tile_size, tile.pos.y * tile_size, 0.0).addVertex(-1.0, 0.0, 0.0)
                            .tex(left.u, left.v + left.height, main_tex).color(white).next())

                    /* right */ mesh.quad(
                        mesh.addVertex(matrix, tile.pos.x * tile_size + tile_size, tile.pos.y * tile_size, 50.0).addVertex(1.0, 0.0, 0.0)
                            .tex(right.u, right.v, main_tex).color(white).next(),
                        mesh.addVertex(matrix, tile.pos.x * tile_size + tile_size, tile.pos.y * tile_size + tile_size, 50.0).addVertex(1.0, 0.0, 0.0)
                            .tex(right.u + right.width, right.v, main_tex).color(white).next(),
                        mesh.addVertex(matrix, tile.pos.x * tile_size + tile_size, tile.pos.y * tile_size + tile_size, 0.0).addVertex(1.0, 0.0, 0.0)
                            .tex(right.u + right.width, right.v + right.height, main_tex).color(white).next(),
                        mesh.addVertex(matrix, tile.pos.x * tile_size + tile_size, tile.pos.y * tile_size, 0.0).addVertex(1.0, 0.0, 0.0)
                            .tex(right.u, right.v + right.height, main_tex).color(white).next())

                    /* front */ mesh.quad(
                        mesh.addVertex(matrix, tile.pos.x * tile_size, tile.pos.y * tile_size, 50.0).addVertex(0.0, -1.0, 0.0)
                            .tex(front.u, front.v, main_tex).color(white).next(),
                        mesh.addVertex(matrix, tile.pos.x * tile_size + tile_size, tile.pos.y * tile_size, 50.0).addVertex(0.0, -1.0, 0.0)
                            .tex(front.u + front.width, front.v, main_tex).color(white).next(),
                        mesh.addVertex(matrix, tile.pos.x * tile_size + tile_size, tile.pos.y * tile_size, 0.0).addVertex(0.0, -1.0, 0.0)
                            .tex(front.u + front.width, front.v + front.height, main_tex).color(white).next(),
                        mesh.addVertex(matrix, tile.pos.x * tile_size, tile.pos.y * tile_size, 0.0).addVertex(0.0, -1.0, 0.0)
                            .tex(front.u, front.v + front.height, main_tex).color(white).next())

                    /* back */ mesh.quad(
                        mesh.addVertex(matrix, tile.pos.x * tile_size, tile.pos.y * tile_size + tile_size, 50.0).addVertex(0.0, 1.0, 0.0)
                            .tex(back.u, back.v, main_tex).color(white).next(),
                        mesh.addVertex(matrix, tile.pos.x * tile_size + tile_size, tile.pos.y * tile_size + tile_size, 50.0).addVertex(0.0, 1.0, 0.0)
                            .tex(back.u + back.width, back.v, main_tex).color(white).next(),
                        mesh.addVertex(matrix, tile.pos.x * tile_size + tile_size, tile.pos.y * tile_size + tile_size, 0.0).addVertex(0.0, 1.0, 0.0)
                            .tex(back.u + back.width, back.v + back.height, main_tex).color(white).next(),
                        mesh.addVertex(matrix, tile.pos.x * tile_size, tile.pos.y * tile_size + tile_size, 0.0).addVertex(0.0, 1.0, 0.0)
                            .tex(back.u, back.v + back.height, main_tex).color(white).next())
                }
                else -> {
                    if (this.texDataId != "empty") {
                        val centerX = (randomCenterMax * 2 * frand - randomCenterMax) * 0.5
                        val centerY = (randomCenterMax * 2 * cos(frand * PI2) - randomCenterMax) * 0.5
                        val windX = Client.worldRenderer.windX * (if (randomRotationAngle == 180.0) -1.0 else 1.0)
                        val r = if (Client.playerInit) Client.player.lerpedR else 0.0
                        val tdata = TexData[texDataId].texData(tile.hash, windX.toFloat())
                        val x = tile.pos.x * tile_size + tile_size * 0.5 + centerX
                        val y = tile.pos.y * tile_size + tile_size * 0.5 + centerY
                        matrix.push {
                            matrix.translate(x, y, 0.0) {
                                matrix.multiply(NEGATIVE_Z.getDegreesQuaternion(r))
                                matrix.scale(scale, 0.0, scale)
                                matrix.multiply(POSITIVE_Z.getDegreesQuaternion(randomRotationAngle))
                            }
                            Client.render.drawTexCenteredVerticalWindyWithoutEnding(tdata, matrix,
                                                                                    x,
                                                                                    y,
                                                                                    tdata.width,
                                                                                    tdata.height,
                                                                                    0xffffffff,
                                                                                    windX,
                                                                                    windMultiplier * 0.2f * 11.5f
                            )
                        }
                    }
                }
            }
        }

        private fun packIJ(i:int, j:int):int {
            return i * 2 + j
        }
    }

}