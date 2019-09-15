package fr.sba

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import fr.sba.data.StandingResponse
import fr.sba.data.TeamResponse

const val HEADER_API_KEY = "x-rapidapi-key"
const val HEADER_API_HOST = "x-rapidapi-host"

class RapidApi {

    private val mapper = jacksonObjectMapper()
    private val key = "8971ac3280msh73a623b488f4a49p153b68jsn250bd96f8428"
    private val host = "api-nba-v1.p.rapidapi.com"

    fun getNbaTeams(): TeamResponse {

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

    fun getStandings(conf: Conference): StandingResponse {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("https://api-nba-v1.p.rapidapi.com/standings/standard/2018/conference/${conf.name.toLowerCase()}")
            .get()
            .addHeader(HEADER_API_HOST, host)
            .addHeader(HEADER_API_KEY, key)
            .build()

        val response = client.newCall(request).execute()
        return mapper.readValue(response.body().string())
    }

    enum class Conference {
        WEST, EAST
    }

    fun getRankingByConf() {
        val eastRanking = getStandings(Conference.EAST)
        val westRanking = getStandings(Conference.WEST)
        val teams = getNbaTeams().api.teams.map { it.teamId to it.nickname }
        val eastStanding = eastRanking.toRuinartStanding(teams)

    }
}
