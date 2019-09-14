package fr.sba

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import fr.sba.data.Response


class RapidApi {

    val mapper = jacksonObjectMapper()

    fun getNbaTeams(): String {

        val client = OkHttpClient()

        val request = Request.Builder()
            .url("https://api-nba-v1.p.rapidapi.com/teams/league/standard")
            .get()
            .addHeader("x-rapidapi-host", "api-nba-v1.p.rapidapi.com")
            .addHeader("x-rapidapi-key", "8971ac3280msh73a623b488f4a49p153b68jsn250bd96f8428")
            .build()

        val response = client.newCall(request).execute()
        val result: Response = mapper.readValue(response.body().string())

        return mapper.writeValueAsString(result.api.teams.filter { it.nbaFranchise == "1" })

    }

}
