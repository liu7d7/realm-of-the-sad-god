package me.ethius.client.rotsg.item

import com.moandjiezana.toml.Toml
import me.ethius.client.Client
import me.ethius.client.rotsg.gui.ldu
import me.ethius.client.rotsg.inventory.item_size
import me.ethius.client.rotsg.inventory.slot_width
import me.ethius.shared.*
import me.ethius.shared.opti.TexData
import me.ethius.shared.rotsg.entity.Stat
import org.joml.Matrix4dStack

const val item_data_loc = "/assets/data/item"

open class Item {

    var statMap = mutableMapOf<Stat, int>()

    constructor(texData:TexData, tier:ItemTier, name:string, desc:string) {
        this.texData = texData
        this.tier = tier
        this.name = name
        this.desc = desc
    }

    constructor(assetLoc:string) {
        val toml = Toml().readCached("$item_data_loc/$assetLoc.dat")
        // item meta
        val meta = toml.getTable("meta")
        this.name = meta.getString("name")
        this.desc = meta.getString("desc")
        this.tier = ItemTier.valueOf(meta.getString("tier"))
        this.texData = TexData[meta.getString("tex_data")]
        run {
            if (meta.containsTable("stats")) {
                val stm = meta.getTable("stats").toMap()
                for (i in stm) {
                    statMap[Stat.valueOf(i.key)] = (i.value as long).toInt()
                }
            }
        }
        if (meta.contains("legendary_effect")) {
            this.legendaryEffect = LegendaryEffect.valueOf(meta.getString("legendary_effect"))
        }
    }

    var texData:TexData
    var tier:ItemTier
    var name:string
    var desc:string

    var x:double = 0.0
    var y:double = 0.0
    var id:string = ""
    var centered:bool = true
    var dragging = false
    var toolTipData:List<string> = emptyList()
    var toolTipHeight:double = 0.0
    var toolTipWidth:double = 0.0
    var equipped:bool = false
    var legendaryEffect:LegendaryEffect? = null

    override fun equals(other:Any?):Boolean {
        if (other !is Item) return false
        return other.id == id
    }

    override fun hashCode():Int {
        return id.hashCode()
    }

    fun render(
        matrix:Matrix4dStack,
        x:double,
        y:double,
        centered:bool = true,
    ) {
        this.x = if (dragging) Client.mouse.x.toDouble() else x
        this.y = if (dragging) Client.mouse.y.toDouble() else y
        this.centered = centered
        val rx = this.x - (if (centered) item_size * 0.5 else 0.0)
        val ry = this.y - (if (centered) item_size * 0.5 else 1.0)
        Client.render.drawTexWithoutEnding(texData, matrix, rx, ry, 0.0, 0xffffffff, item_size, item_size)
    }

    fun renderTier(matrix:Matrix4dStack, x:double, y:double) {
        if (!dragging)
            Client.font.drawLeftWithoutEnding(matrix,
                                              tier.displayString,
                                              x + slot_width - 7.5f,
                                              y + slot_width - 15f,
                                              tier.displayColor,
                                              true,
                                              0.65)
    }

    fun renderToolTip(matrix:Matrix4dStack) {
        if (dragging || !isIn()) return
        if (toolTipData.isEmpty()) {
            toolTipData = getTooltip()
            toolTipHeight = toolTipData.sumOf { Client.font.getHeight(true) + 5.0 } + 5.0
            toolTipWidth = toolTipData.maxOf { Client.font.getWidth(it) } + 2.5f
        }
        val mx = Client.mouse.x.toDouble()
        val my = Client.mouse.y.toDouble()
        val list = toolTipData
        var rx = mx
        var ry = my
        if (toolTipWidth + mx > Client.window.scaledWidth)
            rx -= toolTipWidth
        if (ry + toolTipHeight > Client.window.scaledHeight)
            ry -= toolTipHeight
        Client.render.drawRectAlphaWithoutEnding(matrix, rx, ry, rx + toolTipWidth, ry + toolTipHeight, ldu, 0.9f)
        for ((i, v) in list.withIndex()) {
            Client.font.drawWithoutEnding(matrix,
                                          v,
                                          rx + 2.5f,
                                          ry + 2.5f + i * (Client.font.getHeight(true) + 5f),
                                          0xffffffff,
                                          true)
        }
        Client.font.drawLeftWithoutEnding(matrix,
                                          tier.displayString,
                                          rx + toolTipWidth - 5f,
                                          ry + 2.5f,
                                          tier.displayColor,
                                          true)
    }

    private fun isIn():bool {
        return Client.mouse.x > x - slot_width / 2f && Client.mouse.x < x + slot_width / 2f && Client.mouse.y > y - slot_width / 2f && Client.mouse.y < y + slot_width / 2f
    }

    fun onEquipExt() {
        if (!equipped && Client.playerInit) {
            equipped = true
            legendaryEffect?.addHook()
            if (Client.playerInit) {
                for ((k, v) in statMap) {
                    Client.player.incStat(k, v)
                }
            }
            onEquip()
        }
    }

    protected open fun onEquip() {

    }

    open fun onDequipExt() {
        if (equipped && Client.playerInit) {
            equipped = false
            legendaryEffect?.removeHook()
            if (Client.playerInit) {
                for ((k, v) in statMap) {
                    Client.player.incStat(k, v)
                }
            }
            onDequip()
        }
    }

    protected open fun onDequip() {

    }

    open fun getTooltip():List<string> {
        return listOf(name, *Client.font.wrapWords(desc, 300.0))
    }

    open fun consume():bool {
        return false
    }

}

