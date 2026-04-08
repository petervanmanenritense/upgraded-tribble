package com.commutr.dashboard.dto

data class DashboardSummaryDto(val id: String, val label: String)

data class FilterConfig(val key: String, val label: String)
data class ChartConfig(val type: String, val xLabel: String, val yLabel: String, val colors: List<String>)
data class ColumnConfig(val key: String, val label: String)
data class SummaryCardConfig(val key: String, val label: String)
data class DetailTableConfig(val columns: List<ColumnConfig>)
data class DashboardConfigDto(
    val id: String,
    val label: String,
    val filters: List<FilterConfig>,
    val chart: ChartConfig,
    val summaryCards: List<SummaryCardConfig>,
    val detailTable: DetailTableConfig
)

data class SummaryDataDto(val total: Int, val avgPerMonth: Int, val topMonth: TopMonth?)
data class TopMonth(val label: String, val value: Int)

data class ChartSeriesDto(val label: String, val data: List<Int>)
data class ChartDataDto(val labels: List<String>, val series: List<ChartSeriesDto>)

data class DetailDataDto(val title: String, val rows: List<Map<String, Any?>>)
