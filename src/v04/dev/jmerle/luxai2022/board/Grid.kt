package dev.jmerle.luxai2022.board

import org.json.JSONArray

class Grid<T>(val size: Int, private val init: (tile: Tile) -> T) : Iterable<T> {
    private var tiles = MutableList(size * size) { index ->
        val y = index / size
        val x = index - y * size

        init(Tile(x, y))
    }

    companion object {
        fun parseIntGrid(raw: JSONArray): Grid<Int> {
            val grid = Grid(raw.length()) { 0 }

            for (y in 0 until grid.size) {
                val row = raw.getJSONArray(y)

                for (x in 0 until grid.size) {
                    grid[y, x] = row.getInt(x)
                }
            }

            return grid
        }
    }

    operator fun get(y: Int, x: Int): T {
        return tiles[y * size + x]
    }

    operator fun get(tile: Tile): T {
        return this[tile.y, tile.x]
    }

    operator fun set(y: Int, x: Int, value: T) {
        tiles[y * size + x] = value
    }

    operator fun set(tile: Tile, value: T) {
        this[tile.y, tile.x] = value
    }

    fun clone(): Grid<T> {
        val newGrid = Grid<T>(size, init)
        newGrid.tiles = tiles.toMutableList()
        return newGrid
    }

    fun tiles(): List<TileWithValue<T>> {
        return tiles.mapIndexed { index, value ->
            val y = index / size
            val x = index - y * size

            TileWithValue(Tile(x, y), value)
        }
    }

    override fun iterator(): Iterator<T> {
        return tiles.iterator()
    }
}
