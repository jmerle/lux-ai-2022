package dev.jmerle.luxai2022

import dev.jmerle.luxai2022.action.ActionMap
import dev.jmerle.luxai2022.action.setup.SetupAction
import dev.jmerle.luxai2022.action.setup.WaitAction
import dev.jmerle.luxai2022.agent.FactoryAgent
import dev.jmerle.luxai2022.agent.RobotAgent
import dev.jmerle.luxai2022.agent.SetupAgent

class Agent {
    private val setupAgent = SetupAgent()

    private val factoryAgents = mutableMapOf<String, FactoryAgent>()
    private val robotAgents = mutableMapOf<String, RobotAgent>()

    fun setup(obs: Observation): SetupAction {
        if (obs.step == 0) {
            return setupAgent.bid(obs)
        }

        if (obs.me.factoriesToPlace > 0) {
            return setupAgent.buildFactory(obs)
        }

        return WaitAction()
    }

    fun act(obs: Observation): ActionMap {
        val actionMap = ActionMap()

        for (key in factoryAgents.keys.toList()) {
            if (obs.me.factories.none { it.entityId == key }) {
                factoryAgents.remove(key)
            }
        }

        for (key in robotAgents.keys.toList()) {
            if (obs.me.robots.none { it.entityId == key }) {
                robotAgents.remove(key)
            }
        }

        for (factory in obs.me.factories) {
            val agent = factoryAgents.getOrPut(factory.entityId) { FactoryAgent() }
            val action = agent.act(obs, factory, actionMap)
            if (action != null) {
                actionMap[factory] = action
            }
        }

        for (robot in obs.me.robots) {
            val agent = robotAgents.getOrPut(robot.entityId) { RobotAgent() }
            val actions = agent.act(obs, robot, actionMap)
            if (actions != null) {
                actionMap[robot] = actions
            }
        }

        return actionMap
    }
}
