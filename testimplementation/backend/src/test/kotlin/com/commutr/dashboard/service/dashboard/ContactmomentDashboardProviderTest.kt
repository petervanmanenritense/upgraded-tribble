package com.commutr.dashboard.service.dashboard

import com.commutr.dashboard.model.*
import com.commutr.dashboard.repository.ContactmomentRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import java.time.LocalDate

@DataJpaTest
@Import(ContactmomentDashboardProvider::class)
class ContactmomentDashboardProviderTest {

    @Autowired
    lateinit var provider: ContactmomentDashboardProvider

    @Autowired
    lateinit var em: TestEntityManager

    private lateinit var team1: Team
    private lateinit var team2: Team
    private lateinit var coach1: Coach
    private lateinit var coach2: Coach
    private lateinit var inwoner1: Inwoner

    @BeforeEach
    fun setup() {
        team1 = em.persist(Team(name = "Team Volwassenen"))
        team2 = em.persist(Team(name = "Team Inburgering"))
        coach1 = em.persist(Coach(fullName = "Peter de Vries", team = team1))
        coach2 = em.persist(Coach(fullName = "Ahmed El Amrani", team = team2))
        inwoner1 = em.persist(Inwoner(fullName = "Jan Jansen", birthdate = LocalDate.of(1990, 1, 15), administratienummer = "ADM001"))
        em.flush()
    }

    private fun createContactmoment(
        coach: Coach = coach1,
        team: Team = team1,
        date: LocalDate = LocalDate.of(2025, 3, 15),
        kanaal: String = "Fysiek",
        onderwerp: String = "Voortgangsgesprek"
    ): Contactmoment = em.persist(Contactmoment(
        inwoner = inwoner1, coach = coach, team = team,
        date = date, kanaal = kanaal, onderwerp = onderwerp
    ))

    @Test
    fun `id and label are correct`() {
        assertEquals("contactmomenten", provider.id)
        assertEquals("Contactmomenten", provider.label)
    }

    @Nested
    inner class GetConfig {
        @Test
        fun `returns correct filter keys`() {
            val config = provider.getConfig()
            val filterKeys = config.filters.map { it.key }
            assertEquals(listOf("year", "team", "coach", "kanaal", "onderwerp"), filterKeys)
        }

        @Test
        fun `chart type is bar with green color`() {
            val config = provider.getConfig()
            assertEquals("bar", config.chart.type)
            assertEquals(listOf("#198038"), config.chart.colors)
        }
    }

    @Nested
    inner class GetSummary {
        @Test
        fun `counts all records`() {
            createContactmoment()
            createContactmoment(date = LocalDate.of(2025, 3, 20))
            em.flush()

            val summary = provider.getSummary(emptyMap())
            assertEquals(2, summary.total)
        }

        @Test
        fun `filters by kanaal`() {
            createContactmoment(kanaal = "Fysiek")
            createContactmoment(kanaal = "Videobellen")
            createContactmoment(kanaal = "Fysiek")
            em.flush()

            val summary = provider.getSummary(mapOf("kanaal" to "Fysiek"))
            assertEquals(2, summary.total)
        }

        @Test
        fun `filters by onderwerp`() {
            createContactmoment(onderwerp = "Voortgangsgesprek")
            createContactmoment(onderwerp = "Brede Intake")
            em.flush()

            val summary = provider.getSummary(mapOf("onderwerp" to "Brede Intake"))
            assertEquals(1, summary.total)
        }

        @Test
        fun `filters by year using date field`() {
            createContactmoment(date = LocalDate.of(2024, 6, 10))
            createContactmoment(date = LocalDate.of(2025, 3, 15))
            em.flush()

            val summary = provider.getSummary(mapOf("year" to "2024"))
            assertEquals(1, summary.total)
        }
    }

    @Nested
    inner class GetChartData {
        @Test
        fun `groups by month correctly`() {
            createContactmoment(date = LocalDate.of(2025, 1, 5))
            createContactmoment(date = LocalDate.of(2025, 1, 20))
            createContactmoment(date = LocalDate.of(2025, 3, 10))
            em.flush()

            val chart = provider.getChartData(emptyMap())
            assertEquals(listOf("Jan 2025", "Feb 2025", "Mrt 2025"), chart.labels)
            assertEquals(listOf(2, 0, 1), chart.series[0].data)
            assertEquals("Contactmomenten", chart.series[0].label)
        }
    }

    @Nested
    inner class GetFilterOptions {
        @Test
        fun `returns distinct kanaal values`() {
            createContactmoment(kanaal = "Fysiek")
            createContactmoment(kanaal = "Videobellen")
            createContactmoment(kanaal = "Fysiek")
            em.flush()

            val options = provider.getFilterOptions(emptyMap())
            assertEquals(listOf("Fysiek", "Videobellen"), options["kanaal"])
        }

        @Test
        fun `cascading - kanaal filter narrows onderwerp options`() {
            createContactmoment(kanaal = "Fysiek", onderwerp = "Voortgangsgesprek")
            createContactmoment(kanaal = "Videobellen", onderwerp = "Brede Intake")
            em.flush()

            val options = provider.getFilterOptions(mapOf("kanaal" to "Fysiek"))
            assertEquals(listOf("Voortgangsgesprek"), options["onderwerp"])
            // Kanaal options should still show both
            assertEquals(listOf("Fysiek", "Videobellen"), options["kanaal"])
        }
    }

    @Nested
    inner class GetDetails {
        @Test
        fun `returns correct fields`() {
            createContactmoment(
                date = LocalDate.of(2025, 3, 15),
                kanaal = "Fysiek",
                onderwerp = "Voortgangsgesprek"
            )
            em.flush()

            val details = provider.getDetails("2025-03", null, emptyMap())
            assertEquals(1, details.rows.size)
            val row = details.rows[0]
            assertEquals("2025-03-15", row["date"])
            assertEquals("Jan Jansen", row["inwoner"])
            assertEquals("Fysiek", row["kanaal"])
            assertEquals("Voortgangsgesprek", row["onderwerp"])
        }

        @Test
        fun `filters by month`() {
            createContactmoment(date = LocalDate.of(2025, 3, 10))
            createContactmoment(date = LocalDate.of(2025, 4, 5))
            em.flush()

            val details = provider.getDetails("2025-03", null, emptyMap())
            assertEquals(1, details.rows.size)
        }
    }
}
