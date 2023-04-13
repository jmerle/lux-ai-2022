package dev.jmerle.luxai2022.agent

import dev.jmerle.luxai2022.Observation
import dev.jmerle.luxai2022.action.setup.BidAction
import dev.jmerle.luxai2022.action.setup.BuildFactoryAction
import dev.jmerle.luxai2022.board.Direction
import dev.jmerle.luxai2022.board.Grid
import dev.jmerle.luxai2022.board.Tile
import dev.jmerle.luxai2022.team.Faction
import kotlin.math.abs

class SetupAgent {
    fun bid(obs: Observation): BidAction {
        return BidAction(0, Faction.ALPHA_STRIKE)
    }

    fun buildFactory(obs: Observation): BuildFactoryAction {
        val spawn = obs.me.spawns
            .filter {
                for (dy in -1..1) {
                    for (dx in -1..1) {
                        if (obs.factories[it.add(dx, dy)] != null) {
                            return@filter false
                        }
                    }
                }

                true
            }
            .maxBy { getSpawnScore(obs, it) }

        val water = obs.me.water / obs.me.factoriesToPlace
        val metal = obs.me.metal / obs.me.factoriesToPlace

        return BuildFactoryAction(spawn, water, metal)
    }

    private fun getSpawnScore(obs: Observation, spawn: Tile): Int {
        var score = 0

        for ((dx, dy) in listOf(
            -1 to -2,
            0 to -2,
            1 to -2,
            -1 to 2,
            0 to 2,
            1 to 2,
            -2 to -1,
            -2 to 0,
            -2 to 1,
            2 to -1,
            2 to 0,
            2 to 1
        )) {
            val newTile = spawn.add(dx, dy)
            if (newTile.isOnMap(obs) && obs.board.ice[newTile]) {
                score += 50
                break
            }
        }

        val floodQueue = ArrayDeque<Tile>()
        val visited = Grid(obs.config.mapSize) { false }

        floodQueue.addAll(Direction.neighbors.map { spawn.add(it) })
        for (factory in obs.me.factories) {
            floodQueue.addAll(Direction.neighbors.map { factory.tile.add(it) })
        }

        while (floodQueue.isNotEmpty()) {
            val tile = floodQueue.removeFirst()

            if (visited[tile]) {
                continue
            }

            if (obs.factories[tile] == null
                && (abs(tile.x - spawn.x) > 1 || abs(tile.y - spawn.y) > 1)
                && obs.board.rubble[tile] == 0
                && !obs.board.ice[tile]
                && !obs.board.ore[tile]) {
                score += 1
            }

            visited[tile] = true

            for (direction in Direction.neighbors) {
                val newTile = tile.add(direction)
                if (newTile.isOnMap(obs)
                    && !visited[newTile]
                    && obs.factories[newTile] == null
                    && obs.board.rubble[newTile] == 0
                    && !obs.board.ice[newTile]
                    && !obs.board.ore[newTile]) {
                    floodQueue.add(newTile)
                }
            }
        }

        return score
    }
}
