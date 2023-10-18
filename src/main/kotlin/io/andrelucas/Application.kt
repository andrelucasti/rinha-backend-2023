package io.andrelucas

import io.andrelucas.plugins.configureRouting
import io.andrelucas.plugins.configureSerialization
import io.andrelucas.repository.createTable
import io.ktor.server.application.*
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

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
