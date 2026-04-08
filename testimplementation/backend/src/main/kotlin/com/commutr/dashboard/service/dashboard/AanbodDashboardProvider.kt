package com.commutr.dashboard.service.dashboard

import com.commutr.dashboard.dto.*
import com.commutr.dashboard.model.Aanbod
import com.commutr.dashboard.repository.AanbodRepository
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.YearMonth

@Service
class AanbodDashboardProvider(
    private val aanbodRepository: AanbodRepository
) : DashboardProvider {

    override val id = "aanbod"
    override val label = "Aanbod"

    companion object {
        val EXCLUDED_AFSLUITREDENEN = listOf(
            "Administratief afgesloten",
            "Aanbod afgesloten wegens wijzigen leerroute"
        )

        val COLOR_PALETTE = listOf(
            "#0f62fe", "#198038", "#8a3ffc", "#ee538b", "#ff832b",
            "#002d9c", "#005d5d", "#b28600", "#da1e28", "#6929c4"
        )
    }

    override fun getConfig(): DashboardConfigDto = DashboardConfigDto(
        id = id,
        label = label,
        filters = listOf(
            FilterConfig("year", "Jaar"),
            FilterConfig("team", "Team"),
            FilterConfig("coach", "Coach"),
            FilterConfig("aanbodnaam", "Aanbodnaam"),
            FilterConfig("afsluitreden", "Afsluitreden")
        ),
        chart = ChartConfig("stacked-bar", "Maand", "Aantal", COLOR_PALETTE),
        summaryCards = listOf(
            SummaryCardConfig("total", "Totaal"),
            SummaryCardConfig("avgPerMonth", "Gem. per maand"),
            SummaryCardConfig("topMonth", "Top maand")
        ),
        detailTable = DetailTableConfig(
            columns = listOf(
                ColumnConfig("startDate", "Startdatum"),
                ColumnConfig("aanbodnaam", "Aanbodnaam"),
                ColumnConfig("inwoner", "Inwoner"),
                ColumnConfig("administratienummer", "Administratienummer"),
                ColumnConfig("coach", "Coach"),
                ColumnConfig("team", "Team"),
                ColumnConfig("afsluitreden", "Afsluitreden"),
                ColumnConfig("eindDatum", "Einddatum")
            )
        )
    )

    private fun baseExclusion(): Specification<Aanbod> {
        return Specification { root, _, cb ->
            cb.or(
                cb.isNull(root.get<String>("afsluitreden")),
                cb.not(root.get<String>("afsluitreden").`in`(EXCLUDED_AFSLUITREDENEN))
            )
        }
    }

    private fun buildSpec(filters: Map<String, String?>, excludeKey: String? = null): Specification<Aanbod> {
        return Specification { root, query, cb ->
            val predicates = mutableListOf<Predicate>()

            // Always exclude admin-closed records
            predicates.add(
                cb.or(
                    cb.isNull(root.get<String>("afsluitreden")),
                    cb.not(root.get<String>("afsluitreden").`in`(EXCLUDED_AFSLUITREDENEN))
                )
            )

            if (excludeKey != "year") {
                filters["year"]?.takeIf { it.isNotBlank() }?.let { year ->
                    val yearInt = year.toInt()
                    predicates.add(
                        cb.between(
                            root.get("startDate"),
                            LocalDate.of(yearInt, 1, 1),
                            LocalDate.of(yearInt, 12, 31)
                        )
                    )
                }
            }
            if (excludeKey != "team") {
                filters["team"]?.takeIf { it.isNotBlank() }?.let { team ->
                    predicates.add(cb.equal(root.get<Any>("team").get<String>("name"), team))
                }
            }
            if (excludeKey != "coach") {
                filters["coach"]?.takeIf { it.isNotBlank() }?.let { coach ->
                    predicates.add(cb.equal(root.get<Any>("coach").get<String>("fullName"), coach))
                }
            }
            if (excludeKey != "aanbodnaam") {
                filters["aanbodnaam"]?.takeIf { it.isNotBlank() }?.let { naam ->
                    predicates.add(cb.equal(root.get<String>("aanbodnaam"), naam))
                }
            }
            if (excludeKey != "afsluitreden") {
                filters["afsluitreden"]?.takeIf { it.isNotBlank() }?.let { reden ->
                    predicates.add(cb.equal(root.get<String>("afsluitreden"), reden))
                }
            }

            cb.and(*predicates.toTypedArray())
        }
    }

    override fun getFilterOptions(filters: Map<String, String?>): Map<String, List<String>> {
        val result = mutableMapOf<String, List<String>>()

        val forYear = aanbodRepository.findAll(buildSpec(filters, excludeKey = "year"))
        result["year"] = forYear.map { it.startDate.year.toString() }.distinct().sorted()

        val forTeam = aanbodRepository.findAll(buildSpec(filters, excludeKey = "team"))
        result["team"] = forTeam.mapNotNull { it.team?.name }.distinct().sorted()

        val forCoach = aanbodRepository.findAll(buildSpec(filters, excludeKey = "coach"))
        result["coach"] = forCoach.mapNotNull { it.coach?.fullName }.distinct().sorted()

        val forNaam = aanbodRepository.findAll(buildSpec(filters, excludeKey = "aanbodnaam"))
        result["aanbodnaam"] = forNaam.map { it.aanbodnaam }.distinct().sorted()

        val forReden = aanbodRepository.findAll(buildSpec(filters, excludeKey = "afsluitreden"))
        result["afsluitreden"] = forReden.mapNotNull { it.afsluitreden }.distinct().sorted()

        return result
    }

    override fun getSummary(filters: Map<String, String?>): SummaryDataDto {
        val records = aanbodRepository.findAll(buildSpec(filters))
        return PlaatsingDashboardProvider.buildSummary(records.map { it.startDate })
    }

    override fun getChartData(filters: Map<String, String?>): ChartDataDto {
        val records = aanbodRepository.findAll(buildSpec(filters))
        val yearFilter = filters["year"]?.takeIf { it.isNotBlank() }
        val months = PlaatsingDashboardProvider.buildMonthLabels(records.map { it.startDate }, yearFilter)
        val labels = months.map { PlaatsingDashboardProvider.formatMonth(it) }

        // Stacked: each series is one aanbodnaam
        val aanbodnamen = records.map { it.aanbodnaam }.distinct().sorted()
        val series = aanbodnamen.map { naam ->
            val data = months.map { ym ->
                records.count { it.aanbodnaam == naam && YearMonth.from(it.startDate) == ym }
            }
            ChartSeriesDto(label = naam, data = data)
        }

        return ChartDataDto(labels = labels, series = series)
    }

    override fun getDetails(month: String, category: String?, filters: Map<String, String?>): DetailDataDto {
        val ym = YearMonth.parse(month)
        var records = aanbodRepository.findAll(buildSpec(filters))
            .filter { YearMonth.from(it.startDate) == ym }

        if (!category.isNullOrBlank()) {
            records = records.filter { it.aanbodnaam == category }
        }

        val rows = records.map { a ->
            mapOf<String, Any?>(
                "startDate" to a.startDate.toString(),
                "aanbodnaam" to a.aanbodnaam,
                "inwoner" to (a.inwoner?.fullName),
                "administratienummer" to (a.inwoner?.administratienummer),
                "coach" to (a.coach?.fullName),
                "team" to (a.team?.name),
                "afsluitreden" to a.afsluitreden,
                "eindDatum" to a.eindDatum?.toString()
            )
        }

        val title = if (!category.isNullOrBlank()) {
            "Aanbod ($category) - ${PlaatsingDashboardProvider.formatMonth(ym)}"
        } else {
            "Aanbod - ${PlaatsingDashboardProvider.formatMonth(ym)}"
        }
        return DetailDataDto(title = title, rows = rows)
    }
}
