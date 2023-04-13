package dev.jmerle.luxai2022.action.factory

import dev.jmerle.luxai2022.config.RobotType

class BuildRobotAction(val type: RobotType) : FactoryAction {
    override fun toJSON(): Int {
        return if (type == RobotType.LIGHT) 0 else 1
    }
}
