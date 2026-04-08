package com.commutr.dashboard.service.dashboard

import com.commutr.dashboard.dto.*

interface DashboardProvider {
    val id: String
    val label: String
    fun getConfig(): DashboardConfigDto
    fun getFilterOptions(filters: Map<String, String?>): Map<String, List<String>>
    fun getSummary(filters: Map<String, String?>): SummaryDataDto
    fun getChartData(filters: Map<String, String?>): ChartDataDto
    fun getDetails(month: String, category: String?, filters: Map<String, String?>): DetailDataDto
}
