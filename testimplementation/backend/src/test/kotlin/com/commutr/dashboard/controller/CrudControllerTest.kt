package com.commutr.dashboard.controller

import com.commutr.dashboard.model.*
import com.commutr.dashboard.repository.*
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import jakarta.transaction.Transactional
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDate

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CrudControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

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
    inner class Teams {
        @Test
        fun `GET all teams`() {
            mockMvc.perform(get("/api/v1/teams"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$[?(@.name == 'TestTeam')]").exists())
        }

        @Test
        fun `GET team by id`() {
            mockMvc.perform(get("/api/v1/teams/${team.id}"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.name").value("TestTeam"))
        }

        @Test
        fun `GET nonexistent team returns 404`() {
            mockMvc.perform(get("/api/v1/teams/99999"))
                .andExpect(status().isNotFound)
        }

        @Test
        fun `POST creates team`() {
            mockMvc.perform(
                post("/api/v1/teams")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name": "New Team"}""")
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.name").value("New Team"))
                .andExpect(jsonPath("$.id").isNumber)
        }

        @Test
        fun `PUT updates team`() {
            mockMvc.perform(
                put("/api/v1/teams/${team.id}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name": "Updated Team"}""")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.name").value("Updated Team"))
        }

        @Test
        fun `DELETE removes team`() {
            val toDelete = teamRepository.save(Team(name = "ToDelete"))
            mockMvc.perform(delete("/api/v1/teams/${toDelete.id}"))
                .andExpect(status().isNoContent)

            mockMvc.perform(get("/api/v1/teams/${toDelete.id}"))
                .andExpect(status().isNotFound)
        }

        @Test
        fun `DELETE nonexistent team returns 404`() {
            mockMvc.perform(delete("/api/v1/teams/99999"))
                .andExpect(status().isNotFound)
        }
    }

    @Nested
    inner class Coaches {
        @Test
        fun `GET all coaches`() {
            mockMvc.perform(get("/api/v1/coaches"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$[?(@.fullName == 'Test Coach')]").exists())
        }

        @Test
        fun `POST creates coach with team reference`() {
            mockMvc.perform(
                post("/api/v1/coaches")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"fullName": "New Coach", "teamId": ${team.id}}""")
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.fullName").value("New Coach"))
                .andExpect(jsonPath("$.team.name").value("TestTeam"))
        }

        @Test
        fun `POST with invalid team returns 400`() {
            mockMvc.perform(
                post("/api/v1/coaches")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"fullName": "New Coach", "teamId": 99999}""")
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `PUT updates coach`() {
            val team2 = teamRepository.save(Team(name = "Other Team"))
            mockMvc.perform(
                put("/api/v1/coaches/${coach.id}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"fullName": "Updated Coach", "teamId": ${team2.id}}""")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.fullName").value("Updated Coach"))
                .andExpect(jsonPath("$.team.name").value("Other Team"))
        }
    }

    @Nested
    inner class Inwoners {
        @Test
        fun `POST creates inwoner`() {
            mockMvc.perform(
                post("/api/v1/inwoners")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"fullName": "Nieuwe Inwoner", "birthdate": "1995-08-20", "administratienummer": "ADM999"}""")
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.fullName").value("Nieuwe Inwoner"))
                .andExpect(jsonPath("$.birthdate").value("1995-08-20"))
                .andExpect(jsonPath("$.administratienummer").value("ADM999"))
        }

        @Test
        fun `PUT updates inwoner`() {
            mockMvc.perform(
                put("/api/v1/inwoners/${inwoner.id}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"fullName": "Updated Inwoner", "birthdate": "1991-01-01", "administratienummer": "TEST001"}""")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.fullName").value("Updated Inwoner"))
        }
    }

    @Nested
    inner class Plaatsingen {
        @Test
        fun `POST creates plaatsing`() {
            mockMvc.perform(
                post("/api/v1/plaatsingen")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"inwonerById": ${inwoner.id}, "coachId": ${coach.id}, "teamId": ${team.id}, "startDate": "2025-06-01", "type": "Werk"}""")
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.type").value("Werk"))
                .andExpect(jsonPath("$.inwoner.fullName").value("Test Inwoner"))
                .andExpect(jsonPath("$.coach.fullName").value("Test Coach"))
                .andExpect(jsonPath("$.team.name").value("TestTeam"))
        }

        @Test
        fun `GET all plaatsingen`() {
            plaatsingRepository.save(Plaatsing(inwoner = inwoner, coach = coach, team = team, startDate = LocalDate.of(2025, 3, 15), type = "Werk"))

            mockMvc.perform(get("/api/v1/plaatsingen"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.length()").value(1))
        }

        @Test
        fun `PUT updates plaatsing`() {
            val p = plaatsingRepository.save(Plaatsing(inwoner = inwoner, coach = coach, team = team, startDate = LocalDate.of(2025, 3, 15), type = "Werk"))

            mockMvc.perform(
                put("/api/v1/plaatsingen/${p.id}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"inwonerById": ${inwoner.id}, "coachId": ${coach.id}, "teamId": ${team.id}, "startDate": "2025-07-01", "type": "Scholing"}""")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.type").value("Scholing"))
                .andExpect(jsonPath("$.startDate").value("2025-07-01"))
        }

        @Test
        fun `DELETE plaatsing`() {
            val p = plaatsingRepository.save(Plaatsing(inwoner = inwoner, coach = coach, team = team, startDate = LocalDate.of(2025, 3, 15), type = "Werk"))

            mockMvc.perform(delete("/api/v1/plaatsingen/${p.id}"))
                .andExpect(status().isNoContent)
        }

        @Test
        fun `POST with invalid inwoner returns 400`() {
            mockMvc.perform(
                post("/api/v1/plaatsingen")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"inwonerById": 99999, "coachId": ${coach.id}, "teamId": ${team.id}, "startDate": "2025-06-01", "type": "Werk"}""")
            )
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    inner class Contactmomenten {
        @Test
        fun `POST creates contactmoment`() {
            mockMvc.perform(
                post("/api/v1/contactmomenten")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"inwonerById": ${inwoner.id}, "coachId": ${coach.id}, "teamId": ${team.id}, "date": "2025-06-15", "kanaal": "Fysiek", "onderwerp": "Voortgangsgesprek"}""")
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.kanaal").value("Fysiek"))
                .andExpect(jsonPath("$.onderwerp").value("Voortgangsgesprek"))
        }

        @Test
        fun `DELETE contactmoment`() {
            val cm = contactmomentRepository.save(Contactmoment(inwoner = inwoner, coach = coach, team = team, date = LocalDate.of(2025, 3, 15), kanaal = "Fysiek", onderwerp = "Test"))

            mockMvc.perform(delete("/api/v1/contactmomenten/${cm.id}"))
                .andExpect(status().isNoContent)
        }
    }

    @Nested
    inner class AanbodCrud {
        @Test
        fun `POST creates aanbod with nullable afsluitreden`() {
            mockMvc.perform(
                post("/api/v1/aanbod")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"inwonerById": ${inwoner.id}, "coachId": ${coach.id}, "teamId": ${team.id}, "startDate": "2025-06-01", "aanbodnaam": "Taalcoaching", "afsluitreden": null}""")
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.aanbodnaam").value("Taalcoaching"))
                .andExpect(jsonPath("$.afsluitreden").doesNotExist())
        }

        @Test
        fun `POST creates aanbod with afsluitreden`() {
            mockMvc.perform(
                post("/api/v1/aanbod")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"inwonerById": ${inwoner.id}, "coachId": ${coach.id}, "teamId": ${team.id}, "startDate": "2025-06-01", "aanbodnaam": "Taalcoaching", "afsluitreden": "Succesvol afgerond"}""")
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.afsluitreden").value("Succesvol afgerond"))
        }

        @Test
        fun `PUT updates aanbod`() {
            val a = aanbodRepository.save(Aanbod(inwoner = inwoner, coach = coach, team = team, startDate = LocalDate.of(2025, 3, 15), aanbodnaam = "Taalcoaching"))

            mockMvc.perform(
                put("/api/v1/aanbod/${a.id}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"inwonerById": ${inwoner.id}, "coachId": ${coach.id}, "teamId": ${team.id}, "startDate": "2025-07-01", "aanbodnaam": "Werkfit traject", "afsluitreden": "Succesvol afgerond"}""")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.aanbodnaam").value("Werkfit traject"))
                .andExpect(jsonPath("$.afsluitreden").value("Succesvol afgerond"))
        }
    }
}
