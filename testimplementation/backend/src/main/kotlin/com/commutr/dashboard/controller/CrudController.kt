package com.commutr.dashboard.controller

import com.commutr.dashboard.model.*
import com.commutr.dashboard.repository.*
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

// --- Request DTOs ---

data class TeamRequest(val name: String)
data class CoachRequest(val fullName: String, val teamId: Long)
data class InwonerRequest(val fullName: String, val birthdate: String, val administratienummer: String)
data class PlaatsingRequest(val inwonerById: Long, val coachId: Long, val teamId: Long, val startDate: String, val type: String)
data class ContactmomentRequest(val inwonerById: Long, val coachId: Long, val teamId: Long, val date: String, val kanaal: String, val onderwerp: String)
data class AanbodRequest(val inwonerById: Long, val coachId: Long, val teamId: Long, val startDate: String, val aanbodnaam: String, val afsluitreden: String?)

// --- Team Controller ---

@RestController
@RequestMapping("/api/v1/teams")
class TeamController(private val teamRepository: TeamRepository) {

    @GetMapping
    fun getAll(): List<Team> = teamRepository.findAll()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): Team =
        teamRepository.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody request: TeamRequest): Team =
        teamRepository.save(Team(name = request.name))

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: TeamRequest): Team {
        val team = teamRepository.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        team.name = request.name
        return teamRepository.save(team)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) {
        if (!teamRepository.existsById(id)) throw ResponseStatusException(HttpStatus.NOT_FOUND)
        teamRepository.deleteById(id)
    }
}

// --- Coach Controller ---

@RestController
@RequestMapping("/api/v1/coaches")
class CoachController(
    private val coachRepository: CoachRepository,
    private val teamRepository: TeamRepository
) {

    @GetMapping
    fun getAll(): List<Coach> = coachRepository.findAll()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): Coach =
        coachRepository.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody request: CoachRequest): Coach {
        val team = teamRepository.findById(request.teamId).orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Team not found") }
        return coachRepository.save(Coach(fullName = request.fullName, team = team))
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: CoachRequest): Coach {
        val coach = coachRepository.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        val team = teamRepository.findById(request.teamId).orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Team not found") }
        coach.fullName = request.fullName
        coach.team = team
        return coachRepository.save(coach)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) {
        if (!coachRepository.existsById(id)) throw ResponseStatusException(HttpStatus.NOT_FOUND)
        coachRepository.deleteById(id)
    }
}

// --- Inwoner Controller ---

@RestController
@RequestMapping("/api/v1/inwoners")
class InwonerController(private val inwonerRepository: InwonerRepository) {

    @GetMapping
    fun getAll(): List<Inwoner> = inwonerRepository.findAll()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): Inwoner =
        inwonerRepository.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody request: InwonerRequest): Inwoner =
        inwonerRepository.save(Inwoner(
            fullName = request.fullName,
            birthdate = java.time.LocalDate.parse(request.birthdate),
            administratienummer = request.administratienummer
        ))

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: InwonerRequest): Inwoner {
        val inwoner = inwonerRepository.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        inwoner.fullName = request.fullName
        inwoner.birthdate = java.time.LocalDate.parse(request.birthdate)
        inwoner.administratienummer = request.administratienummer
        return inwonerRepository.save(inwoner)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) {
        if (!inwonerRepository.existsById(id)) throw ResponseStatusException(HttpStatus.NOT_FOUND)
        inwonerRepository.deleteById(id)
    }
}

// --- Plaatsing Controller ---

@RestController
@RequestMapping("/api/v1/plaatsingen")
class PlaatsingController(
    private val plaatsingRepository: PlaatsingRepository,
    private val inwonerRepository: InwonerRepository,
    private val coachRepository: CoachRepository,
    private val teamRepository: TeamRepository
) {

    @GetMapping
    fun getAll(): List<Plaatsing> = plaatsingRepository.findAll()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): Plaatsing =
        plaatsingRepository.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody request: PlaatsingRequest): Plaatsing {
        val inwoner = inwonerRepository.findById(request.inwonerById).orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Inwoner not found") }
        val coach = coachRepository.findById(request.coachId).orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Coach not found") }
        val team = teamRepository.findById(request.teamId).orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Team not found") }
        return plaatsingRepository.save(Plaatsing(
            inwoner = inwoner, coach = coach, team = team,
            startDate = java.time.LocalDate.parse(request.startDate), type = request.type
        ))
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: PlaatsingRequest): Plaatsing {
        val plaatsing = plaatsingRepository.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        plaatsing.inwoner = inwonerRepository.findById(request.inwonerById).orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Inwoner not found") }
        plaatsing.coach = coachRepository.findById(request.coachId).orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Coach not found") }
        plaatsing.team = teamRepository.findById(request.teamId).orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Team not found") }
        plaatsing.startDate = java.time.LocalDate.parse(request.startDate)
        plaatsing.type = request.type
        return plaatsingRepository.save(plaatsing)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) {
        if (!plaatsingRepository.existsById(id)) throw ResponseStatusException(HttpStatus.NOT_FOUND)
        plaatsingRepository.deleteById(id)
    }
}

