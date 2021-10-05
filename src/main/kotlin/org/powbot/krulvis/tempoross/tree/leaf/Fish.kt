package org.powbot.krulvis.tempoross.tree.leaf

import org.powbot.api.Tile
import org.powbot.api.rt4.Camera
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Npc
import org.powbot.api.rt4.Npcs
import org.powbot.krulvis.api.ATContext.debug
import org.powbot.krulvis.api.ATContext.distance
import org.powbot.krulvis.api.ATContext.interact
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.moving
import org.powbot.api.rt4.walking.local.LocalPathFinder
import org.powbot.api.rt4.walking.local.Utils.getWalkableNeighbor
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Random
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tempoross.Data.DOUBLE_FISH_ID
import org.powbot.krulvis.tempoross.Tempoross

class Fish(script: Tempoross) : Leaf<Tempoross>(script, "Fishing") {

    override fun execute() {
        val fishSpot = script.bestFishSpot
        if (fishSpot == null) {
            script.log.info("No safe fishing spot found!")
            if (script.blockedTiles.contains(me.tile())) {
                val safeTile = findSaveTile(me.tile())
                script.log.info("We are standing on a dangerous tile! Walking to $safeTile")
                if (safeTile != null && Movement.step(safeTile)) {
                    waitFor { me.tile() == safeTile }
                }
            } else if (script.fishSpots.any { it.second.actions.last().destination.distance() <= 1 }) {
                script.log.info("Nearby blocked fishing spot found that is blocked")
                val blockedTile =
                    script.fishSpots.filter { it.second.actions.last().destination.distance() <= 1 }
                        .first().second.actions.last()
                val fireOptional =
                    Npcs.stream().name("Fire").within(blockedTile.destination, 2.0).nearest().findFirst()
                if (fireOptional.isPresent) {
                    script.log.info("Dousing nearby fire...")
                    val fire = fireOptional.get()
                    if (interact(fire, "Douse")) {
                        waitFor { Npcs.stream().at(fire.tile()).name("Fire").isEmpty() }
                    }
                }
            } else {
                script.log.info("No fishing spot found, walking to Totem pole / anchor")
                var path = LocalPathFinder.findPath(script.side.totemLocation)
                if (path.isEmpty()) {
                    path = LocalPathFinder.findPath(script.side.anchorLocation)
                }
                script.walkWhileDousing(path, false)
            }
            return
        }
        val interacting = me.interacting()
        val currentSpot = if (interacting is Npc) interacting else null

        if (currentSpot?.name() == "Fishing spot") {
            if (script.blockedTiles.contains(me.tile())
                || (currentSpot.id() != DOUBLE_FISH_ID && fishSpot.id() == DOUBLE_FISH_ID)
            ) {
                println("Moving to double/save fish spot!")
                fishAtSpot(fishSpot)
            } else {
                val tetherPole = script.getTetherPole()
                if (tetherPole != null && !tetherPole.inViewport()) {
                    if (script.side.oddFishingSpot.distance() <= 1) {
                        println("Fishing at weird spot so using unique camera rotation")
                        Camera.pitch(Random.nextInt(1200, 1300))
                    } else {
                        Camera.turnTo(tetherPole)
                    }
                }
            }
        } else {
            println("Fishing at first spot")
            fishAtSpot(fishSpot)
        }
    }

    fun findSaveTile(tile: Tile): Tile? {
        return tile.getWalkableNeighbor(diagonalTiles = true) {
            !script.blockedTiles.contains(it)
        }
    }

    fun fishAtSpot(spot: Npc) {
        if (interact(spot, "Harpoon")) {
            waitFor(long()) { (me.animation() != -1 && me.interacting()?.name() == "Fishing spot") || !spot.valid() }
        } else if (Movement.moving()) {
            waitFor(long()) { spot.distance() <= 2 }
        }
    }

}
