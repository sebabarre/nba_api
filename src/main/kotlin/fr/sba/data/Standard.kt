package fr.sba.data

import com.fasterxml.jackson.annotation.JsonProperty

data class Standard(val confName: String, val divName: String)

data class Leagues(val standard: Standard)

data class Team(val city: String,
                val fullName: String,
                val teamId: String,
                val nickname: String,
                val logo: String,
                val url: String,
                val shortName: String,
                val allStar: String,
                val nbaFranchise: String,
                val leagues: Leagues)

data class TeamApi(val status: Int,
                   val message: String,
                   val results: Int,
                   val filters: List<String>,
                   val teams: List<Team>)

data class TeamResponse(val api: TeamApi)

data class Ranking(val win: String, val loss: String, val rank: String?, val name: String?, val GamesBehind: String?)

data class Standing(val league: String,
                    val teamId: String,
                    val win: String,
                    val loss: String,
                    val gamesBehind: String,
                    val lastTenWin: String,
                    val lastTenLoss: String,
                    val streak: String,
                    val seasonYear: String,
                    val conference: Ranking,
                    val division: Ranking,
                    val winPercentage: String,
                    val lossPercentage: String,
                    val home: Ranking,
                    val away: Ranking,
                    val winStreak: String,
                    val tieBreakerPoints: String?)

data class StandingApi(val status: Int,
                       val message: String,
                       val results: Int,
                       val filters: List<String>,
                       val standings: List<Standing>)

data class StandingResponse(val api: StandingApi)

data class RuinartStandingByConference(val teamName: String,
                            val ranking: Int,
                                       val win: Int,
                                       val losses: Int,
                                       val percentage: String
)
data class RuinardStandingTeam(val east: List<RuinartStandingByConference>,
                               val west: List<RuinartStandingByConference>)

data class Prono(val name: String,
                 @JsonProperty("East") val east: Map<String, String>,
                 @JsonProperty("West") val west: Map<String,String>) {

    var score = 0
    var nbOfCorrectInputs = 0
    var ultimateString = ""

    fun calculate(standings: RuinardStandingTeam){
        val eastCorrectMap = east.entries.associateBy({ it.value }) { Integer.valueOf(it.key) }
        standings.east.forEachIndexed { index, ruinartStandingByConference ->
            if ((eastCorrectMap[ruinartStandingByConference.teamName]) == ruinartStandingByConference.ranking) {
                score += 5
                nbOfCorrectInputs += 1
                ultimateString += "a"
            }
            else if (index <= 7 && eastCorrectMap.keys.contains(ruinartStandingByConference.teamName)) {
                score += 2
                ultimateString += "b"
            } else if (index <= 7) ultimateString += "b"
        }
        val westCorrectMap = west.entries.associateBy({ it.value }) { Integer.valueOf(it.key) }
        standings.west.forEachIndexed { index, ruinartStandingByConference ->
            if ((westCorrectMap[ruinartStandingByConference.teamName]) == ruinartStandingByConference.ranking) {
                score += 5
                nbOfCorrectInputs += 1
                val chars = ultimateString.toCharArray()
                chars[index] = when (chars[index]) {
                    'a' -> 'A'
                    else -> 'a'
                }
                ultimateString = String(chars)
            }
            else if (index <= 7 && westCorrectMap.keys.contains(ruinartStandingByConference.teamName)) {
                score += 2
            }
        }
    }

    fun simple(): SimpleRanking {
        return SimpleRanking(name, score, nbOfCorrectInputs, ultimateString)
    }
}

data class SimpleRanking(val name: String, val score: Int, val nbOfCorrectInputs: Int, val ultimateString: String)
