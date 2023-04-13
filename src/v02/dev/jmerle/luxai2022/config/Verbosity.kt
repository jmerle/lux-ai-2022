package dev.jmerle.luxai2022.config

enum class Verbosity {
    SILENT,
    ERRORS,
    WARNINGS,
    INFO;

    companion object {
        val values = values()
    }
}
