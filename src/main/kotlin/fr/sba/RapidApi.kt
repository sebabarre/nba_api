package fr.sba

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import fr.sba.data.*
import org.apache.logging.log4j.LogManager
import org.slf4j.LoggerFactory
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
    @Volatile private lateinit var nbaRanking: RuinardStandingTeam
    @Volatile private var nbOfApiCalls = 0
    private val logger = LogManager.getLogger(this.javaClass)
    private val EVERY_DAY: Long = 24 * 60 * 60 * 1000L

    init {
        fixedRateTimer(
            name = "reset-nb-of-apicall-counter",
            startAt = Date(ZonedDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0).toInstant().epochSecond),
            period = EVERY_DAY
        ) {
            nbOfApiCalls = 0
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
        nbaTeams = mapResponse.api.teams.filter { it.nbaFranchise == "1" }.map { it.teamId to it.city }
        return nbaTeams
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
        incrementCounter()
        val response = client.newCall(request).execute()
        return response.body().string()
    }

    enum class Conference {
        WEST, EAST
    }

    @Synchronized
    fun getNbaRanking(): RuinardStandingTeam {
        if (::nbaRanking.isInitialized) {
            logger.debug("No need to call api, nbaRanking is already initialized")
            return nbaRanking
        }
        logger.debug("Calling getNbaRanking")

        nbaRanking = RuinardStandingTeam(east = getRankingByConf(Conference.EAST), west = getRankingByConf(Conference.WEST))
        return nbaRanking
    }

    private fun getRankingByConf(conference: Conference): List<RuinartStandingByConference> {
        logger.debug("calling getRankingByConf $conference")

//        return listOf(RuinartStandingByConference(teamName = "BOSTON FUCKERS", ranking = 1, win = 81, losses = 1))
        return mapper.readValue<StandingResponse>(getStandings(conference)).api.standings.map {
            val teamName = getNbaTeams().find { team ->
                team.first == it.teamId
            }?.second
            RuinartStandingByConference(teamName ?: "oups", Integer.valueOf(it.conference.rank), Integer.valueOf(it.win), Integer.valueOf(it.loss))
        }.sortedBy { it.ranking }
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
