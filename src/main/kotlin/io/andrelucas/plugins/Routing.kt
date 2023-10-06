package io.andrelucas.plugins

import io.andrelucas.app.*
import io.andrelucas.business.EntityNotFoundException
import io.andrelucas.business.NumericException
import io.andrelucas.repository.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
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

    val personService = PersonService(
        PersonRepositoryImpl(dataBase()),
        PersonQueyImpl(dataBase())
    )

    routing {
        createPerson(personService)
        findPersonById(personService)
        findPersonByTerm(personService)
        countPerson(personService)
    }
}
