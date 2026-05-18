package pro.masterdoc.data.config

data class ApiConfig(
    val baseUrl: String,
)

/** Platform default when [OnyxBuildConfig.BASE_URL] is not set in local.properties. */
internal expect fun defaultApiBaseUrl(): String

/** Onyx/base URL from local.properties, otherwise the platform default. */
fun apiBaseUrl(): String = OnyxBuildConfig.BASE_URL.ifBlank { defaultApiBaseUrl() }

/** Onyx PAT from local.properties (onyx.pat), or null if unset. */
fun onyxPatOrNull(): String? = OnyxBuildConfig.PAT.takeIf { it.isNotBlank() }
