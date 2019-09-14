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

data class Api(val status: Int,
               val message: String,
               val results: Int,
               val filters: List<String>,
               val teams: List<Team>)

data class Response(val api: Api)