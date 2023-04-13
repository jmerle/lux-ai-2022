package dev.jmerle.luxai2022.action

import dev.jmerle.luxai2022.action.factory.FactoryAction
import dev.jmerle.luxai2022.action.robot.RobotAction
import dev.jmerle.luxai2022.team.Factory
import dev.jmerle.luxai2022.team.Robot
import org.json.JSONArray
import org.json.JSONObject

class ActionMap {
    private val robotActions = mutableMapOf<Robot, List<RobotAction>>()
    private val factoryActions = mutableMapOf<Factory, FactoryAction>()

    operator fun get(robot: Robot): List<RobotAction>? {
        return robotActions[robot]
    }

    operator fun get(factory: Factory): FactoryAction? {
        return factoryActions[factory]
    }

    operator fun set(robot: Robot, actions: List<RobotAction>) {
        robotActions[robot] = actions
    }

    operator fun set(factory: Factory, action: FactoryAction) {
        factoryActions[factory] = action
    }

    fun toJSON(): JSONObject {
        val obj = JSONObject()

        for ((robot, actions) in robotActions) {
            obj.put(robot.entityId, JSONArray(actions.map { it.toJSON() }))
        }

        for ((factory, action) in factoryActions) {
            obj.put(factory.entityId, action.toJSON())
        }

        return obj
    }
}
