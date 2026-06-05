package pro.masterdoc.domain.case

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CaseReportConstraintsTest {

    @Test
    fun isValidCaseReportResult_requiresMoreThan20Characters() {
        assertFalse("".isValidCaseReportResult())
        assertFalse("12345678901234567890".isValidCaseReportResult()) // exactly 20 chars
        assertTrue("больше двадцати символов в отчёте".isValidCaseReportResult())
    }

    @Test
    fun filterValidCaseReports_dropsShortEntries() {
        val reports = listOf(
            CaseReport("1", "", 1, null, "короткий", emptyList()),
            CaseReport("2", "", 1, null, "достаточно длинный текст отчёта", emptyList()),
        )
        assertEquals(1, reports.filterValidCaseReports().size)
        assertEquals("2", reports.filterValidCaseReports().single().id)
    }
}
