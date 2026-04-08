package com.commutr.dashboard.service.dashboard

import com.commutr.dashboard.model.*
import com.commutr.dashboard.repository.PlaatsingRepository
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
@Import(PlaatsingDashboardProvider::class)
class PlaatsingDashboardProviderTest {

    @Autowired
    lateinit var provider: PlaatsingDashboardProvider

    @Autowired
    lateinit var em: TestEntityManager

    @Autowired
    lateinit var plaatsingRepository: PlaatsingRepository

    private lateinit var team1: Team
    private lateinit var team2: Team
    private lateinit var coach1: Coach
    private lateinit var coach2: Coach
    private lateinit var inwoner1: Inwoner
    private lateinit var inwoner2: Inwoner

    @BeforeEach
    fun setup() {
        team1 = em.persist(Team(name = "Team Volwassenen"))
        team2 = em.persist(Team(name = "Team Inburgering"))
        coach1 = em.persist(Coach(fullName = "Peter de Vries", team = team1))
        coach2 = em.persist(Coach(fullName = "Ahmed El Amrani", team = team2))
        inwoner1 = em.persist(Inwoner(fullName = "Jan Jansen", birthdate = LocalDate.of(1990, 1, 15), administratienummer = "ADM001"))
        inwoner2 = em.persist(Inwoner(fullName = "Maria Bakker", birthdate = LocalDate.of(1985, 6, 20), administratienummer = "ADM002"))
        em.flush()
    }

    private fun createPlaatsing(
        inwoner: Inwoner = inwoner1,
        coach: Coach = coach1,
        team: Team = team1,
        startDate: LocalDate = LocalDate.of(2025, 3, 15),
        type: String = "Werk"
    ): Plaatsing = em.persist(Plaatsing(
        inwoner = inwoner, coach = coach, team = team,
        startDate = startDate, type = type
    ))

    @Test
    fun `id and label are correct`() {
        assertEquals("plaatsingen", provider.id)
        assertEquals("Plaatsingen", provider.label)
    }

    @Nested
    inner class GetConfig {
        @Test
        fun `returns correct filter keys`() {
            val config = provider.getConfig()
            val filterKeys = config.filters.map { it.key }
            assertEquals(listOf("year", "team", "coach", "type"), filterKeys)
        }

        @Test
        fun `chart type is bar`() {
            val config = provider.getConfig()
            assertEquals("bar", config.chart.type)
            assertEquals(listOf("#0f62fe"), config.chart.colors)
        }

        @Test
        fun `detail table has correct columns`() {
            val config = provider.getConfig()
            val columnKeys = config.detailTable.columns.map { it.key }
            assertTrue("date" in columnKeys)
            assertTrue("inwoner" in columnKeys)
            assertTrue("type" in columnKeys)
            assertEquals(7, columnKeys.size)
        }
    }

