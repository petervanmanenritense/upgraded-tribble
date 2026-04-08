package com.commutr.dashboard.controller

import com.commutr.dashboard.dto.*
import com.commutr.dashboard.service.DashboardService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/dashboards")
class DashboardController(
    private val dashboardService: DashboardService
) {

    @GetMapping
    fun listDashboards(): List<DashboardSummaryDto> = dashboardService.listDashboards()

    @GetMapping("/{id}/config")
    fun getConfig(@PathVariable id: String): DashboardConfigDto =
        dashboardService.getProvider(id).getConfig()

    @GetMapping("/{id}/filters")
    fun getFilters(@PathVariable id: String, @RequestParam params: Map<String, String>): Map<String, List<String>> {
        val filters = params.filterKeys { it != "id" }.mapValues { it.value.ifBlank { null } }
        return dashboardService.getProvider(id).getFilterOptions(filters)
    }

    @GetMapping("/{id}/summary")
    fun getSummary(@PathVariable id: String, @RequestParam params: Map<String, String>): SummaryDataDto {
        val filters = params.filterKeys { it != "id" }.mapValues { it.value.ifBlank { null } }
        return dashboardService.getProvider(id).getSummary(filters)
    }

    @GetMapping("/{id}/chart")
    fun getChartData(@PathVariable id: String, @RequestParam params: Map<String, String>): ChartDataDto {
        val filters = params.filterKeys { it != "id" }.mapValues { it.value.ifBlank { null } }
        return dashboardService.getProvider(id).getChartData(filters)
    }

    @GetMapping("/{id}/details")
    fun getDetails(
        @PathVariable id: String,
        @RequestParam month: String,
        @RequestParam(required = false) category: String?,
        @RequestParam params: Map<String, String>
    ): DetailDataDto {
        val filters = params.filterKeys { it !in listOf("id", "month", "category") }
            .mapValues { it.value.ifBlank { null } }
        return dashboardService.getProvider(id).getDetails(month, category, filters)
    }
}
