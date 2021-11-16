package org.powbot.krulvis.api.extensions.items

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Game
import org.powbot.krulvis.api.ATContext.currentHP
import org.powbot.krulvis.api.ATContext.maxHP
import org.powbot.krulvis.api.ATContext.missingHP
import org.powbot.api.Random
import org.powbot.krulvis.smither.Smithable
import java.io.Serializable
import kotlin.math.ceil

enum class Food(val healing: Int, override vararg val ids: Int) : Item, Serializable {

    SHRIMP(3, 315),
    MEAT(3, 2142),
    CHOCOLATE_BAR(4, 1973),
    CAKES(5, 1891, 1893, 1895),
    MEAT_PIE(6, 2327, 2331),
    TROUT(8, 333),
    SALMON(9, 329),
    PEACH(9, 6883),
    WINE(11, 1993),
    TUNA(10, 361),
    LOBSTER(12, 379),
    BASS(13, 365),
    SWORDFISH(14, 373),
    POTATO_CHEESE(16, 6705),
    MONKFISH(16, 7946),
    SHARK(20, 385),
    KARAMBWAN(16, 3144);

    override fun toString(): String {
        return name
    }

    fun canEat(): Boolean = missingHP() >= healing

    fun eat(): Boolean {
        nextEatPercent = Random.nextInt(25, 55)
        val item = getInvItem()
        Game.tab(Game.Tab.INVENTORY)
        return item != null
                && item
            .interact(if (this == WINE) "Drink" else "Eat")
    }

    fun requiredAmount(): Int {
        val currHealth = currentHP()
        val missingHealth = maxHP() - currHealth
        return ceil(missingHealth.toDouble() / healing.toDouble()).toInt()
    }

    override fun getCount(countNoted: Boolean): Int {
        return getInventoryCount(countNoted)
    }

    override fun hasWith(): Boolean {
        return inInventory()
    }

    companion object {

        var nextEatPercent = Random.nextInt(25, 55)

        fun getFirstFood(): Food? {
            for (f in values()) {
                if (f.inInventory()) {
                    return f
                }
            }
            return null
        }

        fun getFirstFoodBank(): Food? {
            return forId(Bank.get { true }.firstOrNull { forId(it.id) != null }?.id ?: -1)
        }

        fun forId(id: Int): Food? = values().firstOrNull { id in it.ids }
    }

}

fun main() {
    println("[\"${Food.values().map { it.name }.joinToString("\", \"")}\"]")
}