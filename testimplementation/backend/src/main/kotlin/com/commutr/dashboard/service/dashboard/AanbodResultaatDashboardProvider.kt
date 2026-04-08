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
class AanbodResultaatDashboardProvider(
    private val aanbodRepository: AanbodRepository
) : DashboardProvider {

    override val id = "aanbodresultaat"
    override val label = "Aanbod resultaat"

    companion object {
        val EXCLUDED_AFSLUITREDENEN = AanbodDashboardProvider.EXCLUDED_AFSLUITREDENEN

        val COLOR_PALETTE = listOf(
            "#6929c4", "#1192e8", "#005d5d", "#9f1853", "#fa4d56",
            "#570408", "#198038", "#002d9c", "#ee538b", "#b28600"
        )
    }

    override fun getConfig(): DashboardConfigDto = DashboardConfigDto(
        id = id,
        label = label,
        filters = listOf(
            FilterConfig("year", "Jaar"),
            FilterConfig("team", "Team"),
            FilterConfig("aanbodnaam", "Aanbodnaam"),
            FilterConfig("afsluitreden", "Afsluitreden")
        ),
        chart = ChartConfig("stacked-bar", "Maand", "Aantal", COLOR_PALETTE),
        summaryCards = listOf(
            SummaryCardConfig("total", "Totaal resultaten"),
            SummaryCardConfig("avgPerMonth", "Gem. per maand"),
            SummaryCardConfig("topMonth", "Top maand")
        ),
        detailTable = DetailTableConfig(
            columns = listOf(
                ColumnConfig("startDate", "Startdatum"),
                ColumnConfig("aanbodnaam", "Aanbodnaam"),
                ColumnConfig("inwoner", "Inwoner"),
                ColumnConfig("administratienummer", "Administratienummer"),
                ColumnConfig("team", "Team"),
                ColumnConfig("afsluitreden", "Afsluitreden")
            )
        )
    )

    private fun buildSpec(filters: Map<String, String?>, excludeKey: String? = null): Specification<Aanbod> {
        return Specification { root, _, cb ->
            val predicates = mutableListOf<Predicate>()

            // Must have an afsluitreden (only records with a result)
            predicates.add(cb.isNotNull(root.get<String>("afsluitreden")))

            // Exclude admin-closed records
            predicates.add(
                cb.not(root.get<String>("afsluitreden").`in`(EXCLUDED_AFSLUITREDENEN))
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
                "team" to (a.team?.name),
                "afsluitreden" to a.afsluitreden
            )
        }

        val title = if (!category.isNullOrBlank()) {
            "Aanbod resultaat ($category) - ${PlaatsingDashboardProvider.formatMonth(ym)}"
        } else {
            "Aanbod resultaat - ${PlaatsingDashboardProvider.formatMonth(ym)}"
        }
        return DetailDataDto(title = title, rows = rows)
    }
}
