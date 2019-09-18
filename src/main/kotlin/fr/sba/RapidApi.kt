package fr.sba

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import fr.sba.data.*

const val HEADER_API_KEY = "x-rapidapi-key"
const val HEADER_API_HOST = "x-rapidapi-host"

class RapidApi {

    private val mapper = jacksonObjectMapper()
    private val key = "8971ac3280msh73a623b488f4a49p153b68jsn250bd96f8428"
    private val host = "api-nba-v1.p.rapidapi.com"
    private val nbaTeams = getNbaTeams().api.teams.filter { it.nbaFranchise == "1" }.map { it.teamId to it.city }

    private fun getNbaTeams(): TeamResponse {

        val client = OkHttpClient()

        val request = Request.Builder()
            .url("https://api-nba-v1.p.rapidapi.com/teams/league/standard")
            .get()
            .addHeader(HEADER_API_HOST, host)
            .addHeader(HEADER_API_KEY, key)
            .build()

        val response = client.newCall(request).execute()
        return mapper.readValue(response.body().string())
    }

    private fun getStandings(conf: Conference): String {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("https://api-nba-v1.p.rapidapi.com/standings/standard/2018/conference/${conf.name.toLowerCase()}")
            .get()
            .addHeader(HEADER_API_HOST, host)
            .addHeader(HEADER_API_KEY, key)
            .build()

        val response = client.newCall(request).execute()
        return response.body().string()
    }

    enum class Conference {
        WEST, EAST
    }

    fun getNbaRanking(): RuinardStandingTeam {
        return RuinardStandingTeam(east = getRankingByConf(Conference.EAST), west = getRankingByConf(Conference.WEST))
    }

    private fun getRankingByConf(conference: Conference): List<RuinartStandingByConference> {
        return mapper.readValue<StandingResponse>(getStandings(conference)).api.standings.map {
            val teamName = nbaTeams.find { team ->
                team.first == it.teamId
            }?.second
            val ranking = it.conference.rank
            RuinartStandingByConference(teamName ?: "oups", Integer.valueOf(ranking))
        }.sortedBy { it.ranking }
    }

    fun getPronos(): List<Prono> = mapper.readValue(fileToString(javaClass.classLoader.getResourceAsStream("pronos.json")))

    fun getRuinartRanking() : String {
        val allRanking = getNbaRanking()
        val pronos = getPronos()
        pronos.forEach{ it.calculate(allRanking) }
        return pronos.sortedWith(
            compareBy(Prono::score, Prono::nbOfCorrectInputs, Prono::ultimateString)
        ).reversed().map { it.simple() }.toString()
    }
}
