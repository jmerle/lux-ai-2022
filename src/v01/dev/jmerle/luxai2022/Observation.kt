package dev.jmerle.luxai2022

import dev.jmerle.luxai2022.board.Board
import dev.jmerle.luxai2022.board.Grid
import dev.jmerle.luxai2022.board.Tile
import dev.jmerle.luxai2022.config.Config
import dev.jmerle.luxai2022.config.Weather
import dev.jmerle.luxai2022.team.Faction
import dev.jmerle.luxai2022.team.Factory
import dev.jmerle.luxai2022.team.Robot
import dev.jmerle.luxai2022.team.Team
import org.json.JSONObject

data class Observation(
    val config: Config,

    val board: Board,
    val teams: List<Team>,

    val weatherSchedule: List<Weather>,

    val currentPlayerId: String,
    val remainingOverageTime: Double,

    val step: Int,
    val stepPostBid: Int
) {
    val isDay = stepPostBid % config.cycleLength < config.dayLength
    val weather = if (stepPostBid >= 0) weatherSchedule[stepPostBid] else Weather.NORMAL

    // These must be lazy because on step 0 there are no teams yet
    // Accessing me or opponent on step 0 will throw an error
    val me: Team by lazy { teams.first { it.playerId == currentPlayerId } }
    val opponent: Team by lazy { teams.first { it.playerId != currentPlayerId } }

    val factories = Grid<Factory>(config.mapSize).apply {
        for (team in teams) {
            for (factory in team.factories) {
                for (tile in factory.tiles) {
                    this[tile] = factory
                }
            }
        }
    }

    val robots = Grid<Robot>(config.mapSize).apply {
        for (team in teams) {
            for (robot in team.robots) {
                this[robot.tile] = robot
            }
        }
    }

    companion object {
        fun parse(raw: JSONObject, previous: Observation?, config: Config): Observation {
            val rawObs = raw.getJSONObject("obs")

            val rawBoard = rawObs.getJSONObject("board")
            val board = if (previous == null) {
                val rubble = Grid.parse<Int>(rawBoard.getJSONArray("rubble"))
                val ore = Grid.parse<Int>(rawBoard.getJSONArray("ore"))
                val ice = Grid.parse<Int>(rawBoard.getJSONArray("ice"))
                val lichen = Grid.parse<Int>(rawBoard.getJSONArray("lichen"))
                val strains = Grid.parse<Int>(rawBoard.getJSONArray("lichen_strains"))

                val spawns = mutableMapOf<String, List<Tile>>()
                val rawSpawns = rawBoard.getJSONObject("spawns")
                for (key in rawSpawns.keys()) {
                    val teamSpawns = mutableListOf<Tile>()

                    val tiles = rawSpawns.getJSONArray(key)
                    for (i in 0 until tiles.length()) {
                        teamSpawns.add(Tile.parse(tiles.getJSONArray(i)))
                    }

                    spawns[key] = teamSpawns
                }

                val factoriesPerTeam = rawBoard.getInt("factories_per_team")

                Board(rubble, ore, ice, lichen, strains, spawns, factoriesPerTeam)
            } else {
                val newBoard = previous.board.clone()

                for ((item, grid) in mapOf(
                    "rubble" to newBoard.rubble,
                    "lichen" to newBoard.lichen,
                    "lichen_strains" to newBoard.strains
                )) {
                    val updates = rawBoard.getJSONObject(item)
                    for (key in updates.keys()) {
                        val (x, y) = key.split(",").map { it.toInt() }
                        val value = updates.getInt(key)

                        grid[y, x] = value
                    }
                }

                newBoard
            }

            val teams = mutableListOf<Team>()

            val rawTeams = rawObs.getJSONObject("teams")
            val rawFactories = rawObs.getJSONObject("factories")
            val rawUnits = rawObs.getJSONObject("units")

            for (playerId in rawTeams.keys()) {
                val rawTeam = rawTeams.getJSONObject(playerId)

                val teamId = rawTeam.getInt("team_id")
                val faction = Faction.fromId(rawTeam.getString("faction"))
                val water = rawTeam.getInt("water")
                val metal = rawTeam.getInt("metal")
                val factoriesToPlace = rawTeam.getInt("factories_to_place")
                val factoryStrains = rawTeam.getJSONArray("factory_strains").map { it as Int }

                val factories = Grid<Factory>(config.mapSize)
                val rawFactoriesTeam = rawFactories.getJSONObject(playerId)
                for (entityId in rawFactoriesTeam.keys()) {
                    val factory = Factory.parse(rawFactoriesTeam.getJSONObject(entityId))
                    factories[factory.tile] = factory
                }

                val robots = Grid<Robot>(config.mapSize)
                val rawUnitsTeam = rawUnits.getJSONObject(playerId)
                for (entityId in rawUnitsTeam.keys()) {
                    val robot = Robot.parse(rawUnitsTeam.getJSONObject(entityId))
                    robots[robot.tile] = robot
                }

                val spawns = board.spawns[playerId]!!

                teams.add(
                    Team(
                        teamId,
                        playerId,
                        faction,
                        water,
                        metal,
                        factoriesToPlace,
                        factoryStrains,
                        factories,
                        robots,
                        spawns
                    )
                )
            }

            val weatherSchedule = previous?.weatherSchedule?.toList()
                ?: rawObs.getJSONArray("weather_schedule").map { Weather.values[it as Int] }

            val currentPlayerId = raw.getString("player")
            val remainingOverageTime = raw.getDouble("remainingOverageTime")

            val step = raw.getInt("step")
            val stepPostBid = rawObs.getInt("real_env_steps")

            val obs = Observation(
                config,
                board,
                teams,
                weatherSchedule,
                currentPlayerId,
                remainingOverageTime,
                step,
                stepPostBid
            )

            for (robot in obs.robots) {
                robot.setObservation(obs)
            }

            for (factory in obs.factories) {
                factory.setObservation(obs)
            }

            return obs
        }
    }
}
