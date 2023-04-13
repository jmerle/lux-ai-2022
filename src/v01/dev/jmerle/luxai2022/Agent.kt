package dev.jmerle.luxai2022

import dev.jmerle.luxai2022.action.ActionMap
import dev.jmerle.luxai2022.action.setup.BidAction
import dev.jmerle.luxai2022.action.setup.BuildFactoryAction
import dev.jmerle.luxai2022.action.setup.SetupAction
import dev.jmerle.luxai2022.action.setup.WaitAction
import dev.jmerle.luxai2022.board.Resource
import dev.jmerle.luxai2022.config.RobotType
import dev.jmerle.luxai2022.team.Faction
import kotlin.math.min

class Agent {
    fun setup(obs: Observation): SetupAction {
        if (obs.step == 0) {
            return BidAction(0, Faction.THE_BUILDERS)
        }

        if (obs.me.factoriesToPlace == 0) {
            return WaitAction()
        }

        val spawn = obs.me.spawns.filter { obs.factories[it] == null }.random()
        val water = min(obs.me.water, 100)
        val metal = min(obs.me.metal, 100)

        return BuildFactoryAction(spawn, water, metal)
    }

    fun act(obs: Observation): ActionMap {
        val actionMap = ActionMap()

        for (factory in obs.me.factories) {
            if (obs.config.maxEpisodeLength - obs.stepPostBid < 50 && factory.canWater()) {
                actionMap[factory] = factory.water()
                continue
            }

            if (factory.canBuildRobot(RobotType.HEAVY)) {
                actionMap[factory] = factory.buildRobot(RobotType.HEAVY)
            }
        }

        for (robot in obs.me.robots) {
            if (robot.power < robot.actionQueueCost() || obs.me.factories.none()) {
                continue
            }

            if (robot.cargo.ice < 40) {
                val closestIce = obs.board.ice.tiles()
                    .filter { it.value > 0 }
                    .filter { obs.robots[it.tile] == null || obs.robots[it.tile] == robot }
                    .filter { obs.factories[it.tile] == null }
                    .minByOrNull { it.tile.distanceTo(robot.tile) }

                if (closestIce?.tile == robot.tile) {
                    if (robot.power >= robot.actionQueueCost() + robot.digCost()) {
                        actionMap[robot] = listOf(robot.dig())
                    }
                } else if (closestIce != null) {
                    val direction = robot.tile.directionTo(closestIce.tile)
                    if (robot.power >= robot.actionQueueCost() + robot.moveCost(robot.tile, direction)) {
                        actionMap[robot] = listOf(robot.move(direction))
                    }
                }
            } else {
                val closestFactory = obs.me.factories.minBy { it.tile.distanceTo(robot.tile) }
                val direction = robot.tile.directionTo(closestFactory.tile)

                if (robot.canTransferTo(closestFactory)) {
                    actionMap[robot] = listOf(robot.transfer(direction, Resource.ICE, robot.cargo.ice))
                } else {
                    if (robot.power >= robot.actionQueueCost() + robot.moveCost(robot.tile, direction)) {
                        actionMap[robot] = listOf(robot.move(direction))
                    }
                }
            }
        }

        return actionMap
    }
}
