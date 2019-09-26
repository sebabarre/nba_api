package fr.sba

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import fr.sba.data.*
import org.apache.logging.log4j.LogManager
import org.slf4j.LoggerFactory

const val HEADER_API_KEY = "x-rapidapi-key"
const val HEADER_API_HOST = "x-rapidapi-host"

class RapidApi {

    private val mapper = jacksonObjectMapper()
    private val key = "8971ac3280msh73a623b488f4a49p153b68jsn250bd96f8428"
    private val host = "api-nba-v1.p.rapidapi.com"
    private lateinit var  nbaTeams: List<Pair<String,String>>
    private val logger = LogManager.getLogger(this.javaClass)

    private fun getNbaTeams(): List<Pair<String,String>> {
        if (::nbaTeams.isInitialized) return nbaTeams
        logger.debug("calling api to get nba teams")
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("https://api-nba-v1.p.rapidapi.com/teams/league/standard")
            .get()
            .addHeader(HEADER_API_HOST, host)
            .addHeader(HEADER_API_KEY, key)
            .build()

        val response = client.newCall(request).execute()
        val mapResponse =  mapper.readValue<TeamResponse>(response.body().string())
        return mapResponse.api.teams.filter { it.nbaFranchise == "1" }.map { it.teamId to it.city }
    }

    private fun getStandings(conf: Conference): String {
        val client = OkHttpClient()
        logger.debug("calling api to get nba standings for conference ${conf.name}")

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
        return listOf(RuinartStandingByConference(teamName = "BOSTON FUCKERS", ranking = 1, win = 81, losses = 1))
        /*return mapper.readValue<StandingResponse>(getStandings(conference)).api.standings.map {
            val teamName = getNbaTeams().find { team ->
                team.first == it.teamId
            }?.second
            RuinartStandingByConference(teamName ?: "oups", Integer.valueOf(it.conference.rank), Integer.valueOf(it.win), Integer.valueOf(it.loss))
        }.sortedBy { it.ranking }*/
    }

    private fun getPronos(): List<Prono> = mapper.readValue(fileToString(javaClass.classLoader.getResourceAsStream("pronos.json")))

    fun getRuinartRanking() : String {
        val allRanking = getNbaRanking()
        val pronos = getPronos()
        pronos.forEach{ it.calculate(allRanking) }
        return mapper.writeValueAsString(pronos.sortedWith(
            compareBy(Prono::score, Prono::nbOfCorrectInputs).thenByDescending { it.ultimateString }
        ).reversed())
    }
}
