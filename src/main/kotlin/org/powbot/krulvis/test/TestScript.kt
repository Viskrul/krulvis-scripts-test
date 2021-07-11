package org.powbot.krulvis.test

import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.ATContext.ctx
import org.powbot.krulvis.api.ATContext.debug
import org.powbot.krulvis.api.ATContext.debugComponents
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.toRegionTile
import org.powbot.krulvis.api.extensions.items.Item.Companion.VIAL
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.krulvis.api.script.tree.Leaf
import org.powbot.krulvis.api.script.tree.TreeComponent
import org.powbot.krulvis.api.utils.LastMade.stoppedMaking
import org.powbot.krulvis.tithe.Data
import org.powbot.krulvis.tithe.Patch
import org.powbot.krulvis.tithe.Patch.Companion.isPatch
import org.powerbot.bot.rt4.client.internal.IActor
import org.powerbot.script.*
import org.powerbot.script.rt4.*
import java.awt.Graphics2D
import java.awt.Rectangle
import java.util.*

@Script.Manifest(name = "TestScript", description = "Some testing", version = "1.0")
class TestScript : ATScript(), GameActionListener {


    val tile = Tile(7075, 3761, 0)

    override val painter: ATPainter<*>
        get() = Painter(this)

    var crate: Optional<GameObject> = Optional.empty()

    var patches = listOf<Patch>()
    override val rootComponent: TreeComponent<*> = object : Leaf<TestScript>(this, "TestLeaf") {
        override fun execute() {
            ctx.objects.toStream().at(Tile(3752, 5674, 0)).list().forEach {
                println("${it.name()}, actions=${it.actions().joinToString()}")
            }
            crate = ctx.objects.toStream().at(Tile(3752, 5674, 0)).name("Crate").findFirst()
            println("Crate object present=${crate.isPresent}")
        }
    }

    fun getCornerPatchTile(): Tile {
        val allPatches = ctx.objects.toStream(25).filter { it.isPatch() }.list()
        val maxX = allPatches.minOf { it.tile().x() }
        val maxY = allPatches.maxOf { it.tile().y() }
        return Tile(maxX + 5, maxY, 0)
    }

    val inputTexts = listOf(
        "enter message to", "enter amount", "set a price for each item",
        "enter name", "how many do you wish to buy", "please pick a unique display name",
        "how many charges would you like to add", "enter the player name",
        "what would you like to buy"
    )

    fun waitingForInput(): Boolean {
        val widget = ctx.widgets.widget(162).takeIf { w ->
            w.any { wc ->
                val text = wc.text().toLowerCase()
                wc.visible() && inputTexts.any { it in text }
            }
        }
        return widget?.any { it.visible() && it.text().endsWith("*") } ?: false
    }

    override fun startGUI() {
        debugComponents = true
        started = true
    }

    override fun stop() {
        painter.saveProgressImage()
    }

    override fun onAction(evt: GameActionEvent) {
        val tithe = ctx.objects.toStream().id(evt.id).nearest().findFirst()

        println("Var0: ${evt.var0}, WidgetId: ${evt.widgetId}, Interaction: ${evt.interaction}, ID: ${evt.id}, Name: ${evt.rawEntityName}, OpCode: ${evt.rawOpcode}")
        if (tithe.isPresent) {
            val localTile = tithe.get().tile().toRegionTile()
            println("Nearest ${evt.rawEntityName} farm: Local X: ${localTile.x()}, Y: ${localTile.y()}")
        }
    }

}

class Painter(script: TestScript) : ATPainter<TestScript>(script, 10) {
    override fun paint(g: Graphics2D) {
        var y = this.y
        val crate = script.crate

        crate.ifPresent {
            it.tile().drawOnScreen(g, "Crate")
        }
    }


    override fun drawProgressImage(g: Graphics2D, startY: Int) {
        var y = startY
        g.drawString("Test string", x, y)
        y += yy
    }
}