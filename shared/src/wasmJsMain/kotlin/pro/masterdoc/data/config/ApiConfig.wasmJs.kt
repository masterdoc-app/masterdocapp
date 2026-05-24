package pro.masterdoc.data.config

/** GitHub Pages is HTTPS-only; browser blocks HTTP API (mixed content). */
internal actual fun defaultApiBaseUrl(): String = "https://api.masterdoc.pro/v1"
