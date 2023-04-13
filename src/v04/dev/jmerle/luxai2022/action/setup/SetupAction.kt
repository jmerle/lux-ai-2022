package dev.jmerle.luxai2022.action.setup

import org.json.JSONObject

interface SetupAction {
    fun toJSON(): JSONObject
}
