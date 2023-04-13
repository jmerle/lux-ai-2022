package dev.jmerle.luxai2022.team

import org.json.JSONObject

data class Cargo(val ice: Int, val ore: Int, val water: Int, val metal: Int) {
    companion object {
        fun parse(raw: JSONObject): Cargo {
            return Cargo(raw.getInt("ice"), raw.getInt("ore"), raw.getInt("water"), raw.getInt("metal"))
        }
    }
}