// --- Contactmoment Controller ---

@RestController
@RequestMapping("/api/v1/contactmomenten")
class ContactmomentController(
    private val contactmomentRepository: ContactmomentRepository,
    private val inwonerRepository: InwonerRepository,
    private val coachRepository: CoachRepository,
    private val teamRepository: TeamRepository
) {

    @GetMapping
    fun getAll(): List<Contactmoment> = contactmomentRepository.findAll()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): Contactmoment =
        contactmomentRepository.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody request: ContactmomentRequest): Contactmoment {
        val inwoner = inwonerRepository.findById(request.inwonerById).orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Inwoner not found") }
        val coach = coachRepository.findById(request.coachId).orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Coach not found") }
        val team = teamRepository.findById(request.teamId).orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Team not found") }
        return contactmomentRepository.save(Contactmoment(
            inwoner = inwoner, coach = coach, team = team,
            date = java.time.LocalDate.parse(request.date), kanaal = request.kanaal, onderwerp = request.onderwerp
        ))
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: ContactmomentRequest): Contactmoment {
        val cm = contactmomentRepository.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        cm.inwoner = inwonerRepository.findById(request.inwonerById).orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Inwoner not found") }
        cm.coach = coachRepository.findById(request.coachId).orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Coach not found") }
        cm.team = teamRepository.findById(request.teamId).orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Team not found") }
        cm.date = java.time.LocalDate.parse(request.date)
        cm.kanaal = request.kanaal
        cm.onderwerp = request.onderwerp
        return contactmomentRepository.save(cm)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) {
        if (!contactmomentRepository.existsById(id)) throw ResponseStatusException(HttpStatus.NOT_FOUND)
        contactmomentRepository.deleteById(id)
    }
}

// --- Aanbod Controller ---

@RestController
@RequestMapping("/api/v1/aanbod")
class AanbodController(
    private val aanbodRepository: AanbodRepository,
    private val inwonerRepository: InwonerRepository,
    private val coachRepository: CoachRepository,
    private val teamRepository: TeamRepository
) {

    @GetMapping
    fun getAll(): List<Aanbod> = aanbodRepository.findAll()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): Aanbod =
        aanbodRepository.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody request: AanbodRequest): Aanbod {
        val inwoner = inwonerRepository.findById(request.inwonerById).orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Inwoner not found") }
        val coach = coachRepository.findById(request.coachId).orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Coach not found") }
        val team = teamRepository.findById(request.teamId).orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Team not found") }
        return aanbodRepository.save(Aanbod(
            inwoner = inwoner, coach = coach, team = team,
            startDate = java.time.LocalDate.parse(request.startDate),
            aanbodnaam = request.aanbodnaam, afsluitreden = request.afsluitreden
        ))
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: AanbodRequest): Aanbod {
        val aanbod = aanbodRepository.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        aanbod.inwoner = inwonerRepository.findById(request.inwonerById).orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Inwoner not found") }
        aanbod.coach = coachRepository.findById(request.coachId).orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Coach not found") }
        aanbod.team = teamRepository.findById(request.teamId).orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Team not found") }
        aanbod.startDate = java.time.LocalDate.parse(request.startDate)
        aanbod.aanbodnaam = request.aanbodnaam
        aanbod.afsluitreden = request.afsluitreden
        return aanbodRepository.save(aanbod)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) {
        if (!aanbodRepository.existsById(id)) throw ResponseStatusException(HttpStatus.NOT_FOUND)
        aanbodRepository.deleteById(id)
    }
}
