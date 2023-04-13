package dev.jmerle.luxai2022

import dev.jmerle.luxai2022.config.Config
import org.json.JSONObject

fun main() {
    val agent = Agent()

    var config: Config? = null
    var previousObs: Observation? = null

    while (true) {
        val data = readLine() ?: break
        val obj = JSONObject(data)

        if (config == null) {
            config = Config.parse(obj.getJSONObject("info").getJSONObject("env_cfg"))
        }

        val obs = Observation.parse(obj, previousObs, config)
        previousObs = obs

        val actions = if (obs.step <= obs.board.factoriesPerTeam + 1) {
            agent.setup(obs).toJSON()
        } else {
            agent.act(obs).toJSON()
        }

        println(actions.toString())
    }
}
