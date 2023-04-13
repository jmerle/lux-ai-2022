package dev.jmerle.luxai2022.board

enum class Resource {
    ICE,
    ORE,
    WATER,
    METAL,
    POWER;

    companion object {
        val values = values()
    }
}
