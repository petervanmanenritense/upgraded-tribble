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
@Import(AanbodDashboardProvider::class)
class AanbodDashboardProviderTest {

    @Autowired
    lateinit var provider: AanbodDashboardProvider

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
        assertEquals("aanbod", provider.id)
        assertEquals("Aanbod", provider.label)
    }

    @Nested
    inner class GetConfig {
        @Test
        fun `chart type is stacked-bar`() {
            val config = provider.getConfig()
            assertEquals("stacked-bar", config.chart.type)
            assertTrue(config.chart.colors.size > 1)
        }

        @Test
        fun `returns correct filter keys`() {
            val config = provider.getConfig()
            val filterKeys = config.filters.map { it.key }
            assertEquals(listOf("year", "team", "coach", "aanbodnaam", "afsluitreden"), filterKeys)
        }
    }

    @Nested
    inner class ExclusionRule {
        @Test
        fun `excludes administratief afgesloten from summary`() {
            createAanbod(aanbodnaam = "Taalcoaching", afsluitreden = "Succesvol afgerond")
            createAanbod(aanbodnaam = "Werkfit traject", afsluitreden = "Administratief afgesloten")
            em.flush()

            val summary = provider.getSummary(emptyMap())
            assertEquals(1, summary.total)
        }

        @Test
        fun `excludes aanbod afgesloten wegens wijzigen leerroute from summary`() {
            createAanbod(afsluitreden = "Succesvol afgerond")
            createAanbod(afsluitreden = "Aanbod afgesloten wegens wijzigen leerroute")
            em.flush()

            val summary = provider.getSummary(emptyMap())
            assertEquals(1, summary.total)
        }

        @Test
        fun `includes records with null afsluitreden`() {
            createAanbod(afsluitreden = null)
            createAanbod(afsluitreden = "Succesvol afgerond")
            em.flush()

            val summary = provider.getSummary(emptyMap())
            assertEquals(2, summary.total)
        }

        @Test
        fun `excludes from chart data`() {
            createAanbod(startDate = LocalDate.of(2025, 3, 10), afsluitreden = null)
            createAanbod(startDate = LocalDate.of(2025, 3, 20), afsluitreden = "Administratief afgesloten")
            em.flush()

            val chart = provider.getChartData(emptyMap())
            val totalCount = chart.series.sumOf { s -> s.data.sum() }
            assertEquals(1, totalCount)
        }

        @Test
        fun `excludes from filter options`() {
            createAanbod(afsluitreden = "Succesvol afgerond")
            createAanbod(afsluitreden = "Administratief afgesloten")
            em.flush()

            val options = provider.getFilterOptions(emptyMap())
            assertFalse(options["afsluitreden"]!!.contains("Administratief afgesloten"))
            assertTrue(options["afsluitreden"]!!.contains("Succesvol afgerond"))
        }

        @Test
        fun `excludes from details`() {
            createAanbod(startDate = LocalDate.of(2025, 3, 10), afsluitreden = null)
            createAanbod(startDate = LocalDate.of(2025, 3, 20), afsluitreden = "Administratief afgesloten")
            em.flush()

            val details = provider.getDetails("2025-03", null, emptyMap())
            assertEquals(1, details.rows.size)
        }
    }

    @Nested
    inner class GetSummary {
        @Test
        fun `filters by aanbodnaam`() {
            createAanbod(aanbodnaam = "Taalcoaching")
            createAanbod(aanbodnaam = "Taalcoaching")
            createAanbod(aanbodnaam = "Werkfit traject")
            em.flush()

            val summary = provider.getSummary(mapOf("aanbodnaam" to "Taalcoaching"))
            assertEquals(2, summary.total)
        }

        @Test
        fun `filters by afsluitreden`() {
            createAanbod(afsluitreden = "Succesvol afgerond")
            createAanbod(afsluitreden = "Voortijdig gestopt")
            createAanbod(afsluitreden = "Succesvol afgerond")
            em.flush()

            val summary = provider.getSummary(mapOf("afsluitreden" to "Voortijdig gestopt"))
            assertEquals(1, summary.total)
        }
    }

    @Nested
    inner class GetChartData {
        @Test
        fun `creates stacked series per aanbodnaam`() {
            createAanbod(startDate = LocalDate.of(2025, 3, 10), aanbodnaam = "Taalcoaching")
            createAanbod(startDate = LocalDate.of(2025, 3, 15), aanbodnaam = "Taalcoaching")
            createAanbod(startDate = LocalDate.of(2025, 3, 20), aanbodnaam = "Werkfit traject")
            em.flush()

            val chart = provider.getChartData(emptyMap())
            assertEquals(2, chart.series.size)
            val taalcoaching = chart.series.find { it.label == "Taalcoaching" }
            val werkfit = chart.series.find { it.label == "Werkfit traject" }
            assertNotNull(taalcoaching)
            assertNotNull(werkfit)
            assertEquals(2, taalcoaching!!.data[0])
            assertEquals(1, werkfit!!.data[0])
        }

        @Test
        fun `fills zero months in stacked chart`() {
            createAanbod(startDate = LocalDate.of(2025, 1, 10), aanbodnaam = "Taalcoaching")
            createAanbod(startDate = LocalDate.of(2025, 3, 10), aanbodnaam = "Taalcoaching")
            em.flush()

            val chart = provider.getChartData(emptyMap())
            assertEquals(3, chart.labels.size) // Jan, Feb, Mar
            assertEquals(listOf(1, 0, 1), chart.series[0].data)
        }
    }

    @Nested
    inner class GetDetails {
        @Test
        fun `filters by category (aanbodnaam)`() {
            createAanbod(startDate = LocalDate.of(2025, 3, 10), aanbodnaam = "Taalcoaching")
            createAanbod(startDate = LocalDate.of(2025, 3, 15), aanbodnaam = "Werkfit traject")
            em.flush()

            val details = provider.getDetails("2025-03", "Taalcoaching", emptyMap())
            assertEquals(1, details.rows.size)
            assertEquals("Taalcoaching", details.rows[0]["aanbodnaam"])
            assertTrue(details.title.contains("Taalcoaching"))
        }

        @Test
        fun `returns all when no category specified`() {
            createAanbod(startDate = LocalDate.of(2025, 3, 10), aanbodnaam = "Taalcoaching")
            createAanbod(startDate = LocalDate.of(2025, 3, 15), aanbodnaam = "Werkfit traject")
            em.flush()

            val details = provider.getDetails("2025-03", null, emptyMap())
            assertEquals(2, details.rows.size)
        }

        @Test
        fun `detail rows contain afsluitreden`() {
            createAanbod(startDate = LocalDate.of(2025, 3, 10), afsluitreden = "Succesvol afgerond")
            em.flush()

            val details = provider.getDetails("2025-03", null, emptyMap())
            assertEquals("Succesvol afgerond", details.rows[0]["afsluitreden"])
        }
    }

    @Nested
    inner class GetFilterOptions {
        @Test
        fun `cascading - aanbodnaam filter narrows afsluitreden options`() {
            createAanbod(aanbodnaam = "Taalcoaching", afsluitreden = "Succesvol afgerond")
            createAanbod(aanbodnaam = "Werkfit traject", afsluitreden = "Voortijdig gestopt")
            em.flush()

            val options = provider.getFilterOptions(mapOf("aanbodnaam" to "Taalcoaching"))
            assertEquals(listOf("Succesvol afgerond"), options["afsluitreden"])
            // Aanbodnaam should still show both
            assertEquals(listOf("Taalcoaching", "Werkfit traject"), options["aanbodnaam"])
        }
    }
}
