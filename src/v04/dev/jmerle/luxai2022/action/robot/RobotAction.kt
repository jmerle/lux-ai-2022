package dev.jmerle.luxai2022.action.robot

import dev.jmerle.luxai2022.board.Direction
import dev.jmerle.luxai2022.board.Resource
import org.json.JSONArray

abstract class RobotAction(val repeat: Boolean) {
    companion object {
        fun parse(raw: JSONArray): RobotAction {
            return when (raw.getInt(0)) {
                0 -> MoveAction(raw.getInt(4) == 1, Direction.values[raw.getInt(1)])

                1 -> TransferAction(
                    raw.getInt(4) == 1,
                    Direction.values[raw.getInt(1)],
                    Resource.values[raw.getInt(2)],
                    raw.getInt(3)
                )

                2 -> PickupAction(raw.getInt(4) == 1, Resource.values[raw.getInt(2)], raw.getInt(3))
                3 -> DigAction(raw.getInt(4) == 1)
                4 -> SelfDestructAction()
                5 -> RechargeAction(raw.getInt(4) == 1, raw.getDouble(3))

                else -> throw IllegalArgumentException("Invalid action: $raw")
            }
        }
    }

    abstract fun toJSON(): JSONArray
}
