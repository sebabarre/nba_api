package fr.sba.data

data class Standard(val confName: String, val divName: String)

data class Leagues(val standard: Standard)

data class Team(val city: String,
                val fullName: String,
                val teamId: String,
                val nickname: String,
                val logo: String,
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
                    val tieBreakerPoints: String)

data class StandingApi(val status: Int,
                       val message: String,
                       val results: Int,
                       val filters: List<String>,
                       val standings: List<Standing>)

data class StandingResponse(val api: StandingApi) {
    fun toRuinartStanding(teams: Map<Int, String>): List<TeamStanding> {
        api.standings.forEach {team ->
            println(TeamStanding(teams[team.teamId]!!, )
        }
    }
}