    @Nested
    inner class GetSummary {
        @Test
        fun `returns zero summary when no data`() {
            val summary = provider.getSummary(emptyMap())
            assertEquals(0, summary.total)
            assertEquals(0, summary.avgPerMonth)
            assertNull(summary.topMonth)
        }

        @Test
        fun `counts all records without filters`() {
            createPlaatsing(startDate = LocalDate.of(2025, 1, 10))
            createPlaatsing(startDate = LocalDate.of(2025, 1, 20))
            createPlaatsing(startDate = LocalDate.of(2025, 2, 5))
            em.flush()

            val summary = provider.getSummary(emptyMap())
            assertEquals(3, summary.total)
        }

        @Test
        fun `calculates average per month`() {
            createPlaatsing(startDate = LocalDate.of(2025, 1, 10))
            createPlaatsing(startDate = LocalDate.of(2025, 1, 20))
            createPlaatsing(startDate = LocalDate.of(2025, 2, 5))
            createPlaatsing(startDate = LocalDate.of(2025, 2, 15))
            em.flush()

            val summary = provider.getSummary(emptyMap())
            assertEquals(4, summary.total)
            assertEquals(2, summary.avgPerMonth) // 4 / 2 months
        }

        @Test
        fun `identifies top month`() {
            createPlaatsing(startDate = LocalDate.of(2025, 1, 10))
            createPlaatsing(startDate = LocalDate.of(2025, 2, 5))
            createPlaatsing(startDate = LocalDate.of(2025, 2, 15))
            createPlaatsing(startDate = LocalDate.of(2025, 2, 25))
            em.flush()

            val summary = provider.getSummary(emptyMap())
            assertNotNull(summary.topMonth)
            assertEquals("Feb 2025", summary.topMonth!!.label)
            assertEquals(3, summary.topMonth!!.value)
        }

        @Test
        fun `filters by year`() {
            createPlaatsing(startDate = LocalDate.of(2024, 6, 10))
            createPlaatsing(startDate = LocalDate.of(2025, 3, 15))
            createPlaatsing(startDate = LocalDate.of(2025, 4, 20))
            em.flush()

            val summary = provider.getSummary(mapOf("year" to "2025"))
            assertEquals(2, summary.total)
        }

        @Test
        fun `filters by team`() {
            createPlaatsing(team = team1)
            createPlaatsing(team = team1)
            createPlaatsing(team = team2)
            em.flush()

            val summary = provider.getSummary(mapOf("team" to "Team Volwassenen"))
            assertEquals(2, summary.total)
        }

        @Test
        fun `filters by coach`() {
            createPlaatsing(coach = coach1)
            createPlaatsing(coach = coach2)
            em.flush()

            val summary = provider.getSummary(mapOf("coach" to "Peter de Vries"))
            assertEquals(1, summary.total)
        }

        @Test
        fun `filters by type`() {
            createPlaatsing(type = "Werk")
            createPlaatsing(type = "Werk")
            createPlaatsing(type = "Scholing")
            em.flush()

            val summary = provider.getSummary(mapOf("type" to "Scholing"))
            assertEquals(1, summary.total)
        }

        @Test
        fun `combines multiple filters`() {
            createPlaatsing(team = team1, type = "Werk", startDate = LocalDate.of(2025, 1, 10))
            createPlaatsing(team = team1, type = "Scholing", startDate = LocalDate.of(2025, 1, 20))
            createPlaatsing(team = team2, type = "Werk", startDate = LocalDate.of(2025, 1, 15))
            em.flush()

            val summary = provider.getSummary(mapOf("team" to "Team Volwassenen", "type" to "Werk"))
            assertEquals(1, summary.total)
        }

        @Test
        fun `blank filter values are treated as no filter`() {
            createPlaatsing()
            createPlaatsing()
            em.flush()

            val summary = provider.getSummary(mapOf("year" to "", "team" to ""))
            assertEquals(2, summary.total)
        }
    }

    @Nested
    inner class GetChartData {
        @Test
        fun `returns empty chart when no data`() {
            val chart = provider.getChartData(emptyMap())
            assertTrue(chart.labels.isEmpty())
            assertEquals(1, chart.series.size)
            assertEquals("Plaatsingen", chart.series[0].label)
        }

        @Test
        fun `groups records by month`() {
            createPlaatsing(startDate = LocalDate.of(2025, 1, 10))
            createPlaatsing(startDate = LocalDate.of(2025, 1, 20))
            createPlaatsing(startDate = LocalDate.of(2025, 3, 5))
            em.flush()

            val chart = provider.getChartData(emptyMap())
            assertEquals(listOf("Jan 2025", "Feb 2025", "Mrt 2025"), chart.labels)
            assertEquals(listOf(2, 0, 1), chart.series[0].data)
        }

        @Test
        fun `shows all 12 months when year filter active`() {
            createPlaatsing(startDate = LocalDate.of(2025, 3, 10))
            em.flush()

            val chart = provider.getChartData(mapOf("year" to "2025"))
            assertEquals(12, chart.labels.size)
            assertEquals("Jan 2025", chart.labels[0])
            assertEquals("Dec 2025", chart.labels[11])
            assertEquals(0, chart.series[0].data[0]) // Jan = 0
            assertEquals(1, chart.series[0].data[2]) // Mar = 1
        }

        @Test
        fun `fills gaps between months with zeros`() {
            createPlaatsing(startDate = LocalDate.of(2025, 1, 10))
            createPlaatsing(startDate = LocalDate.of(2025, 4, 10))
            em.flush()

            val chart = provider.getChartData(emptyMap())
            assertEquals(4, chart.labels.size)
            assertEquals(listOf(1, 0, 0, 1), chart.series[0].data)
        }
    }

