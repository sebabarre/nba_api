package fr.sba

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import fr.sba.data.*
import org.apache.logging.log4j.LogManager
import java.time.ZonedDateTime
import java.util.*
import kotlin.concurrent.fixedRateTimer

const val HEADER_API_KEY = "x-rapidapi-key"
const val HEADER_API_HOST = "x-rapidapi-host"

class RapidApi {

    private val mapper = jacksonObjectMapper()
    private val key = "8971ac3280msh73a623b488f4a49p153b68jsn250bd96f8428"
    private val host = "api-nba-v1.p.rapidapi.com"
    @Volatile private lateinit var  nbaTeams: List<Pair<String,String>>
    @Volatile private var nbaRanking: RuinardStandingTeam? = null
    @Volatile private var nbOfApiCalls = 0
    private val logger = LogManager.getLogger(this.javaClass)

    init {
        fixedRateTimer(
            name = "reset-nb-of-apicall-counter-and-nbaranking",
            startAt = Date(ZonedDateTime.now().plusDays(1).withHour(9).withMinute(50).withSecond(0).toInstant().epochSecond),
            period = EVERY_DAY
        ) {
            nbOfApiCalls = 0
            nbaRanking = null
        }
    }

    @Synchronized
    private fun incrementCounter() {
        nbOfApiCalls++
        logger.debug("Number of api calls today : $nbOfApiCalls")
    }

    private fun getNbaTeams(): List<Pair<String,String>> {
        if (::nbaTeams.isInitialized) {
            logger.debug("No need to call api, nbaTeams is already initialized")
            return nbaTeams
        }
        logger.debug("Calling api to get nba teams")
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("https://api-nba-v1.p.rapidapi.com/teams/league/standard")
            .get()
            .addHeader(HEADER_API_HOST, host)
            .addHeader(HEADER_API_KEY, key)
            .build()
        incrementCounter()
        val response = client.newCall(request).execute()
        val mapResponse =  mapper.readValue<TeamResponse>(response.body().string())
        nbaTeams = mapResponse.api.teams.filter { it.nbaFranchise == "1" }.map {
            it.teamId to it.fullName }
        return nbaTeams
    }

    private fun getStandings(conf: Conference): String {
        val client = OkHttpClient()
        logger.debug("calling api to get nba standings for conference ${conf.name}")

        val request = Request.Builder()
            .url("https://api-nba-v1.p.rapidapi.com/standings/standard/2019/conference/${conf.name.toLowerCase()}")
            .get()
            .addHeader(HEADER_API_HOST, host)
            .addHeader(HEADER_API_KEY, key)
            .build()
        incrementCounter()
        val response = client.newCall(request).execute()
        return response.body().string()
    }

    enum class Conference {
        WEST, EAST
    }

    @Synchronized
    fun getNbaRanking(): RuinardStandingTeam {
        if (nbaRanking != null) {
            logger.debug("No need to call api, nbaRanking is already initialized")
            return nbaRanking as RuinardStandingTeam
        }
        logger.debug("Calling getNbaRanking")

        nbaRanking = RuinardStandingTeam(east = getRankingByConf(Conference.EAST), west = getRankingByConf(Conference.WEST))
        return nbaRanking as RuinardStandingTeam
    }

    private fun getRankingByConf(conference: Conference): List<RuinartStandingByConference> {
        logger.debug("calling getRankingByConf $conference")

//        return listOf(RuinartStandingByConference(teamName = "BOSTON FUCKERS", ranking = 1, win = 81, losses = 1))
        return mapper.readValue<StandingResponse>(getStandings(conference)).api.standings.map { standing ->
            val teamName = getNbaTeams().find { team ->
                team.first == standing.teamId
            }?.second
            RuinartStandingByConference(teamName ?: "oups", Integer.valueOf(standing.conference.rank?.let {
                if (it.isNotEmpty()) it else "0"
            }), Integer.valueOf(standing.win), Integer.valueOf(standing.loss), standing.winPercentage)
        }.sortedByDescending { it.percentage }
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

    companion object {
        private const val EVERY_DAY: Long = 24 * 60 * 60 * 1000L
    }
}
