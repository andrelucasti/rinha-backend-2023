package io.andrelucas

import io.andrelucas.plugins.*
import io.andrelucas.repository.createCacheTable
import io.andrelucas.repository.createTable
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.migrations() {
    runBlocking {
        createTable()
        createCacheTable()
    }
}

fun Application.module() {
    migrations()
    configureSerialization()
    configureRouting()
}
