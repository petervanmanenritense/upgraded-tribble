package com.commutr.dashboard.service.dashboard

import com.commutr.dashboard.model.*
import com.commutr.dashboard.repository.AanbodRepository
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
@Import(AanbodResultaatDashboardProvider::class)
class AanbodResultaatDashboardProviderTest {

    @Autowired
    lateinit var provider: AanbodResultaatDashboardProvider

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

    private fun createAanbod(
        coach: Coach = coach1,
        team: Team = team1,
        startDate: LocalDate = LocalDate.of(2025, 3, 15),
        aanbodnaam: String = "Taalcoaching",
        afsluitreden: String? = null
    ): Aanbod = em.persist(Aanbod(
        inwoner = inwoner1, coach = coach, team = team,
        startDate = startDate, aanbodnaam = aanbodnaam, afsluitreden = afsluitreden
    ))

    @Test
    fun `id and label are correct`() {
        assertEquals("aanbodresultaat", provider.id)
        assertEquals("Aanbod resultaat", provider.label)
    }

    @Nested
    inner class GetConfig {
        @Test
        fun `chart type is stacked-bar`() {
            val config = provider.getConfig()
            assertEquals("stacked-bar", config.chart.type)
        }

        @Test
        fun `does not have coach filter`() {
            val config = provider.getConfig()
            val filterKeys = config.filters.map { it.key }
            assertFalse("coach" in filterKeys)
            assertEquals(listOf("year", "team", "aanbodnaam", "afsluitreden"), filterKeys)
        }

        @Test
        fun `detail table does not have coach column`() {
            val config = provider.getConfig()
            val columnKeys = config.detailTable.columns.map { it.key }
            assertFalse("coach" in columnKeys)
        }
    }

    @Nested
    inner class OnlyRecordsWithAfsluitreden {
        @Test
        fun `excludes records without afsluitreden`() {
            createAanbod(afsluitreden = "Succesvol afgerond")
            createAanbod(afsluitreden = null)
            em.flush()

            val summary = provider.getSummary(emptyMap())
            assertEquals(1, summary.total)
        }

        @Test
        fun `excludes administratief afgesloten`() {
            createAanbod(afsluitreden = "Succesvol afgerond")
            createAanbod(afsluitreden = "Administratief afgesloten")
            em.flush()

            val summary = provider.getSummary(emptyMap())
            assertEquals(1, summary.total)
        }

        @Test
        fun `excludes aanbod afgesloten wegens wijzigen leerroute`() {
            createAanbod(afsluitreden = "Succesvol afgerond")
            createAanbod(afsluitreden = "Aanbod afgesloten wegens wijzigen leerroute")
            em.flush()

            val summary = provider.getSummary(emptyMap())
            assertEquals(1, summary.total)
        }

        @Test
        fun `includes valid afsluitredenen`() {
            createAanbod(afsluitreden = "Succesvol afgerond")
            createAanbod(afsluitreden = "Voortijdig gestopt")
            createAanbod(afsluitreden = "Medische redenen")
            em.flush()

            val summary = provider.getSummary(emptyMap())
            assertEquals(3, summary.total)
        }

        @Test
        fun `exclusion applies to chart data`() {
            createAanbod(startDate = LocalDate.of(2025, 3, 10), afsluitreden = "Succesvol afgerond")
            createAanbod(startDate = LocalDate.of(2025, 3, 20), afsluitreden = null)
            createAanbod(startDate = LocalDate.of(2025, 3, 25), afsluitreden = "Administratief afgesloten")
            em.flush()

            val chart = provider.getChartData(emptyMap())
            val total = chart.series.sumOf { it.data.sum() }
            assertEquals(1, total)
        }

        @Test
        fun `exclusion applies to filter options`() {
            createAanbod(afsluitreden = "Succesvol afgerond")
            createAanbod(afsluitreden = null)
            createAanbod(afsluitreden = "Administratief afgesloten")
            em.flush()

            val options = provider.getFilterOptions(emptyMap())
            assertEquals(listOf("Succesvol afgerond"), options["afsluitreden"])
        }

        @Test
        fun `exclusion applies to details`() {
            createAanbod(startDate = LocalDate.of(2025, 3, 10), afsluitreden = "Succesvol afgerond")
            createAanbod(startDate = LocalDate.of(2025, 3, 20), afsluitreden = null)
            em.flush()

            val details = provider.getDetails("2025-03", null, emptyMap())
            assertEquals(1, details.rows.size)
        }
    }

    @Nested
    inner class IgnoresCoach {
        @Test
        fun `results include records from all coaches`() {
            createAanbod(coach = coach1, afsluitreden = "Succesvol afgerond")
            createAanbod(coach = coach2, afsluitreden = "Voortijdig gestopt")
            em.flush()

            val summary = provider.getSummary(emptyMap())
            assertEquals(2, summary.total)
        }

        @Test
        fun `detail rows do not contain coach field`() {
            createAanbod(afsluitreden = "Succesvol afgerond")
            em.flush()

            val details = provider.getDetails("2025-03", null, emptyMap())
            assertFalse(details.rows[0].containsKey("coach"))
        }
    }

