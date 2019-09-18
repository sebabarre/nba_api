package fr.sba

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.javalin.Javalin

fun main() {
    val api = Javalin.create().start(7777)
    val mapper = jacksonObjectMapper()
    val rapidApi = RapidApi()
    api.get("/healthcheck") {
        it.result("ok")
    }
    api.get("/standings") {
        it.result(mapper.writeValueAsString(rapidApi.getNbaRanking()))
    }
    api.get("/pronos") { it.result(rapidApi.getRuinartRanking())}
}

