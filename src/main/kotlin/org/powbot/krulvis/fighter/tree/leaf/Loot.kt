package org.powbot.krulvis.fighter.tree.leaf

import org.powbot.api.rt4.Equipment
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Npcs
import org.powbot.api.rt4.Players
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.distance
import org.powbot.krulvis.api.ATContext.getCount
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.fighter.Fighter

class Loot(script: Fighter) : Leaf<Fighter>(script, "Looting") {
    override fun execute() {
        val loots = script.loot()

        script.log.info("Looting: name=${loots.first().name()} at=${loots.first().tile}")
        loots.forEachIndexed { i, gi ->
            val id = gi.id()
            if ((!gi.stackable() || !Inventory.containsOneOf(id)) && Inventory.isFull() && script.food?.inInventory() == true) {
                script.food!!.eat()
            }
            val currentCount = Inventory.getCount(id)
            if (gi.interact("Take") && (i == loots.size - 1 || gi.distance() >= 1)) {
                if (script.equipment.firstOrNull { it.slot == Equipment.Slot.QUIVER }?.id == id) {
                    waitFor(5000) { script.loot().any { loot -> loot.id() != id } }
                }
                waitFor { currentCount < Inventory.getCount(gi.id()) }
            }
        }
    }
}