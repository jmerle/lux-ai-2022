package dev.jmerle.luxai2022.action.robot

import dev.jmerle.luxai2022.board.Direction
import org.json.JSONArray

class MoveAction(repeat: Boolean, val direction: Direction) : RobotAction(repeat) {
    override fun toJSON(): JSONArray {
        return JSONArray(intArrayOf(0, direction.ordinal, 1, 0, if (repeat) 1 else 0))
    }
}
