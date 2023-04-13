package dev.jmerle.luxai2022.team

import dev.jmerle.luxai2022.board.Grid
import dev.jmerle.luxai2022.board.Tile

data class Team(
    val teamId: Int,
    val playerId: String,

    val faction: Faction,

    val water: Int,
    val metal: Int,

    val factoriesToPlace: Int,
    val factoryStrains: List<Int>,

    val factories: Grid<Factory>,
    val robots: Grid<Robot>,

    val spawns: List<Tile>
)
