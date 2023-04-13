package dev.jmerle.luxai2022.action.setup

import dev.jmerle.luxai2022.team.Faction
import org.json.JSONObject

class BidAction(val bid: Int, val faction: Faction) : SetupAction {
    override fun toJSON(): JSONObject {
        return JSONObject(mapOf("bid" to bid, "faction" to faction.id))
    }
}
