package dev.jmerle.luxai2022.action.robot

import org.json.JSONArray

class DigAction(repeat: Boolean) : RobotAction(repeat) {
    override fun toJSON(): JSONArray {
        return JSONArray(intArrayOf(3, 0, 0, 0, if (repeat) 1 else 0))
    }
}
