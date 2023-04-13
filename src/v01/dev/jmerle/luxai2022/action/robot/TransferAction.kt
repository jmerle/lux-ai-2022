package dev.jmerle.luxai2022.action.robot

import dev.jmerle.luxai2022.board.Direction
import dev.jmerle.luxai2022.board.Resource
import org.json.JSONArray

class TransferAction(repeat: Boolean, val direction: Direction, val resource: Resource, val amount: Int) :
    RobotAction(repeat) {
    override fun toJSON(): JSONArray {
        return JSONArray(intArrayOf(1, direction.ordinal, resource.ordinal, amount, if (repeat) 1 else 0))
    }
}
