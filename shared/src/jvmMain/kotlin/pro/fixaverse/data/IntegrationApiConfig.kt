package pro.fixaverse.data

import pro.fixaverse.data.config.ApiConfig
import pro.fixaverse.data.config.FixaverseBuildConfig

/** Base URL for JVM/desktop integration tests (HTTPS; avoids 301 from http://api.masterdoc.pro). */
fun integrationApiConfig(): ApiConfig {
    val url = System.getenv("MASTERDOC_API_BASE_URL")?.trim()?.takeIf { it.isNotEmpty() }
        ?: FixaverseBuildConfig.API_BASE_URL.trim().takeIf { it.isNotEmpty() }
        ?: "https://api.masterdoc.pro/v1"
    return ApiConfig(url)
}
