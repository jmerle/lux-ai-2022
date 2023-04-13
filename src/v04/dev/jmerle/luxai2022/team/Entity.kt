package dev.jmerle.luxai2022.team

import dev.jmerle.luxai2022.Observation
import dev.jmerle.luxai2022.board.Tile

// The engine calls robots "units" and factories "factories", and robots and factories are both "units" as well
// Here we call robots "robots" and factories "factories", and robots and factories are both "entities"
// This is more consistent, and using the "units" name is a bit inconvenient because Unit is Kotlin's void type
open class Entity(
    val entityId: String,
    val teamId: Int,

    val tile: Tile,

    val power: Double,
    val cargo: Cargo
) {
    // The observation is always non-null by the time we need to access it
    // This way we don't need to add non-null assertions everywhere
    // We cannot pass observation in the constructor because that would create a cyclic dependency between constructors
    private var _obs: Observation? = null
    val obs: Observation
        get() = _obs!!

    fun setObservation(obs: Observation) {
        _obs = obs
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Entity) return false

        if (entityId != other.entityId) return false

        return true
    }

    override fun hashCode(): Int {
        return entityId.hashCode()
    }
}
