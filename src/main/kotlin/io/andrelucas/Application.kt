package io.andrelucas

import io.andrelucas.plugins.configureRouting
import io.andrelucas.plugins.configureSerialization
import io.andrelucas.repository.createTable
import io.ktor.server.application.*
import kotlinx.coroutines.debug.DebugProbes
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>){
    io.ktor.server.netty.EngineMain.main(args)
    System.setProperty("kotlinx.coroutines.debug", "on")
    System.setProperty("kotlinx.coroutines.stacktrace.recovery", "true")
    // It is recommended to set this to false to avoid performance hits with the DebugProbes option!
    DebugProbes.enableCreationStackTraces = false
    DebugProbes.install()

}

fun Application.migrations() {
    runBlocking {
        createTable()
    }
}

fun Application.module() {
    migrations()
    configureSerialization()
    configureRouting()
}
