package dev.jmerle.luxai2022.agent

import dev.jmerle.luxai2022.Observation
import dev.jmerle.luxai2022.action.ActionMap
import dev.jmerle.luxai2022.action.factory.FactoryAction
import dev.jmerle.luxai2022.board.Direction
import dev.jmerle.luxai2022.config.RobotType
import dev.jmerle.luxai2022.team.Factory
import kotlin.math.ceil

class FactoryAgent {
    fun act(obs: Observation, factory: Factory, actionMap: ActionMap): FactoryAction? {
        if (factory.canBuildRobot(RobotType.HEAVY)) {
            return factory.buildRobot(RobotType.HEAVY)
        }

        if (canWaterUntilEnd(obs, factory)) {
            return factory.water()
        }

        return null
    }

    private fun canWaterUntilEnd(obs: Observation, factory: Factory): Boolean {
        var cost = 0
        val strains = obs.board.strains.clone()

        var ownedTiles = strains.count { it == factory.strain }

        var boundaries = if (ownedTiles > 0) {
            strains.tiles()
                .filter { it.value == factory.strain }
                .map { it.tile }
        } else {
            Direction.neighbors.map { factory.tile.add(it) }
        }

        for (i in 0 until obs.config.maxEpisodeLength - obs.stepPostBid + 1) {
            cost += ceil(ownedTiles.toDouble() / obs.config.lichenWateringCostFactor).toInt() + 1
            if (factory.cargo.water < cost) {
                return false
            }

            val newTiles = boundaries
                .flatMap { tile -> Direction.neighbors.map { tile.add(it) } }
                .distinct()
                .filter {
                    it.isOnMap(obs)
                        && strains[it] == -1
                        && obs.factories[it] == null
                        && obs.board.rubble[it] == 0
                        && !obs.board.ice[it]
                        && !obs.board.ore[it]
                }

            ownedTiles += newTiles.size
            boundaries = newTiles

            for (tile in newTiles) {
                strains[tile] = factory.strain
            }
        }

        return true
    }
}
