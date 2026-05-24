package pro.masterdoc.data.config

data class ApiConfig(
    val baseUrl: String,
)

/** Production API (HTTP until TLS on VPS: certbot --nginx). Used when [MasterdocBuildConfig.API_BASE_URL] is blank. */
internal const val DEFAULT_API_BASE_URL = "http://api.masterdoc.pro/v1"

/** Platform default when [MasterdocBuildConfig.API_BASE_URL] is not set in local.properties. */
internal expect fun defaultApiBaseUrl(): String

/** Masterdoc API base URL (includes /v1), from local.properties or platform default. */
fun apiBaseUrl(): String = MasterdocBuildConfig.API_BASE_URL.ifBlank { defaultApiBaseUrl() }
