package fr.sba

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.javalin.Javalin
import org.apache.logging.log4j.LogManager

private val logger = LogManager.getLogger("Main")

fun main() {


    val api = Javalin.create { config ->
        config.enableCorsForAllOrigins()
        config.requestLogger { ctx, timeMs ->
            logger.debug("${ctx.attribute<String>("id")} : ${ctx.method()} ${ctx.path()} took $timeMs ms and respond ${ctx.status()}")
        }
    }.start(7777)
    val mapper = jacksonObjectMapper()
    val rapidApi = RapidApi()
    api.get("/healthcheck") {
        it.result("ok")
    }
    api.get("/standings") {
        logger.debug("calling /standings")
        it.result(mapper.writeValueAsString(rapidApi.getNbaRanking()))
    }
    api.get("/pronos") {
        logger.debug("calling /pronos")
        it.result(rapidApi.getRuinartRanking())
    }
}

