package de.hpi.cloud.webfrontend

import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.routing.Route
import io.ktor.util.StringValues
import io.ktor.util.filter
import io.ktor.util.pipeline.ContextDsl

fun Headers.filterContentTypeLength(): StringValues {
    return filter { key, _ ->
        !key.equals(HttpHeaders.ContentType, ignoreCase = true)
                && !key.equals(HttpHeaders.ContentLength, ignoreCase = true)
    }
}
