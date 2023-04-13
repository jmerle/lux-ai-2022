package dev.jmerle.luxai2022.config

import org.json.JSONObject

data class RobotConfig(
    val metalCost: Int,
    val powerCost: Int,
    val cargoSpace: Int,
    val batteryCapacity: Double,
    val charge: Double,
    val initPower: Double,
    val moveCost: Double,
    val rubbleMovementCost: Double,
    val digCost: Double,
    val digRubbleRemoved: Int,
    val digResourceGain: Int,
    val digLichenRemoved: Int,
    val selfDestructCost: Double,
    val rubbleAfterDestruction: Int
) {
    companion object {
        fun parse(raw: JSONObject): RobotConfig {
            return RobotConfig(
                raw.getInt("METAL_COST"),
                raw.getInt("POWER_COST"),
                raw.getInt("CARGO_SPACE"),
                raw.getDouble("BATTERY_CAPACITY"),
                raw.getDouble("CHARGE"),
                raw.getDouble("INIT_POWER"),
                raw.getDouble("MOVE_COST"),
                raw.getDouble("RUBBLE_MOVEMENT_COST"),
                raw.getDouble("DIG_COST"),
                raw.getInt("DIG_RUBBLE_REMOVED"),
                raw.getInt("DIG_RESOURCE_GAIN"),
                raw.getInt("DIG_LICHEN_REMOVED"),
                raw.getDouble("SELF_DESTRUCT_COST"),
                raw.getInt("RUBBLE_AFTER_DESTRUCTION")
            )
        }
    }
}