    @Nested
    inner class GetFilterOptions {
        @Test
        fun `returns all distinct years`() {
            createPlaatsing(startDate = LocalDate.of(2024, 5, 10))
            createPlaatsing(startDate = LocalDate.of(2025, 3, 15))
            em.flush()

            val options = provider.getFilterOptions(emptyMap())
            assertEquals(listOf("2024", "2025"), options["year"])
        }

        @Test
        fun `returns all distinct teams`() {
            createPlaatsing(team = team1)
            createPlaatsing(team = team2)
            em.flush()

            val options = provider.getFilterOptions(emptyMap())
            assertTrue(options["team"]!!.containsAll(listOf("Team Inburgering", "Team Volwassenen")))
        }

        @Test
        fun `cascading - year filter does not apply to year options`() {
            createPlaatsing(startDate = LocalDate.of(2024, 5, 10), team = team1)
            createPlaatsing(startDate = LocalDate.of(2025, 3, 15), team = team1)
            em.flush()

            val options = provider.getFilterOptions(mapOf("year" to "2025"))
            // Year options should show both years (year filter excluded for year)
            assertEquals(listOf("2024", "2025"), options["year"])
            // Team options should only show teams from 2025
            assertEquals(listOf("Team Volwassenen"), options["team"])
        }

        @Test
        fun `cascading - team filter narrows other options`() {
            createPlaatsing(team = team1, coach = coach1, type = "Werk")
            createPlaatsing(team = team2, coach = coach2, type = "Scholing")
            em.flush()

            val options = provider.getFilterOptions(mapOf("team" to "Team Volwassenen"))
            assertEquals(listOf("Peter de Vries"), options["coach"])
            assertEquals(listOf("Werk"), options["type"])
            // Team options should still show both (team filter excluded for team)
            assertEquals(listOf("Team Inburgering", "Team Volwassenen"), options["team"])
        }
    }

    @Nested
    inner class GetDetails {
        @Test
        fun `returns records for specified month`() {
            createPlaatsing(startDate = LocalDate.of(2025, 3, 10), type = "Werk")
            createPlaatsing(startDate = LocalDate.of(2025, 3, 20), type = "Scholing")
            createPlaatsing(startDate = LocalDate.of(2025, 4, 5), type = "Werk")
            em.flush()

            val details = provider.getDetails("2025-03", null, emptyMap())
            assertEquals(2, details.rows.size)
            assertTrue(details.title.contains("Mrt 2025"))
        }

        @Test
        fun `detail rows contain all expected fields`() {
            createPlaatsing(
                inwoner = inwoner1, coach = coach1, team = team1,
                startDate = LocalDate.of(2025, 3, 15), type = "Werk"
            )
            em.flush()

            val details = provider.getDetails("2025-03", null, emptyMap())
            val row = details.rows[0]
            assertEquals("2025-03-15", row["date"])
            assertEquals("Jan Jansen", row["inwoner"])
            assertEquals("ADM001", row["administratienummer"])
            assertEquals("1990-01-15", row["birthdate"])
            assertEquals("Peter de Vries", row["coach"])
            assertEquals("Team Volwassenen", row["team"])
            assertEquals("Werk", row["type"])
        }

        @Test
        fun `respects active filters`() {
            createPlaatsing(startDate = LocalDate.of(2025, 3, 10), type = "Werk")
            createPlaatsing(startDate = LocalDate.of(2025, 3, 20), type = "Scholing")
            em.flush()

            val details = provider.getDetails("2025-03", null, mapOf("type" to "Werk"))
            assertEquals(1, details.rows.size)
            assertEquals("Werk", details.rows[0]["type"])
        }
    }

    @Nested
    inner class CompanionUtilities {
        @Test
        fun `formatMonth formats correctly`() {
            assertEquals("Jan 2025", PlaatsingDashboardProvider.formatMonth(java.time.YearMonth.of(2025, 1)))
            assertEquals("Mrt 2025", PlaatsingDashboardProvider.formatMonth(java.time.YearMonth.of(2025, 3)))
            assertEquals("Dec 2024", PlaatsingDashboardProvider.formatMonth(java.time.YearMonth.of(2024, 12)))
        }

        @Test
        fun `buildMonthLabels with year filter returns 12 months`() {
            val months = PlaatsingDashboardProvider.buildMonthLabels(emptyList(), "2025")
            assertEquals(12, months.size)
            assertEquals(java.time.YearMonth.of(2025, 1), months.first())
            assertEquals(java.time.YearMonth.of(2025, 12), months.last())
        }

        @Test
        fun `buildMonthLabels without year filter spans full range`() {
            val dates = listOf(
                LocalDate.of(2025, 2, 10),
                LocalDate.of(2025, 5, 15)
            )
            val months = PlaatsingDashboardProvider.buildMonthLabels(dates, null)
            assertEquals(4, months.size)
            assertEquals(java.time.YearMonth.of(2025, 2), months.first())
            assertEquals(java.time.YearMonth.of(2025, 5), months.last())
        }

        @Test
        fun `buildMonthLabels with empty dates returns empty`() {
            val months = PlaatsingDashboardProvider.buildMonthLabels(emptyList(), null)
            assertTrue(months.isEmpty())
        }

        @Test
        fun `buildSummary with empty list returns zeros`() {
            val summary = PlaatsingDashboardProvider.buildSummary(emptyList())
            assertEquals(0, summary.total)
            assertEquals(0, summary.avgPerMonth)
            assertNull(summary.topMonth)
        }
    }
}
