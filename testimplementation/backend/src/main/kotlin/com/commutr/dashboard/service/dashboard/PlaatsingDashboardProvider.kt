package com.commutr.dashboard.service.dashboard

import com.commutr.dashboard.dto.*
import com.commutr.dashboard.model.Plaatsing
import com.commutr.dashboard.repository.PlaatsingRepository
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class PlaatsingDashboardProvider(
    private val plaatsingRepository: PlaatsingRepository
) : DashboardProvider {

    override val id = "plaatsingen"
    override val label = "Plaatsingen"

    override fun getConfig(): DashboardConfigDto = DashboardConfigDto(
        id = id,
        label = label,
        filters = listOf(
            FilterConfig("year", "Jaar"),
            FilterConfig("team", "Team"),
            FilterConfig("coach", "Coach"),
            FilterConfig("soort", "Soort"),
            FilterConfig("type", "Type")
        ),
        chart = ChartConfig("bar", "Maand", "Aantal", listOf("#0f62fe")),
        summaryCards = listOf(
            SummaryCardConfig("total", "Totaal"),
            SummaryCardConfig("avgPerMonth", "Gem. per maand"),
            SummaryCardConfig("topMonth", "Top maand")
        ),
        detailTable = DetailTableConfig(
            columns = listOf(
                ColumnConfig("date", "Datum"),
                ColumnConfig("inwoner", "Inwoner"),
                ColumnConfig("administratienummer", "Administratienummer"),
                ColumnConfig("coach", "Coach"),
                ColumnConfig("team", "Team"),
                ColumnConfig("soort", "Soort"),
                ColumnConfig("type", "Type"),
                ColumnConfig("zaakstatus", "Zaakstatus"),
                ColumnConfig("afsluitreden", "Afsluitreden")
            )
        )
    )

    private fun buildSpec(filters: Map<String, String?>, excludeKey: String? = null): Specification<Plaatsing> {
        return Specification { root, _, cb ->
            val predicates = mutableListOf<Predicate>()

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
            if (excludeKey != "soort") {
                filters["soort"]?.takeIf { it.isNotBlank() }?.let { soort ->
                    predicates.add(cb.equal(root.get<String>("soort"), soort))
                }
            }
            if (excludeKey != "type") {
                filters["type"]?.takeIf { it.isNotBlank() }?.let { type ->
                    predicates.add(cb.equal(root.get<String>("type"), type))
                }
            }

            cb.and(*predicates.toTypedArray())
        }
    }

    override fun getFilterOptions(filters: Map<String, String?>): Map<String, List<String>> {
        val result = mutableMapOf<String, List<String>>()

        val forYear = plaatsingRepository.findAll(buildSpec(filters, excludeKey = "year"))
        result["year"] = forYear.map { it.startDate.year.toString() }.distinct().sorted()

        val forTeam = plaatsingRepository.findAll(buildSpec(filters, excludeKey = "team"))
        result["team"] = forTeam.mapNotNull { it.team?.name }.distinct().sorted()

        val forCoach = plaatsingRepository.findAll(buildSpec(filters, excludeKey = "coach"))
        result["coach"] = forCoach.mapNotNull { it.coach?.fullName }.distinct().sorted()

        val forSoort = plaatsingRepository.findAll(buildSpec(filters, excludeKey = "soort"))
        result["soort"] = forSoort.map { it.soort }.distinct().sorted()

        val forType = plaatsingRepository.findAll(buildSpec(filters, excludeKey = "type"))
        result["type"] = forType.mapNotNull { it.type }.distinct().sorted()

        return result
    }

    override fun getSummary(filters: Map<String, String?>): SummaryDataDto {
        val records = plaatsingRepository.findAll(buildSpec(filters))
        return buildSummary(records.map { it.startDate })
    }

    override fun getChartData(filters: Map<String, String?>): ChartDataDto {
        val records = plaatsingRepository.findAll(buildSpec(filters))
        val yearFilter = filters["year"]?.takeIf { it.isNotBlank() }
        val months = buildMonthLabels(records.map { it.startDate }, yearFilter)
        val counts = months.map { ym ->
            records.count {
                YearMonth.from(it.startDate) == ym
            }
        }
        val labels = months.map { formatMonth(it) }
        return ChartDataDto(
            labels = labels,
            series = listOf(ChartSeriesDto(label = "Plaatsingen", data = counts))
        )
    }

    override fun getDetails(month: String, category: String?, filters: Map<String, String?>): DetailDataDto {
        val ym = YearMonth.parse(month)
        val records = plaatsingRepository.findAll(buildSpec(filters))
            .filter { YearMonth.from(it.startDate) == ym }

        val rows = records.map { p ->
            mapOf<String, Any?>(
                "date" to p.startDate.toString(),
                "inwoner" to (p.inwoner?.fullName),
                "administratienummer" to (p.inwoner?.administratienummer),
                "coach" to (p.coach?.fullName),
                "team" to (p.team?.name),
                "soort" to p.soort,
                "type" to p.type,
                "zaakstatus" to p.zaakstatus,
                "afsluitreden" to if (p.zaakstatus == "Afgerond") p.afsluitreden else null
            )
        }
        return DetailDataDto(title = "Plaatsingen - ${formatMonth(ym)}", rows = rows)
    }

    companion object {
        private val MONTH_NAMES = arrayOf(
            "Jan", "Feb", "Mrt", "Apr", "Mei", "Jun",
            "Jul", "Aug", "Sep", "Okt", "Nov", "Dec"
        )

        fun formatMonth(ym: YearMonth): String = "${MONTH_NAMES[ym.monthValue - 1]} ${ym.year}"

        fun buildMonthLabels(dates: List<LocalDate>, yearFilter: String?): List<YearMonth> {
            if (yearFilter != null) {
                val year = yearFilter.toInt()
                return (1..12).map { YearMonth.of(year, it) }
            }
            if (dates.isEmpty()) return emptyList()
            val min = dates.minOf { YearMonth.from(it) }
            val max = dates.maxOf { YearMonth.from(it) }
            val result = mutableListOf<YearMonth>()
            var current = min
            while (!current.isAfter(max)) {
                result.add(current)
                current = current.plusMonths(1)
            }
            return result
        }

        fun buildSummary(dates: List<LocalDate>): SummaryDataDto {
            val total = dates.size
            if (total == 0) return SummaryDataDto(0, 0, null)
            val byMonth = dates.groupBy { YearMonth.from(it) }
            val monthCount = byMonth.size
            val avgPerMonth = if (monthCount > 0) total / monthCount else 0
            val topEntry = byMonth.maxByOrNull { it.value.size }
            val topMonth = topEntry?.let {
                TopMonth(formatMonth(it.key), it.value.size)
            }
            return SummaryDataDto(total, avgPerMonth, topMonth)
        }
    }
}
