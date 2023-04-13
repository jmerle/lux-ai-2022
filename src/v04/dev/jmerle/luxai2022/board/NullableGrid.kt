package dev.jmerle.luxai2022.board

class NullableGrid<T>(val size: Int) : Iterable<T> {
    private var tiles = MutableList<T?>(size * size) { null }

    operator fun get(y: Int, x: Int): T? {
        return tiles[y * size + x]
    }

    operator fun get(tile: Tile): T? {
        return this[tile.y, tile.x]
    }

    operator fun set(y: Int, x: Int, value: T?) {
        tiles[y * size + x] = value
    }

    operator fun set(tile: Tile, value: T?) {
        this[tile.y, tile.x] = value
    }

    fun clone(): NullableGrid<T> {
        val newGrid = NullableGrid<T>(size)
        newGrid.tiles = tiles.toMutableList()
        return newGrid
    }

    fun tiles(): List<TileWithValue<T>> {
        return tiles.mapIndexed { index, value ->
            if (value == null) {
                return@mapIndexed null
            }

            val y = index / size
            val x = index - y * size

            TileWithValue<T>(Tile(x, y), value)
        }.filterNotNull()
    }

    override fun iterator(): Iterator<T> {
        return tiles.filterNotNull().iterator()
    }
}
