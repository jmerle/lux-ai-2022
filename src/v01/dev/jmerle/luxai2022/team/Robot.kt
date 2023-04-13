package dev.jmerle.luxai2022.team

import dev.jmerle.luxai2022.action.robot.DigAction
import dev.jmerle.luxai2022.action.robot.MoveAction
import dev.jmerle.luxai2022.action.robot.PickupAction
import dev.jmerle.luxai2022.action.robot.RechargeAction
import dev.jmerle.luxai2022.action.robot.RobotAction
import dev.jmerle.luxai2022.action.robot.SelfDestructAction
import dev.jmerle.luxai2022.action.robot.TransferAction
import dev.jmerle.luxai2022.board.Direction
import dev.jmerle.luxai2022.board.Resource
import dev.jmerle.luxai2022.board.Tile
import dev.jmerle.luxai2022.config.RobotType
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.ceil

class Robot(
    entityId: String,
    teamId: Int,
    tile: Tile,
    power: Double,
    cargo: Cargo,
    val type: RobotType,
    val actionQueue: List<RobotAction>
) :
    Entity(entityId, teamId, tile, power, cargo) {
    val config get() = obs.config.robots.forType(type)

    companion object {
        fun parse(raw: JSONObject): Robot {
            return Robot(
                raw.getString("unit_id"),
                raw.getInt("team_id"),
                Tile.parse(raw.getJSONArray("pos")),
                raw.getDouble("power"),
                Cargo.parse(raw.getJSONObject("cargo")),
                RobotType.valueOf(raw.getString("unit_type")),
                raw.getJSONArray("action_queue").map { RobotAction.parse(it as JSONArray) }
            )
        }
    }

    fun actionQueueCost(): Double {
        return obs.config.unitActionQueuePowerCost.forType(type) * obs.weather.powerConsumption
    }

    fun moveCost(from: Tile, direction: Direction): Double {
        if (direction == Direction.CENTER) {
            return 0.0
        }

        val newTile = from.add(direction)
        if (!newTile.isOnMap(obs)) {
            throw IllegalArgumentException("Move goes outside the board")
        }

        val targetRubble = obs.board.rubble[newTile]!!
        return ceil(config.moveCost + config.rubbleMovementCost * targetRubble) * obs.weather.powerConsumption
    }

    fun digCost(): Double {
        return config.digCost * obs.weather.powerConsumption
    }

    fun selfDestructCost(): Double {
        return config.selfDestructCost * obs.weather.powerConsumption
    }

    fun canTransferTo(factory: Factory): Boolean {
        return tile.distanceTo(factory.tile) <= 3
    }

    fun move(direction: Direction, repeat: Boolean = false): MoveAction {
        return MoveAction(repeat, direction)
    }

    fun transfer(direction: Direction, resource: Resource, amount: Int, repeat: Boolean = false): TransferAction {
        return TransferAction(repeat, direction, resource, amount)
    }

    fun pickup(resource: Resource, amount: Int, repeat: Boolean = false): PickupAction {
        return PickupAction(repeat, resource, amount)
    }

    fun dig(repeat: Boolean = false): DigAction {
        return DigAction(repeat)
    }

    fun selfDestruct(): SelfDestructAction {
        return SelfDestructAction()
    }

    fun recharge(targetPower: Double, repeat: Boolean = false): RechargeAction {
        return RechargeAction(repeat, targetPower)
    }
}
