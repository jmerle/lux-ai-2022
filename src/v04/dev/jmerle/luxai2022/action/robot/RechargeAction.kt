package dev.jmerle.luxai2022.action.robot

import org.json.JSONArray

class RechargeAction(repeat: Boolean, val targetPower: Double) : RobotAction(repeat) {
    override fun toJSON(): JSONArray {
        return JSONArray(arrayOf(5, 0, 0, targetPower, if (repeat) 1 else 0))
    }
}
