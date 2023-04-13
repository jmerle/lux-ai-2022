package dev.jmerle.luxai2022.action.robot

import dev.jmerle.luxai2022.board.Resource
import org.json.JSONArray

class PickupAction(repeat: Boolean, val resource: Resource, val amount: Int) : RobotAction(repeat) {
    override fun toJSON(): JSONArray {
        return JSONArray(intArrayOf(2, 0, resource.ordinal, amount, if (repeat) 1 else 0))
    }
}
