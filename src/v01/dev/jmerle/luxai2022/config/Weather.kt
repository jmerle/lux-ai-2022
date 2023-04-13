package dev.jmerle.luxai2022.config

enum class Weather(
    val rubble: RobotConfigContainer<Int> = RobotConfigContainer(1, 1),
    val powerConsumption: Double = 1.0,
    val powerGain: Double = 1.0
) {
    NORMAL,
    MARS_QUAKE(rubble = RobotConfigContainer(1, 10)),
    COLD_SNAP(powerConsumption = 2.0),
    DUST_STORM(powerGain = 0.5),
    SOLAR_FLARE(powerGain = 2.0);

    companion object {
        val values = values()
    }
}
