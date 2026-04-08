package com.commutr.dashboard.config

import com.commutr.dashboard.model.*
import com.commutr.dashboard.repository.*
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.LocalDate
import kotlin.random.Random

@Component
@Profile("!test")
class DataInitializer(
    private val teamRepository: TeamRepository,
    private val coachRepository: CoachRepository,
    private val inwonerRepository: InwonerRepository,
    private val plaatsingRepository: PlaatsingRepository,
    private val contactmomentRepository: ContactmomentRepository,
    private val aanbodRepository: AanbodRepository
) : CommandLineRunner {

    private val random = Random(42)

    override fun run(vararg args: String?) {
        if (teamRepository.count() > 0) return

        // Teams
        val teamNames = listOf(
            "Team Volwassenen", "Team Inburgering", "Team Partners", "Team Maatschappelijk Fit"
        )
        val teams = teamNames.map { teamRepository.save(Team(name = it)) }

        // Coaches - 5 per team
        val coachNames = listOf(
            // Team Volwassenen
            "Jan de Vries", "Maria Jansen", "Pieter Bakker", "Annemarie Visser", "Klaas Meijer",
            // Team Inburgering
            "Sophie de Groot", "Thomas Mulder", "Linda de Boer", "Henk Bos", "Fatima El Amrani",
            // Team Partners
            "Rob Dekker", "Ingrid van Dijk", "Marco Hendriks", "Petra Smits", "Arjan van den Berg",
            // Team Maatschappelijk Fit
            "Monique Kok", "Jeroen van Leeuwen", "Sandra Brouwer", "Willem Scholten", "Nadia Youssef"
        )
        val coaches = coachNames.mapIndexed { index, name ->
            coachRepository.save(Coach(fullName = name, team = teams[index / 5]))
        }

        // Inwoners
        val firstNames = listOf(
            "Ahmed", "Fatima", "Mohammed", "Aisha", "Ibrahim", "Youssef", "Khadija", "Omar",
            "Nadia", "Hassan", "Sara", "Ali", "Maryam", "Khalid", "Layla", "Rachid",
            "Amina", "Tariq", "Samira", "Jamal", "Hanan", "Mustafa", "Salma", "Bilal",
            "Zineb", "Karim", "Naima", "Adil", "Hafsa", "Samir", "Leila", "Yassine",
            "Soumaya", "Driss", "Imane", "Reda", "Wafa", "Hamid", "Loubna", "Fouad",
            "Siham", "Brahim", "Karima", "Nordin", "Houda", "Aziz", "Rajae", "Tarik",
            "Malika", "Rachida"
        )
        val lastNames = listOf(
            "El Idrissi", "Benali", "El Moussa", "Tahiri", "Bakali", "El Amrani", "Bouzid",
            "Chaabi", "El Fassi", "Moussaoui", "Haddaoui", "Berrada", "El Kharraz", "Zouhair",
            "Tazi", "Benjelloun", "El Alami", "Rachidi", "Amrani", "Ouazzani", "Kabbaj",
            "El Mansouri", "Saidi", "Lamrani", "Fassi Fihri", "Kettani", "Bennani", "Alaoui",
            "Slimani", "Bouazza", "Daoudi", "El Ouali", "Chraibi", "Touhami", "Jabri",
            "El Ghazi", "Ziani", "Bekkali", "Hajji", "Omari", "Lahlou", "Rifi",
            "Boukhari", "Meziane", "Aboutaleb", "Boussaid", "El Khattabi", "Nassiri",
            "Belhaj", "Kadiri"
        )

        val inwoners = (0 until 50).map { i ->
            inwonerRepository.save(
                Inwoner(
                    fullName = "${firstNames[i]} ${lastNames[i]}",
                    birthdate = LocalDate.of(
                        1960 + random.nextInt(40),
                        1 + random.nextInt(12),
                        1 + random.nextInt(28)
                    ),
                    administratienummer = "ADM${(100000 + i).toString()}"
                )
            )
        }

        // Date range: 2024-01 to 2026-03
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2026, 3, 31)
        val totalDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate).toInt()

        fun randomDate(): LocalDate = startDate.plusDays(random.nextInt(totalDays).toLong())

        // Plaatsingen (~200)
        val plaatsingTypes = listOf(
            "Werk", "Scholing", "Ondernemen", "Beschut werk",
            "WSW", "Banenafspraak regulier", "Banenafspraak praktijk route"
        )
        repeat(200) {
            val coach = coaches[random.nextInt(coaches.size)]
            plaatsingRepository.save(
                Plaatsing(
                    inwoner = inwoners[random.nextInt(inwoners.size)],
                    coach = coach,
                    team = coach.team!!,
                    startDate = randomDate(),
                    type = plaatsingTypes[random.nextInt(plaatsingTypes.size)]
                )
            )
        }

        // Contactmomenten (~500)
        val kanalen = listOf(
            "Telefonisch inkomend", "Telefonisch uitgaand", "Fysiek", "Videobellen",
            "E-mail inkomend", "E-mail uitgaand", "WhatsApp", "Brief"
        )
        val onderwerpen = listOf(
            "Voortgangsgesprek", "Participatiegesprek", "Brede Intake", "Plaatsing",
            "Heronderzoek", "Statusoverleg", "Evaluatie traject", "Crisisinterventie",
            "Administratieve afhandeling", "Netwerk overleg"
        )
        repeat(500) {
            val coach = coaches[random.nextInt(coaches.size)]
            contactmomentRepository.save(
                Contactmoment(
                    inwoner = inwoners[random.nextInt(inwoners.size)],
                    coach = coach,
                    team = coach.team!!,
                    date = randomDate(),
                    kanaal = kanalen[random.nextInt(kanalen.size)],
                    onderwerp = onderwerpen[random.nextInt(onderwerpen.size)]
                )
            )
        }

        // Aanbod (~300)
        val aanbodnamen = listOf(
            "Taalcoaching", "Werkfit traject", "Participatieplaats",
            "Sociale activering", "Schuldhulpverlening", "Re-integratie traject",
            "Jobcoaching", "Digitale vaardigheden", "Budgetbeheer"
        )
        val afsluitredenen = listOf(
            "Succesvol afgerond", "Voortijdig gestopt", "Administratief afgesloten",
            "Aanbod afgesloten wegens wijzigen leerroute", "Doorverwezen",
            "Niet verschenen", null
        )
        repeat(300) {
            val coach = coaches[random.nextInt(coaches.size)]
            val aanbodStartDate = randomDate()
            val afsluitreden = afsluitredenen[random.nextInt(afsluitredenen.size)]
            val eindDatum = if (afsluitreden != null) {
                aanbodStartDate.plusDays(30L + random.nextInt(180))
            } else null
            aanbodRepository.save(
                Aanbod(
                    inwoner = inwoners[random.nextInt(inwoners.size)],
                    coach = coach,
                    team = coach.team!!,
                    startDate = aanbodStartDate,
                    aanbodnaam = aanbodnamen[random.nextInt(aanbodnamen.size)],
                    afsluitreden = afsluitreden,
                    eindDatum = eindDatum
                )
            )
        }

        println("Data initialization complete: ${teams.size} teams, ${coaches.size} coaches, ${inwoners.size} inwoners, 200 plaatsingen, 500 contactmomenten, 300 aanbod records")
    }
}
