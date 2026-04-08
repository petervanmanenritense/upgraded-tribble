package com.commutr.dashboard.controller

import com.commutr.dashboard.model.*
import com.commutr.dashboard.repository.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import jakarta.transaction.Transactional
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDate

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DashboardControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var teamRepository: TeamRepository
    @Autowired
    lateinit var coachRepository: CoachRepository
    @Autowired
    lateinit var inwonerRepository: InwonerRepository
    @Autowired
    lateinit var plaatsingRepository: PlaatsingRepository
    @Autowired
    lateinit var contactmomentRepository: ContactmomentRepository
    @Autowired
    lateinit var aanbodRepository: AanbodRepository

    private lateinit var team: Team
    private lateinit var coach: Coach
    private lateinit var inwoner: Inwoner

    @BeforeEach
    fun setup() {
        team = teamRepository.save(Team(name = "TestTeam"))
        coach = coachRepository.save(Coach(fullName = "Test Coach", team = team))
        inwoner = inwonerRepository.save(Inwoner(fullName = "Test Inwoner", birthdate = LocalDate.of(1990, 5, 15), administratienummer = "TEST001"))
    }

    @Nested
    inner class ListDashboards {
        @Test
        fun `returns all dashboards`() {
            mockMvc.perform(get("/api/v1/dashboards"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[?(@.id == 'plaatsingen')]").exists())
                .andExpect(jsonPath("$[?(@.id == 'contactmomenten')]").exists())
                .andExpect(jsonPath("$[?(@.id == 'aanbod')]").exists())
        }
    }

    @Nested
    inner class GetConfig {
        @Test
        fun `returns config for plaatsingen`() {
            mockMvc.perform(get("/api/v1/dashboards/plaatsingen/config"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value("plaatsingen"))
                .andExpect(jsonPath("$.chart.type").value("bar"))
                .andExpect(jsonPath("$.filters.length()").value(4))
        }

        @Test
        fun `returns 404 for unknown dashboard`() {
            mockMvc.perform(get("/api/v1/dashboards/nonexistent/config"))
                .andExpect(status().isNotFound)
        }
    }

    @Nested
    inner class GetSummary {
        @Test
        fun `returns summary for plaatsingen`() {
            plaatsingRepository.save(Plaatsing(inwoner = inwoner, coach = coach, team = team, startDate = LocalDate.of(2025, 3, 15), type = "Werk"))
            plaatsingRepository.save(Plaatsing(inwoner = inwoner, coach = coach, team = team, startDate = LocalDate.of(2025, 3, 20), type = "Scholing"))

            mockMvc.perform(get("/api/v1/dashboards/plaatsingen/summary"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.total").value(2))
        }

        @Test
        fun `applies query param filters`() {
            plaatsingRepository.save(Plaatsing(inwoner = inwoner, coach = coach, team = team, startDate = LocalDate.of(2025, 3, 15), type = "Werk"))
            plaatsingRepository.save(Plaatsing(inwoner = inwoner, coach = coach, team = team, startDate = LocalDate.of(2025, 3, 20), type = "Scholing"))

            mockMvc.perform(get("/api/v1/dashboards/plaatsingen/summary?type=Werk"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.total").value(1))
        }
    }

    @Nested
    inner class GetChart {
        @Test
        fun `returns chart data`() {
            plaatsingRepository.save(Plaatsing(inwoner = inwoner, coach = coach, team = team, startDate = LocalDate.of(2025, 3, 15), type = "Werk"))

            mockMvc.perform(get("/api/v1/dashboards/plaatsingen/chart"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.labels").isArray)
                .andExpect(jsonPath("$.series").isArray)
                .andExpect(jsonPath("$.series[0].label").value("Plaatsingen"))
        }

        @Test
        fun `year filter returns 12 months`() {
            plaatsingRepository.save(Plaatsing(inwoner = inwoner, coach = coach, team = team, startDate = LocalDate.of(2025, 6, 15), type = "Werk"))

            mockMvc.perform(get("/api/v1/dashboards/plaatsingen/chart?year=2025"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.labels.length()").value(12))
        }
    }

    @Nested
    inner class GetFilters {
        @Test
        fun `returns filter options`() {
            plaatsingRepository.save(Plaatsing(inwoner = inwoner, coach = coach, team = team, startDate = LocalDate.of(2025, 3, 15), type = "Werk"))

            mockMvc.perform(get("/api/v1/dashboards/plaatsingen/filters"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.year").isArray)
                .andExpect(jsonPath("$.team").isArray)
                .andExpect(jsonPath("$.type[0]").value("Werk"))
        }
    }

    @Nested
    inner class GetDetails {
        @Test
        fun `returns detail rows for month`() {
            plaatsingRepository.save(Plaatsing(inwoner = inwoner, coach = coach, team = team, startDate = LocalDate.of(2025, 3, 15), type = "Werk"))

            mockMvc.perform(get("/api/v1/dashboards/plaatsingen/details?month=2025-03"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.title").isString)
                .andExpect(jsonPath("$.rows.length()").value(1))
                .andExpect(jsonPath("$.rows[0].type").value("Werk"))
        }

        @Test
        fun `aanbod details support category param`() {
            aanbodRepository.save(Aanbod(inwoner = inwoner, coach = coach, team = team, startDate = LocalDate.of(2025, 3, 10), aanbodnaam = "Taalcoaching"))
            aanbodRepository.save(Aanbod(inwoner = inwoner, coach = coach, team = team, startDate = LocalDate.of(2025, 3, 15), aanbodnaam = "Werkfit traject"))

            mockMvc.perform(get("/api/v1/dashboards/aanbod/details?month=2025-03&category=Taalcoaching"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.rows.length()").value(1))
                .andExpect(jsonPath("$.rows[0].aanbodnaam").value("Taalcoaching"))
        }
    }
}
