package org.powbot.krulvis.blastfurnace

import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.mobile.drawing.Graphics
import java.awt.Graphics2D

class BFPainter(script: BlastFurnace) : ATPainter<BlastFurnace>(script, 8, 300) {
    override fun paint(g: Graphics, startY: Int) {
        var y = startY
        val bar = script.bar
        val prim = bar.primary
        val coal = bar.secondary
        y = drawSplitText(g, "Leaf:", script.lastLeaf.name, x, y)
        y = drawSplitText(g, "Bars: ", bar.amount.toString(), x, y)
        y = drawSplitText(g, "${prim.name} Ore: ", prim.amount.toString(), x, y)
        if (prim != Ore.GOLD) {
            y = drawSplitText(g, "Coal: ", coal.amount.toString(), x, y)
        }
        y = script.skillTracker.draw(g, x, y)
        y = script.lootTracker.drawLoot(g, x, y)
    }

    override fun drawTitle(g: Graphics, x: Int, y: Int) {
        Companion.drawTitle(g, "Blast Furnace", x, y)
    }
}