package dev.jmerle.luxai2022.team

import dev.jmerle.luxai2022.action.factory.BuildRobotAction
import dev.jmerle.luxai2022.action.factory.WaterAction
import dev.jmerle.luxai2022.board.Tile
import dev.jmerle.luxai2022.config.RobotType
import org.json.JSONObject
import kotlin.math.ceil

class Factory(entityId: String, teamId: Int, center: Tile, power: Double, cargo: Cargo, val strain: Int) :
    Entity(entityId, teamId, center, power, cargo) {
    val tiles = mutableListOf<Tile>().apply {
        for (dy in -1..1) {
            for (dx in -1..1) {
                add(center.add(dx, dy))
            }
        }
    }.toList()

    companion object {
        fun parse(raw: JSONObject): Factory {
            return Factory(
                raw.getString("unit_id"),
                raw.getInt("team_id"),
                Tile.parse(raw.getJSONArray("pos")),
                raw.getDouble("power"),
                Cargo.parse(raw.getJSONObject("cargo")),
                raw.getInt("strain_id")
            )
        }
    }

    fun canBuildRobot(type: RobotType): Boolean {
        if (obs.robots[tile] != null) {
            return false
        }

        val config = obs.config.robots.forType(type)

        val powerCost = config.powerCost * obs.weather.powerConsumption
        val metalCost = config.metalCost

        return power >= powerCost && cargo.metal >= metalCost
    }

    fun buildRobot(type: RobotType): BuildRobotAction {
        return BuildRobotAction(type)
    }

    fun waterCost(): Int {
        val ownedLichenTiles = obs.board.strains.count { it == strain }
        return ceil(ownedLichenTiles.toDouble() / obs.config.lichenWateringCostFactor).toInt() + 1
    }

    fun water(): WaterAction {
        return WaterAction()
    }
}
