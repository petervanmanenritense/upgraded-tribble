package com.commutr.dashboard.service.dashboard

import com.commutr.dashboard.dto.*
import com.commutr.dashboard.model.Contactmoment
import com.commutr.dashboard.repository.ContactmomentRepository
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.YearMonth

@Service
class ContactmomentDashboardProvider(
    private val contactmomentRepository: ContactmomentRepository
) : DashboardProvider {

    override val id = "contactmomenten"
    override val label = "Contactmomenten"

    override fun getConfig(): DashboardConfigDto = DashboardConfigDto(
        id = id,
        label = label,
        filters = listOf(
            FilterConfig("year", "Jaar"),
            FilterConfig("team", "Team"),
            FilterConfig("coach", "Coach"),
            FilterConfig("kanaal", "Kanaal"),
            FilterConfig("onderwerp", "Onderwerp")
        ),
        chart = ChartConfig("bar", "Maand", "Aantal", listOf("#198038")),
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
                ColumnConfig("kanaal", "Kanaal"),
                ColumnConfig("onderwerp", "Onderwerp")
            )
        )
    )

    private fun buildSpec(filters: Map<String, String?>, excludeKey: String? = null): Specification<Contactmoment> {
        return Specification { root, _, cb ->
            val predicates = mutableListOf<Predicate>()

            if (excludeKey != "year") {
                filters["year"]?.takeIf { it.isNotBlank() }?.let { year ->
                    val yearInt = year.toInt()
                    predicates.add(
                        cb.between(
                            root.get("date"),
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
            if (excludeKey != "kanaal") {
                filters["kanaal"]?.takeIf { it.isNotBlank() }?.let { kanaal ->
                    predicates.add(cb.equal(root.get<String>("kanaal"), kanaal))
                }
            }
            if (excludeKey != "onderwerp") {
                filters["onderwerp"]?.takeIf { it.isNotBlank() }?.let { onderwerp ->
                    predicates.add(cb.equal(root.get<String>("onderwerp"), onderwerp))
                }
            }

            cb.and(*predicates.toTypedArray())
        }
    }

    override fun getFilterOptions(filters: Map<String, String?>): Map<String, List<String>> {
        val result = mutableMapOf<String, List<String>>()

        val forYear = contactmomentRepository.findAll(buildSpec(filters, excludeKey = "year"))
        result["year"] = forYear.map { it.date.year.toString() }.distinct().sorted()

        val forTeam = contactmomentRepository.findAll(buildSpec(filters, excludeKey = "team"))
        result["team"] = forTeam.mapNotNull { it.team?.name }.distinct().sorted()

        val forCoach = contactmomentRepository.findAll(buildSpec(filters, excludeKey = "coach"))
        result["coach"] = forCoach.mapNotNull { it.coach?.fullName }.distinct().sorted()

        val forKanaal = contactmomentRepository.findAll(buildSpec(filters, excludeKey = "kanaal"))
        result["kanaal"] = forKanaal.map { it.kanaal }.distinct().sorted()

        val forOnderwerp = contactmomentRepository.findAll(buildSpec(filters, excludeKey = "onderwerp"))
        result["onderwerp"] = forOnderwerp.map { it.onderwerp }.distinct().sorted()

        return result
    }

    override fun getSummary(filters: Map<String, String?>): SummaryDataDto {
        val records = contactmomentRepository.findAll(buildSpec(filters))
        return PlaatsingDashboardProvider.buildSummary(records.map { it.date })
    }

    override fun getChartData(filters: Map<String, String?>): ChartDataDto {
        val records = contactmomentRepository.findAll(buildSpec(filters))
        val yearFilter = filters["year"]?.takeIf { it.isNotBlank() }
        val months = PlaatsingDashboardProvider.buildMonthLabels(records.map { it.date }, yearFilter)
        val counts = months.map { ym ->
            records.count { YearMonth.from(it.date) == ym }
        }
        val labels = months.map { PlaatsingDashboardProvider.formatMonth(it) }
        return ChartDataDto(
            labels = labels,
            series = listOf(ChartSeriesDto(label = "Contactmomenten", data = counts))
        )
    }

    override fun getDetails(month: String, category: String?, filters: Map<String, String?>): DetailDataDto {
        val ym = YearMonth.parse(month)
        val records = contactmomentRepository.findAll(buildSpec(filters))
            .filter { YearMonth.from(it.date) == ym }

        val rows = records.map { c ->
            mapOf<String, Any?>(
                "date" to c.date.toString(),
                "inwoner" to (c.inwoner?.fullName),
                "administratienummer" to (c.inwoner?.administratienummer),
                "coach" to (c.coach?.fullName),
                "team" to (c.team?.name),
                "kanaal" to c.kanaal,
                "onderwerp" to c.onderwerp
            )
        }
        return DetailDataDto(title = "Contactmomenten - ${PlaatsingDashboardProvider.formatMonth(ym)}", rows = rows)
    }
}
