package com.commutr.dashboard.service

import com.commutr.dashboard.dto.DashboardSummaryDto
import com.commutr.dashboard.service.dashboard.DashboardProvider
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class DashboardService(
    private val providers: List<DashboardProvider>
) {
    fun listDashboards(): List<DashboardSummaryDto> =
        providers.map { DashboardSummaryDto(it.id, it.label) }

    fun getProvider(id: String): DashboardProvider =
        providers.find { it.id == id }
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Dashboard '$id' not found")
}
