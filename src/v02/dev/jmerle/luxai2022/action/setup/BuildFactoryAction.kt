package dev.jmerle.luxai2022.action.setup

import dev.jmerle.luxai2022.board.Tile
import org.json.JSONObject

class BuildFactoryAction(val spawn: Tile, val water: Int, val metal: Int) : SetupAction {
    override fun toJSON(): JSONObject {
        return JSONObject(mapOf("spawn" to spawn.toJSON(), "water" to water, "metal" to metal))
    }
}
