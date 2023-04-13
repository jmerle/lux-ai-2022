package dev.jmerle.luxai2022.board

data class Board(
    val rubble: Grid<Int>,
    val ore: Grid<Boolean>,
    val ice: Grid<Boolean>,
    val lichen: Grid<Int>,
    val strains: Grid<Int>,
    val spawns: Map<String, List<Tile>>,
    val factoriesPerTeam: Int
) {
    fun clone(): Board {
        val newSpawns = spawns.mapValues { it.value.map { tile -> tile.copy() }.toList() }

        return Board(
            rubble.clone(),
            ore.clone(),
            ice.clone(),
            lichen.clone(),
            strains.clone(),
            newSpawns,
            factoriesPerTeam
        )
    }
}
