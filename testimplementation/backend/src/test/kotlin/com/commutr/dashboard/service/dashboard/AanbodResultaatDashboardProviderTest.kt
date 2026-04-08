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
        startDate: LocalDate = LocalDate.of(2025, 1, 15),
        eindDatum: LocalDate? = null,
        aanbodnaam: String = "Taalcoaching",
        afsluitreden: String? = null
    ): Aanbod = em.persist(Aanbod(
        inwoner = inwoner1, coach = coach, team = team,
        startDate = startDate, aanbodnaam = aanbodnaam, afsluitreden = afsluitreden,
        eindDatum = eindDatum
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
        fun `detail table has eindDatum column, not startDate or coach`() {
            val config = provider.getConfig()
            val columnKeys = config.detailTable.columns.map { it.key }
            assertTrue("eindDatum" in columnKeys)
            assertFalse("startDate" in columnKeys)
            assertFalse("coach" in columnKeys)
        }
    }

    @Nested
    inner class OnlyRecordsWithAfsluitredenAndEindDatum {
        @Test
        fun `excludes records without afsluitreden`() {
            createAanbod(afsluitreden = "Succesvol afgerond", eindDatum = LocalDate.of(2025, 3, 15))
            createAanbod(afsluitreden = null, eindDatum = null)
            em.flush()

            val summary = provider.getSummary(emptyMap())
            assertEquals(1, summary.total)
        }

        @Test
        fun `excludes records without eindDatum`() {
            createAanbod(afsluitreden = "Succesvol afgerond", eindDatum = LocalDate.of(2025, 3, 15))
            createAanbod(afsluitreden = "Succesvol afgerond", eindDatum = null)
            em.flush()

            val summary = provider.getSummary(emptyMap())
            assertEquals(1, summary.total)
        }

        @Test
        fun `excludes administratief afgesloten`() {
            createAanbod(afsluitreden = "Succesvol afgerond", eindDatum = LocalDate.of(2025, 3, 15))
            createAanbod(afsluitreden = "Administratief afgesloten", eindDatum = LocalDate.of(2025, 3, 20))
            em.flush()

            val summary = provider.getSummary(emptyMap())
            assertEquals(1, summary.total)
        }

        @Test
        fun `excludes aanbod afgesloten wegens wijzigen leerroute`() {
            createAanbod(afsluitreden = "Succesvol afgerond", eindDatum = LocalDate.of(2025, 3, 15))
            createAanbod(afsluitreden = "Aanbod afgesloten wegens wijzigen leerroute", eindDatum = LocalDate.of(2025, 3, 20))
            em.flush()

            val summary = provider.getSummary(emptyMap())
            assertEquals(1, summary.total)
        }

        @Test
        fun `includes valid afsluitredenen`() {
            createAanbod(afsluitreden = "Succesvol afgerond", eindDatum = LocalDate.of(2025, 3, 10))
            createAanbod(afsluitreden = "Voortijdig gestopt", eindDatum = LocalDate.of(2025, 3, 15))
            createAanbod(afsluitreden = "Medische redenen", eindDatum = LocalDate.of(2025, 3, 20))
            em.flush()

            val summary = provider.getSummary(emptyMap())
            assertEquals(3, summary.total)
        }

        @Test
        fun `exclusion applies to chart data`() {
            createAanbod(afsluitreden = "Succesvol afgerond", eindDatum = LocalDate.of(2025, 3, 10))
            createAanbod(afsluitreden = null, eindDatum = null)
            createAanbod(afsluitreden = "Administratief afgesloten", eindDatum = LocalDate.of(2025, 3, 25))
            em.flush()

            val chart = provider.getChartData(emptyMap())
            val total = chart.series.sumOf { it.data.sum() }
            assertEquals(1, total)
        }

        @Test
        fun `exclusion applies to filter options`() {
            createAanbod(afsluitreden = "Succesvol afgerond", eindDatum = LocalDate.of(2025, 3, 10))
            createAanbod(afsluitreden = null, eindDatum = null)
            createAanbod(afsluitreden = "Administratief afgesloten", eindDatum = LocalDate.of(2025, 3, 20))
            em.flush()

            val options = provider.getFilterOptions(emptyMap())
            assertEquals(listOf("Succesvol afgerond"), options["afsluitreden"])
        }

        @Test
        fun `exclusion applies to details`() {
            createAanbod(afsluitreden = "Succesvol afgerond", eindDatum = LocalDate.of(2025, 3, 10))
            createAanbod(afsluitreden = null, eindDatum = null)
            em.flush()

            val details = provider.getDetails("2025-03", null, emptyMap())
            assertEquals(1, details.rows.size)
        }
    }

    @Nested
    inner class UsesEindDatum {
        @Test
        fun `year filter uses eindDatum not startDate`() {
            createAanbod(startDate = LocalDate.of(2024, 6, 10), eindDatum = LocalDate.of(2025, 1, 15), afsluitreden = "Succesvol afgerond")
            createAanbod(startDate = LocalDate.of(2025, 3, 15), eindDatum = LocalDate.of(2025, 6, 20), afsluitreden = "Succesvol afgerond")
            em.flush()

            // Filter by year 2025 on eindDatum - both should match
            val summary = provider.getSummary(mapOf("year" to "2025"))
            assertEquals(2, summary.total)
        }

        @Test
        fun `chart groups by eindDatum month`() {
            createAanbod(startDate = LocalDate.of(2025, 1, 10), eindDatum = LocalDate.of(2025, 3, 15), afsluitreden = "Succesvol afgerond")
            createAanbod(startDate = LocalDate.of(2025, 1, 20), eindDatum = LocalDate.of(2025, 5, 10), afsluitreden = "Succesvol afgerond")
            em.flush()

            val chart = provider.getChartData(emptyMap())
            assertEquals("Mrt 2025", chart.labels[0])
            assertEquals(1, chart.series[0].data[0]) // Mar
            // May should also have 1
            val mayIndex = chart.labels.indexOf("Mei 2025")
            assertEquals(1, chart.series[0].data[mayIndex])
        }

        @Test
        fun `details filters by eindDatum month`() {
            createAanbod(startDate = LocalDate.of(2025, 1, 10), eindDatum = LocalDate.of(2025, 3, 15), afsluitreden = "Succesvol afgerond")
            createAanbod(startDate = LocalDate.of(2025, 3, 10), eindDatum = LocalDate.of(2025, 5, 20), afsluitreden = "Succesvol afgerond")
            em.flush()

            val details = provider.getDetails("2025-03", null, emptyMap())
            assertEquals(1, details.rows.size)
            assertEquals("2025-03-15", details.rows[0]["eindDatum"])
        }

        @Test
        fun `detail rows contain eindDatum field`() {
            createAanbod(eindDatum = LocalDate.of(2025, 3, 15), afsluitreden = "Succesvol afgerond")
            em.flush()

            val details = provider.getDetails("2025-03", null, emptyMap())
            assertTrue(details.rows[0].containsKey("eindDatum"))
            assertEquals("2025-03-15", details.rows[0]["eindDatum"])
        }
    }

    @Nested
    inner class IgnoresCoach {
        @Test
        fun `results include records from all coaches`() {
            createAanbod(coach = coach1, afsluitreden = "Succesvol afgerond", eindDatum = LocalDate.of(2025, 3, 15))
            createAanbod(coach = coach2, afsluitreden = "Voortijdig gestopt", eindDatum = LocalDate.of(2025, 3, 20))
            em.flush()

            val summary = provider.getSummary(emptyMap())
            assertEquals(2, summary.total)
        }

        @Test
        fun `detail rows do not contain coach field`() {
            createAanbod(afsluitreden = "Succesvol afgerond", eindDatum = LocalDate.of(2025, 3, 15))
            em.flush()

            val details = provider.getDetails("2025-03", null, emptyMap())
            assertFalse(details.rows[0].containsKey("coach"))
        }
    }

    @Nested
    inner class GetSummary {
        @Test
        fun `filters by aanbodnaam`() {
            createAanbod(aanbodnaam = "Taalcoaching", afsluitreden = "Succesvol afgerond", eindDatum = LocalDate.of(2025, 3, 10))
            createAanbod(aanbodnaam = "Taalcoaching", afsluitreden = "Voortijdig gestopt", eindDatum = LocalDate.of(2025, 3, 15))
            createAanbod(aanbodnaam = "Werkfit traject", afsluitreden = "Succesvol afgerond", eindDatum = LocalDate.of(2025, 3, 20))
            em.flush()

            val summary = provider.getSummary(mapOf("aanbodnaam" to "Taalcoaching"))
            assertEquals(2, summary.total)
        }

        @Test
        fun `filters by afsluitreden`() {
            createAanbod(afsluitreden = "Succesvol afgerond", eindDatum = LocalDate.of(2025, 3, 10))
            createAanbod(afsluitreden = "Succesvol afgerond", eindDatum = LocalDate.of(2025, 3, 15))
            createAanbod(afsluitreden = "Voortijdig gestopt", eindDatum = LocalDate.of(2025, 3, 20))
            em.flush()

            val summary = provider.getSummary(mapOf("afsluitreden" to "Succesvol afgerond"))
            assertEquals(2, summary.total)
        }

        @Test
        fun `filters by team`() {
            createAanbod(team = team1, afsluitreden = "Succesvol afgerond", eindDatum = LocalDate.of(2025, 3, 10))
            createAanbod(team = team2, afsluitreden = "Succesvol afgerond", eindDatum = LocalDate.of(2025, 3, 15))
            em.flush()

            val summary = provider.getSummary(mapOf("team" to "Team Volwassenen"))
            assertEquals(1, summary.total)
        }

        @Test
        fun `filters by year on eindDatum`() {
            createAanbod(eindDatum = LocalDate.of(2024, 6, 10), afsluitreden = "Succesvol afgerond")
            createAanbod(eindDatum = LocalDate.of(2025, 3, 15), afsluitreden = "Succesvol afgerond")
            em.flush()

            val summary = provider.getSummary(mapOf("year" to "2025"))
            assertEquals(1, summary.total)
        }
    }

    @Nested
    inner class GetChartData {
        @Test
        fun `creates stacked series per afsluitreden`() {
            createAanbod(aanbodnaam = "Taalcoaching", afsluitreden = "Succesvol afgerond", eindDatum = LocalDate.of(2025, 5, 10))
            createAanbod(aanbodnaam = "Taalcoaching", afsluitreden = "Succesvol afgerond", eindDatum = LocalDate.of(2025, 5, 15))
            createAanbod(aanbodnaam = "Taalcoaching", afsluitreden = "Voortijdig gestopt", eindDatum = LocalDate.of(2025, 5, 20))
            createAanbod(aanbodnaam = "Werkfit traject", afsluitreden = "Succesvol afgerond", eindDatum = LocalDate.of(2025, 5, 25))
            em.flush()

            val chart = provider.getChartData(emptyMap())
            assertEquals(2, chart.series.size)
            val succesvol = chart.series.find { it.label == "Succesvol afgerond" }
            val voortijdig = chart.series.find { it.label == "Voortijdig gestopt" }
            assertNotNull(succesvol)
            assertNotNull(voortijdig)
            assertEquals(3, succesvol!!.data[0])
            assertEquals(1, voortijdig!!.data[0])
        }
    }

    @Nested
    inner class GetDetails {
        @Test
        fun `filters by category (afsluitreden)`() {
            createAanbod(aanbodnaam = "Taalcoaching", afsluitreden = "Succesvol afgerond", eindDatum = LocalDate.of(2025, 3, 10))
            createAanbod(aanbodnaam = "Werkfit traject", afsluitreden = "Voortijdig gestopt", eindDatum = LocalDate.of(2025, 3, 15))
            em.flush()

            val details = provider.getDetails("2025-03", "Succesvol afgerond", emptyMap())
            assertEquals(1, details.rows.size)
            assertTrue(details.title.contains("Succesvol afgerond"))
        }

        @Test
        fun `rows contain afsluitreden`() {
            createAanbod(afsluitreden = "Succesvol afgerond", eindDatum = LocalDate.of(2025, 3, 10))
            em.flush()

            val details = provider.getDetails("2025-03", null, emptyMap())
            assertEquals("Succesvol afgerond", details.rows[0]["afsluitreden"])
        }
    }

    @Nested
    inner class GetFilterOptions {
        @Test
        fun `cascading works correctly`() {
            createAanbod(aanbodnaam = "Taalcoaching", team = team1, afsluitreden = "Succesvol afgerond", eindDatum = LocalDate.of(2025, 3, 10))
            createAanbod(aanbodnaam = "Werkfit traject", team = team2, afsluitreden = "Voortijdig gestopt", eindDatum = LocalDate.of(2025, 3, 15))
            em.flush()

            val options = provider.getFilterOptions(mapOf("team" to "Team Volwassenen"))
            assertEquals(listOf("Taalcoaching"), options["aanbodnaam"])
            assertEquals(listOf("Succesvol afgerond"), options["afsluitreden"])
            assertEquals(listOf("Team Inburgering", "Team Volwassenen"), options["team"])
        }
    }
}
