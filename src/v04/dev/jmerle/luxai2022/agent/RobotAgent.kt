package dev.jmerle.luxai2022.agent

import dev.jmerle.luxai2022.Observation
import dev.jmerle.luxai2022.action.ActionMap
import dev.jmerle.luxai2022.action.robot.RobotAction
import dev.jmerle.luxai2022.board.Direction
import dev.jmerle.luxai2022.board.Grid
import dev.jmerle.luxai2022.board.NullableGrid
import dev.jmerle.luxai2022.board.Resource
import dev.jmerle.luxai2022.board.Tile
import dev.jmerle.luxai2022.team.Robot
import java.util.PriorityQueue
import kotlin.math.ceil
import kotlin.math.min

class RobotAgent {
    private var factoryId: String? = null
    private var actionQueueCooldown = 0

    fun act(obs: Observation, robot: Robot, actionMap: ActionMap): List<RobotAction>? {
        if (actionQueueCooldown > 0) {
            actionQueueCooldown--
        }

        if (actionQueueCooldown > 0 || obs.me.factories.none()) {
            return null
        }

        val defaultActions = if (robot.actionQueue.isNotEmpty()) listOf(robot.recharge(robot.power)) else null

        if (factoryId == null || obs.me.factories.none { it.entityId == factoryId }) {
            factoryId = obs.me.factories.minBy { it.tiles.minOf { tile -> tile.distanceTo(robot.tile) } }.entityId
        }

        val factory = obs.me.factories.first { it.entityId == factoryId }

        if (factory.tiles.any { it == robot.tile }) {
            if (robot.cargo.ice > 0) {
                return listOf(robot.transfer(Direction.CENTER, Resource.ICE, robot.cargo.ice))
            }

            if (robot.power < robot.config.batteryCapacity && factory.power > robot.config.batteryCapacity / 3) {
                val amount = min(robot.config.batteryCapacity - robot.power, factory.power)
                return listOf(robot.pickup(Resource.POWER, amount.toInt()))
            }

            if (robot.power < robot.config.batteryCapacity / 2) {
                return defaultActions
            }
        }

        if (robot.cargo.ice == 0
            && obs.board.ice[robot.tile]
            && obs.factories[robot.tile] == null
            && obs.board.lichen[robot.tile] == 0) {
            val returnMoveCost = factory.tiles.minOf { getPath(obs, robot, robot.tile, it).last().second }
            val returnCost = robot.actionQueueCost() + returnMoveCost

            val maxRubbleDig = ceil(obs.board.rubble[robot.tile].toDouble() / robot.config.digRubbleRemoved).toInt()
            val maxResourceDig = ceil(robot.config.cargoSpace.toDouble() / robot.config.digResourceGain).toInt()
            val maxDig = maxRubbleDig + maxResourceDig

            val availableDigPower = robot.power - returnCost - robot.actionQueueCost()
            val maxDigPower = (availableDigPower / robot.digCost()).toInt()

            val digTimes = min(maxDigPower, maxDig)
            if (digTimes <= 0) {
                return defaultActions
            }

            actionQueueCooldown = digTimes
            return listOf(robot.dig(true))
        }

        val target = if (robot.cargo.ice == 0) {
            obs.board.ice.tiles()
                .filter { it.value && obs.factories[it.tile] == null && obs.board.lichen[it.tile] == 0 }
                .minBy { tile -> factory.tiles.minOf { it.distanceTo(tile.tile) } }
                .tile
        } else {
            factory.tiles.minBy { it.distanceTo(robot.tile) }
        }

        val path = getPath(obs, robot, robot.tile, target)
        val availablePower = robot.power - robot.actionQueueCost()

        val possibleMoves = path.dropLastWhile { it.second > availablePower }.take(obs.config.unitActionQueueSize)
        if (possibleMoves.isEmpty()) {
            return defaultActions
        }

        actionQueueCooldown = possibleMoves.size
        return possibleMoves.map { robot.move(it.first) }
    }

    private fun getPath(obs: Observation, robot: Robot, from: Tile, to: Tile): List<Pair<Direction, Double>> {
        val distances = Grid(obs.config.mapSize) { Double.MAX_VALUE }
        val previous = NullableGrid<Tile>(obs.config.mapSize)

        val queue = PriorityQueue<Tile>(Comparator.comparing { distances[it] })

        distances[from] = 0.0
        queue.add(from)

        while (queue.isNotEmpty()) {
            val current = queue.poll()

            if (current == to) {
                val path = mutableListOf<Pair<Direction, Double>>()

                var tile = current
                while (tile != from) {
                    val previousTile = previous[tile]!!
                    path.add(previousTile.directionTo(tile) to distances[tile])
                    tile = previousTile
                }

                return path.reversed()
            }

            for (direction in Direction.neighbors) {
                val neighbor = current.add(direction)
                if (!neighbor.isOnMap(obs)) {
                    continue
                }

                val newDistance =
                    distances[current] + robot.config.moveCost + robot.config.rubbleMovementCost * obs.board.rubble[neighbor]

                if (newDistance < distances[neighbor]) {
                    distances[neighbor] = newDistance
                    previous[neighbor] = current
                    queue.add(neighbor)
                }
            }
        }

        throw IllegalArgumentException("Cannot find path from $from to $to, but one should exist")
    }
}
