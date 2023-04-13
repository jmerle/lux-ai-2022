package dev.jmerle.luxai2022.action.robot

import org.json.JSONArray

class SelfDestructAction : RobotAction(false) {
    override fun toJSON(): JSONArray {
        return JSONArray(intArrayOf(4, 0, 0, 0, 0))
    }
}
