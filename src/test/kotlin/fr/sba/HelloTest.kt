package fr.sba

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import fr.sba.data.*
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class HelloTest {

    private val mapper = jacksonObjectMapper()
    private val expectedStanding = StandingResponse(
        api= StandingApi(
            status=200,
            message="GET standings/standard/2020/conference/east",
            results=15,
            filters= listOf("conference", "division", "teamId"),
            standings= listOf(
                Standing(
                    league="standard",
                    teamId="20",
                    win="40",
                    loss="32",
                    gamesBehind="9.0",
                    lastTenWin="8",
                    lastTenLoss="2",
                    streak="1",
                    seasonYear="2020",
                    conference= Ranking(
                        win="24", loss="18", rank="6", name="east", GamesBehind=null
                    ),
                    division=Ranking(
                        win="6", loss="6", rank="2", name="southeast", GamesBehind="1.0"
                    ),
                    winPercentage=".556",
                    lossPercentage=".444",
                    home=Ranking(
                        win="21", loss="15", rank=null, name=null, GamesBehind=null
                    ),
                    away=Ranking(win="19", loss="17", rank=null, name=null, GamesBehind=null
                    ),
                    winStreak="1", tieBreakerPoints=""
                )
            )
        )
    )

    private val expectedTeams = TeamResponse(
        api = TeamApi(
            status=200,
            message="GET teams/league/standard",
            results=57,
            filters=listOf("teamId", "league", "city", "shortName", "nickName", "confName", "divName"),
            teams= listOf(
                Team(
                    city="Atlanta",
                    fullName="Atlanta Hawks",
                    teamId="1",
                    nickname="Hawks",
                    logo="https://upload.wikimedia.org/wikipedia/fr/e/ee/Hawks_2016.png",
                    url="1182",
                    shortName="ATL",
                    allStar="0",
                    nbaFranchise="1",
                    leagues=Leagues(
                        standard=Standard(
                            confName="East", divName="Southeast"
                        )
                    )
                )
            )
        )
    )

    @Test
    fun testStandingMapping() {
        val pronos = fileToString(javaClass.classLoader.getResourceAsStream("nba_standing.json"))
        assertNotNull(pronos)
        val response: StandingResponse = mapper.readValue(pronos)
        assertNotNull(response)
        assertEquals(response, expectedStanding)
    }

    @Test
    fun testTeamsMapping() {
        val teams = fileToString(javaClass.classLoader.getResourceAsStream("teams.json"))
        assertNotNull(teams)
        val response: TeamResponse = mapper.readValue(teams)
        assertNotNull(response)
        assertEquals(response, expectedTeams)
    }
}
