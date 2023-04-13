package dev.jmerle.luxai2022.config

data class RobotConfigContainer<T>(val light: T, val heavy: T) {
    fun forType(type: RobotType): T {
        return if (type == RobotType.LIGHT) light else heavy
    }
}
