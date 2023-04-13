package dev.jmerle.luxai2022.team

enum class Faction(val id: String) {
    NULL("Null"),
    ALPHA_STRIKE("AlphaStrike"),
    MOTHER_MARS("MotherMars"),
    THE_BUILDERS("TheBuilders"),
    FIRST_MARS("FirstMars");

    companion object {
        val values = values()

        fun fromId(id: String): Faction {
            return values.first { it.id == id }
        }
    }
}
