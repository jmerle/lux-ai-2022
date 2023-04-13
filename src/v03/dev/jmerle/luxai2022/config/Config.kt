package dev.jmerle.luxai2022.config

import org.json.JSONObject

data class Config(
    val maxEpisodeLength: Int,
    val mapSize: Int,
    val verbose: Verbosity,

    val validateActionSpace: Boolean,

    val maxTransferAmount: Int,
    val minFactories: Int,
    val maxFactories: Int,
    val cycleLength: Int,
    val dayLength: Int,
    val unitActionQueueSize: Int,
    val unitActionQueuePowerCost: RobotConfigContainer<Double>,

    val maxRubble: Int,
    val factoryRubbleAfterDestruction: Int,
    val initWaterMetalPerFactory: Int,
    val initPowerPerFactory: Double,

    val minLichenToSpread: Int,
    val lichenLostWithoutWater: Int,
    val lichenGainedWithWater: Int,
    val maxLichenPerTile: Int,
    val lichenWateringCostFactor: Int,

    val biddingSystem: Boolean,

    val factoryProcessingRateWater: Int,
    val iceWaterRatio: Int,
    val factoryProcessingRateMetal: Int,
    val oreMetalRatio: Int,
    val factoryCharge: Double,
    val factoryWaterConsumption: Int,

    val robots: RobotConfigContainer<RobotConfig>
) {
    companion object {
        fun parse(raw: JSONObject): Config {
            val rawUnitActionQueuePowerCost = raw.getJSONObject("UNIT_ACTION_QUEUE_POWER_COST")
            val rawRobots = raw.getJSONObject("ROBOTS")

            return Config(
                raw.getInt("max_episode_length"),
                raw.getInt("map_size"),
                Verbosity.values[raw.getInt("verbose")],

                raw.getBoolean("validate_action_space"),

                raw.getInt("max_transfer_amount"),
                raw.getInt("MIN_FACTORIES"),
                raw.getInt("MAX_FACTORIES"),
                raw.getInt("CYCLE_LENGTH"),
                raw.getInt("DAY_LENGTH"),
                raw.getInt("UNIT_ACTION_QUEUE_SIZE"),
                RobotConfigContainer(
                    rawUnitActionQueuePowerCost.getDouble("LIGHT"),
                    rawUnitActionQueuePowerCost.getDouble("HEAVY")
                ),

                raw.getInt("MAX_RUBBLE"),
                raw.getInt("FACTORY_RUBBLE_AFTER_DESTRUCTION"),
                raw.getInt("INIT_WATER_METAL_PER_FACTORY"),
                raw.getDouble("INIT_POWER_PER_FACTORY"),

                raw.getInt("MIN_LICHEN_TO_SPREAD"),
                raw.getInt("LICHEN_LOST_WITHOUT_WATER"),
                raw.getInt("LICHEN_GAINED_WITH_WATER"),
                raw.getInt("MAX_LICHEN_PER_TILE"),
                raw.getInt("LICHEN_WATERING_COST_FACTOR"),

                raw.getBoolean("BIDDING_SYSTEM"),

                raw.getInt("FACTORY_PROCESSING_RATE_WATER"),
                raw.getInt("ICE_WATER_RATIO"),
                raw.getInt("FACTORY_PROCESSING_RATE_METAL"),
                raw.getInt("ORE_METAL_RATIO"),
                raw.getDouble("FACTORY_CHARGE"),
                raw.getInt("FACTORY_WATER_CONSUMPTION"),

                RobotConfigContainer(
                    RobotConfig.parse(rawRobots.getJSONObject("LIGHT")),
                    RobotConfig.parse(rawRobots.getJSONObject("HEAVY"))
                )
            )
        }
    }
}
