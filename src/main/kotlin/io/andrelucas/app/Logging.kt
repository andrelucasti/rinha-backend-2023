package io.andrelucas.app

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.util.logging.*

internal val LOGGER = KtorSimpleLogger("io.andrelucas.app")

val RequestTracePlugin = createRouteScopedPlugin("RequestTracePlugin", { }) {
    onCall { call ->
        LOGGER.trace("Processing call: ${call.request.uri}")
    }
}