package io.andrelucas

import io.andrelucas.app.PersonService
import io.andrelucas.app.handle
import io.andrelucas.business.Person
import io.andrelucas.plugins.configureRouting
import io.andrelucas.plugins.configureSerialization
import io.andrelucas.repository.*
import io.ktor.server.application.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.debug.DebugProbes

fun main(args: Array<String>){
    io.ktor.server.netty.EngineMain.main(args)
    System.setProperty("kotlinx.coroutines.debug", "on")
    System.setProperty("kotlinx.coroutines.stacktrace.recovery", "true")
}

fun migrations() {
    createTable()
    createCacheTable()
}

val personChannelCache = Channel<Person>()
val personChannelBatchInsert = Channel<Person>()

val personRepository = PersonRepositoryImpl
val cacheService = CacheServiceImpl
val personService = PersonService.getInstance(personChannelCache, personRepository, PersonQueryImpl, cacheService)

fun Application.module() {
    DebugProbes.enableCreationStackTraces = false
    DebugProbes.install()

    migrations()

    configureSerialization()
    configureRouting(personService)

    handle(personChannelCache, personChannelBatchInsert, personRepository, cacheService)
}
