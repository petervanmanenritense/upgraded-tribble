package com.commutr.dashboard.service

import com.commutr.dashboard.dto.*
import com.commutr.dashboard.service.dashboard.DashboardProvider
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.web.server.ResponseStatusException

class DashboardServiceTest {

    private fun fakeProvider(id: String, label: String) = object : DashboardProvider {
        override val id = id
        override val label = label
        override fun getConfig() = DashboardConfigDto(id, label, emptyList(), ChartConfig("bar", "", "", emptyList()), emptyList(), DetailTableConfig(emptyList()))
        override fun getFilterOptions(filters: Map<String, String?>) = emptyMap<String, List<String>>()
        override fun getSummary(filters: Map<String, String?>) = SummaryDataDto(0, 0, null)
        override fun getChartData(filters: Map<String, String?>) = ChartDataDto(emptyList(), emptyList())
        override fun getDetails(month: String, category: String?, filters: Map<String, String?>) = DetailDataDto("", emptyList())
    }

    @Test
    fun `listDashboards returns all providers`() {
        val service = DashboardService(listOf(
            fakeProvider("a", "Alpha"),
            fakeProvider("b", "Beta")
        ))
        val list = service.listDashboards()
        assertEquals(2, list.size)
        assertEquals("a", list[0].id)
        assertEquals("Beta", list[1].label)
    }

    @Test
    fun `getProvider returns correct provider`() {
        val providerA = fakeProvider("a", "Alpha")
        val service = DashboardService(listOf(providerA, fakeProvider("b", "Beta")))
        assertSame(providerA, service.getProvider("a"))
    }

    @Test
    fun `getProvider throws 404 for unknown id`() {
        val service = DashboardService(listOf(fakeProvider("a", "Alpha")))
        val ex = assertThrows<ResponseStatusException> { service.getProvider("unknown") }
        assertEquals(404, ex.statusCode.value())
    }

    @Test
    fun `listDashboards with no providers returns empty`() {
        val service = DashboardService(emptyList())
        assertTrue(service.listDashboards().isEmpty())
    }
}
