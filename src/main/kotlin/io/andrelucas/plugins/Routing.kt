package io.andrelucas.plugins

import io.andrelucas.app.*
import io.andrelucas.business.EntityNotFoundException
import io.andrelucas.business.NumericException
import io.andrelucas.repository.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.application.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.debug.DebugProbes
import org.jetbrains.exposed.sql.Database

fun Application.configureRouting() {
    log.info("Configuring routing")
    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            call.respondText(text = "${cause.message}", status = HttpStatusCode.UnprocessableEntity)
        }

        exception<NumericException> { call, cause ->
            call.respondText(text = "${cause.message}", status = HttpStatusCode.BadRequest)
        }

        exception<BadRequestException> { call, cause ->
            call.respondText(text = "${cause.message}", status = HttpStatusCode.BadRequest)
        }

        exception<EntityNotFoundException> { call, cause ->
            call.respondText(text = "${cause.message}", status = HttpStatusCode.NotFound)
        }
    }

    val personService = PersonService.getInstance(PersonRepositoryImpl, PersonQueryImpl, CacheServiceImpl)

    routing {
        createPerson(personService)
        findPersonById(personService)
        findPersonByTerm(personService)
        countPerson(personService)
        coroutine()
    }
}
