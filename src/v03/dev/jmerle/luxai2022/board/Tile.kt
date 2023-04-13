package dev.jmerle.luxai2022.board

import dev.jmerle.luxai2022.Observation
import org.json.JSONArray
import kotlin.math.abs

data class Tile(val x: Int, val y: Int) {
    companion object {
        fun parse(raw: JSONArray): Tile {
            return Tile(raw.getInt(0), raw.getInt(1))
        }
    }

    fun add(dx: Int, dy: Int): Tile {
        return Tile(x + dx, y + dy)
    }

    fun add(direction: Direction): Tile {
        return add(direction.dx, direction.dy)
    }

    fun isOnMap(obs: Observation): Boolean {
        return x >= 0 && x < obs.config.mapSize && y >= 0 && y < obs.config.mapSize
    }

    fun distanceTo(tile: Tile): Int {
        return abs(tile.x - x) + abs(tile.y - y)
    }

    fun directionTo(tile: Tile): Direction {
        return when {
            tile.x < x -> Direction.LEFT
            tile.x > x -> Direction.RIGHT
            tile.y < y -> Direction.UP
            tile.y > y -> Direction.DOWN
            else -> Direction.CENTER
        }
    }

    fun toJSON(): JSONArray {
        return JSONArray(intArrayOf(x, y))
    }
}
