package fr.sba

import io.javalin.Javalin

fun main() {
    val api = Javalin.create().start(7777)
    val rapidApi = RapidApi()
    api.get("/healthcheck") {
        it.result("ok")
    }
   /* api.get("/teams"){
        it.result(rapidApi.getNbaTeams())
    }
    api.get("/standings") {
        it.result(rapidApi.getRankingByConf())
    }*/
}

