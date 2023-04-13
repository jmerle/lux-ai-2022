package dev.jmerle.luxai2022.board

enum class Direction(val dx: Int, val dy: Int) {
    CENTER(0, 0),
    UP(0, -1),
    RIGHT(1, 0),
    DOWN(0, 1),
    LEFT(-1, 0);

    companion object {
        val values = values()
        val neighbors = values.filter { it != CENTER }
    }
}