    @Nested
    inner class GetSummary {
        @Test
        fun `filters by aanbodnaam`() {
            createAanbod(aanbodnaam = "Taalcoaching", afsluitreden = "Succesvol afgerond")
            createAanbod(aanbodnaam = "Taalcoaching", afsluitreden = "Voortijdig gestopt")
            createAanbod(aanbodnaam = "Werkfit traject", afsluitreden = "Succesvol afgerond")
            em.flush()

            val summary = provider.getSummary(mapOf("aanbodnaam" to "Taalcoaching"))
            assertEquals(2, summary.total)
        }

        @Test
        fun `filters by afsluitreden`() {
            createAanbod(afsluitreden = "Succesvol afgerond")
            createAanbod(afsluitreden = "Succesvol afgerond")
            createAanbod(afsluitreden = "Voortijdig gestopt")
            em.flush()

            val summary = provider.getSummary(mapOf("afsluitreden" to "Succesvol afgerond"))
            assertEquals(2, summary.total)
        }

        @Test
        fun `filters by team`() {
            createAanbod(team = team1, afsluitreden = "Succesvol afgerond")
            createAanbod(team = team2, afsluitreden = "Succesvol afgerond")
            em.flush()

            val summary = provider.getSummary(mapOf("team" to "Team Volwassenen"))
            assertEquals(1, summary.total)
        }

        @Test
        fun `filters by year`() {
            createAanbod(startDate = LocalDate.of(2024, 6, 10), afsluitreden = "Succesvol afgerond")
            createAanbod(startDate = LocalDate.of(2025, 3, 15), afsluitreden = "Succesvol afgerond")
            em.flush()

            val summary = provider.getSummary(mapOf("year" to "2025"))
            assertEquals(1, summary.total)
        }
    }

    @Nested
    inner class GetChartData {
        @Test
        fun `creates stacked series per aanbodnaam`() {
            createAanbod(startDate = LocalDate.of(2025, 5, 10), aanbodnaam = "Taalcoaching", afsluitreden = "Succesvol afgerond")
            createAanbod(startDate = LocalDate.of(2025, 5, 15), aanbodnaam = "Taalcoaching", afsluitreden = "Succesvol afgerond")
            createAanbod(startDate = LocalDate.of(2025, 5, 20), aanbodnaam = "Taalcoaching", afsluitreden = "Voortijdig gestopt")
            createAanbod(startDate = LocalDate.of(2025, 5, 25), aanbodnaam = "Werkfit traject", afsluitreden = "Succesvol afgerond")
            em.flush()

            val chart = provider.getChartData(emptyMap())
            assertEquals(2, chart.series.size)
            val taalcoaching = chart.series.find { it.label == "Taalcoaching" }
            assertEquals(3, taalcoaching!!.data[0])
        }
    }

    @Nested
    inner class GetDetails {
        @Test
        fun `filters by category`() {
            createAanbod(startDate = LocalDate.of(2025, 3, 10), aanbodnaam = "Taalcoaching", afsluitreden = "Succesvol afgerond")
            createAanbod(startDate = LocalDate.of(2025, 3, 15), aanbodnaam = "Werkfit traject", afsluitreden = "Succesvol afgerond")
            em.flush()

            val details = provider.getDetails("2025-03", "Taalcoaching", emptyMap())
            assertEquals(1, details.rows.size)
            assertTrue(details.title.contains("Taalcoaching"))
        }

        @Test
        fun `rows contain afsluitreden`() {
            createAanbod(startDate = LocalDate.of(2025, 3, 10), afsluitreden = "Succesvol afgerond")
            em.flush()

            val details = provider.getDetails("2025-03", null, emptyMap())
            assertEquals("Succesvol afgerond", details.rows[0]["afsluitreden"])
        }
    }

    @Nested
    inner class GetFilterOptions {
        @Test
        fun `cascading works correctly`() {
            createAanbod(aanbodnaam = "Taalcoaching", team = team1, afsluitreden = "Succesvol afgerond")
            createAanbod(aanbodnaam = "Werkfit traject", team = team2, afsluitreden = "Voortijdig gestopt")
            em.flush()

            val options = provider.getFilterOptions(mapOf("team" to "Team Volwassenen"))
            assertEquals(listOf("Taalcoaching"), options["aanbodnaam"])
            assertEquals(listOf("Succesvol afgerond"), options["afsluitreden"])
            // Team options should still show both
            assertEquals(listOf("Team Inburgering", "Team Volwassenen"), options["team"])
        }
    }
}
