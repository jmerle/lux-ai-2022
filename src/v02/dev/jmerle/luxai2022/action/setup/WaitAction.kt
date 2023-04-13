package dev.jmerle.luxai2022.action.setup

import org.json.JSONObject

class WaitAction : SetupAction {
    override fun toJSON(): JSONObject {
        return JSONObject()
    }
}
