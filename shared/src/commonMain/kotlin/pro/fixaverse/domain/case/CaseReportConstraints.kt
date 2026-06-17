package pro.fixaverse.domain.case

/** Minimum inclusive length for a saved case-report result (> 20 characters). */
const val MIN_CASE_REPORT_RESULT_LENGTH = 21

fun String.isValidCaseReportResult(): Boolean = trim().length >= MIN_CASE_REPORT_RESULT_LENGTH

fun List<CaseReport>.filterValidCaseReports(): List<CaseReport> =
    filter { it.result.isValidCaseReportResult() }
